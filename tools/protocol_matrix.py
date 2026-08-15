#!/usr/bin/env python3
"""Validate and run the evidence-first protocol-state matrix."""

from __future__ import annotations

import argparse
import json
import socket
import struct
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MATRIX = ROOT / "compatibility" / "protocol-state-matrix.yml"
RESULT_VALUES = {"PASS", "FAIL", "BLOCKED", "NOT_RUN"}


def now() -> str:
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")


def load_yaml(path: Path) -> dict[str, Any]:
    try:
        import yaml  # type: ignore
    except ImportError as exc:
        raise RuntimeError("PyYAML is required to validate the protocol matrix") from exc
    with path.open("r", encoding="utf-8") as handle:
        value = yaml.safe_load(handle)
    if not isinstance(value, dict):
        raise ValueError(f"{path} must contain a YAML object")
    return value


def output(value: Any, destination: Path | None) -> None:
    rendered = json.dumps(value, ensure_ascii=False, indent=2, sort_keys=True) + "\n"
    if destination is None:
        sys.stdout.write(rendered)
        return
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(rendered, encoding="utf-8")
    print(destination)


def validate_matrix(path: Path) -> dict[str, Any]:
    matrix = load_yaml(path)
    errors: list[str] = []
    if matrix.get("schema_version") != 1:
        errors.append("schema_version must be 1")
    for field in ("matrix_id", "environment_id", "version_pins", "cases"):
        if field not in matrix:
            errors.append(f"missing {field}")
    pins = matrix.get("version_pins")
    if not isinstance(pins, dict):
        errors.append("version_pins must be an object")
    else:
        for field in ("minecraft", "server", "server_build", "java", "packetevents", "p2p", "protocol_version"):
            if pins.get(field) in (None, ""):
                errors.append(f"version_pins.{field} must be pinned")
    cases = matrix.get("cases")
    seen: set[str] = set()
    if not isinstance(cases, list) or not cases:
        errors.append("cases must be a non-empty list")
    else:
        for case in cases:
            if not isinstance(case, dict):
                errors.append("each case must be an object")
                continue
            case_id = case.get("id")
            if not isinstance(case_id, str) or not case_id:
                errors.append("each case must have a string id")
                continue
            if case_id in seen:
                errors.append(f"duplicate case id: {case_id}")
            seen.add(case_id)
            if case.get("result") not in RESULT_VALUES:
                errors.append(f"case {case_id} has invalid result {case.get('result')}")
            if case.get("status") not in ({"NOT_CERTIFIED"} | RESULT_VALUES | {"BOOT", "CORE", "PACKET_BEHAVIOR", "FULLY_TESTED"}):
                errors.append(f"case {case_id} has invalid status {case.get('status')}")
            observations = case.get("required_observations")
            if not isinstance(observations, list) or not observations:
                errors.append(f"case {case_id} has no required_observations")
            if case.get("result") == "PASS" and not case.get("evidence"):
                errors.append(f"case {case_id} is PASS without evidence")
            if case.get("status") == "FULLY_TESTED" and case.get("result") != "PASS":
                errors.append(f"case {case_id} is FULLY_TESTED without result PASS")
    if errors:
        raise ValueError("protocol matrix validation failed:\n- " + "\n- ".join(errors))
    return matrix


def encode_varint(value: int) -> bytes:
    if value < 0:
        value &= 0xFFFFFFFF
    result = bytearray()
    while True:
        part = value & 0x7F
        value >>= 7
        if value:
            result.append(part | 0x80)
        else:
            result.append(part)
            return bytes(result)


def decode_varint(data: bytes, offset: int = 0) -> tuple[int, int]:
    value = 0
    shift = 0
    for index in range(offset, min(len(data), offset + 5)):
        part = data[index]
        value |= (part & 0x7F) << shift
        if not part & 0x80:
            return value, index + 1
        shift += 7
    raise ValueError("incomplete or oversized VarInt")


def encode_string(value: str) -> bytes:
    encoded = value.encode("utf-8")
    return encode_varint(len(encoded)) + encoded


def packet_frame(payload: bytes) -> bytes:
    return encode_varint(len(payload)) + payload


def read_exact(connection: socket.socket, length: int) -> bytes:
    chunks: list[bytes] = []
    remaining = length
    while remaining:
        chunk = connection.recv(remaining)
        if not chunk:
            raise ConnectionError("server closed the socket before the frame completed")
        chunks.append(chunk)
        remaining -= len(chunk)
    return b"".join(chunks)


def read_varint_socket(connection: socket.socket) -> int:
    value = 0
    shift = 0
    for _ in range(5):
        part = read_exact(connection, 1)[0]
        value |= (part & 0x7F) << shift
        if not part & 0x80:
            return value
        shift += 7
    raise ValueError("oversized VarInt from server")


def read_frame(connection: socket.socket) -> bytes:
    length = read_varint_socket(connection)
    return read_exact(connection, length)


def status_probe(args: argparse.Namespace) -> int:
    host = args.host
    port = args.port
    started = now()
    observations: dict[str, Any] = {
        "raw_handshake_accepted": False,
        "status_response": False,
        "pong_response": False,
    }
    status_response: dict[str, Any] | None = None
    error: str | None = None
    try:
        with socket.create_connection((host, port), timeout=args.timeout) as connection:
            connection.settimeout(args.timeout)
            handshake = (
                encode_varint(0x00)
                + encode_varint(args.protocol_version)
                + encode_string(host)
                + struct.pack(">H", port)
                + encode_varint(1)
            )
            connection.sendall(packet_frame(handshake))
            observations["raw_handshake_accepted"] = True
            connection.sendall(packet_frame(encode_varint(0x00)))
            status_frame = read_frame(connection)
            packet_id, offset = decode_varint(status_frame)
            if packet_id != 0:
                raise ValueError(f"unexpected status packet id {packet_id}")
            json_length, offset = decode_varint(status_frame, offset)
            status_payload = status_frame[offset:offset + json_length]
            status_response = json.loads(status_payload.decode("utf-8"))
            observations["status_response"] = True
            timestamp = int(datetime.now(timezone.utc).timestamp() * 1000)
            connection.sendall(packet_frame(encode_varint(0x01) + struct.pack(">q", timestamp)))
            pong_frame = read_frame(connection)
            pong_id, pong_offset = decode_varint(pong_frame)
            observations["pong_response"] = pong_id == 0x01 and len(pong_frame) >= pong_offset + 8
    except (OSError, ValueError, ConnectionError, json.JSONDecodeError) as exc:
        error = f"{type(exc).__name__}: {exc}"

    result = "PASS" if all(observations.values()) else "FAIL"
    record = {
        "schema_version": 1,
        "kind": "status_probe",
        "started_at": started,
        "finished_at": now(),
        "target": {"host": host, "port": port},
        "protocol_version": args.protocol_version,
        "result": result,
        "observations": observations,
        "status_response": status_response,
        "p2p_certification": "NOT_CERTIFIED",
        "limitations": [
            "This raw probe proves the server status path only.",
            "It does not prove that ProtocolLib2PacketEvents intercepted or transformed a packet.",
        ],
    }
    if error:
        record["error"] = error
    output(record, args.output)
    return 0 if result == "PASS" else 1


def plan(args: argparse.Namespace) -> int:
    matrix = validate_matrix(args.matrix)
    output({
        "matrix_id": matrix["matrix_id"],
        "environment_id": matrix["environment_id"],
        "version_pins": matrix["version_pins"],
        "cases": [
            {
                "id": case["id"],
                "protocol_state": case["protocol_state"],
                "driver": case["driver"],
                "result": case["result"],
                "status": case["status"],
                "required_observations": case["required_observations"],
            }
            for case in matrix["cases"]
        ],
    }, args.output)
    return 0


def record_case(args: argparse.Namespace) -> int:
    matrix = validate_matrix(args.matrix)
    cases = {case["id"]: case for case in matrix["cases"]}
    if args.case not in cases:
        raise ValueError(f"unknown case: {args.case}")
    evidence: list[Any] = []
    if args.evidence:
        evidence_value = json.loads(args.evidence.read_text(encoding="utf-8"))
        evidence.append({"path": str(args.evidence), "record": evidence_value})
    if args.result == "PASS" and not evidence:
        raise ValueError("PASS requires --evidence")
    result = {
        "schema_version": 1,
        "matrix_id": matrix["matrix_id"],
        "environment_id": matrix["environment_id"],
        "case_id": args.case,
        "protocol_state": cases[args.case]["protocol_state"],
        "version_pins": matrix["version_pins"],
        "result": args.result,
        "status": "NOT_CERTIFIED",
        "required_observations": cases[args.case]["required_observations"],
        "evidence": evidence,
        "notes": args.notes or "",
        "recorded_at": now(),
    }
    output(result, args.output)
    return 0


def make_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)

    validate = sub.add_parser("validate")
    validate.add_argument("--matrix", type=Path, default=DEFAULT_MATRIX)

    plan_parser = sub.add_parser("plan")
    plan_parser.add_argument("--matrix", type=Path, default=DEFAULT_MATRIX)
    plan_parser.add_argument("--output", type=Path)

    probe = sub.add_parser("status-probe")
    probe.add_argument("--host", required=True)
    probe.add_argument("--port", type=int, required=True)
    probe.add_argument("--protocol-version", type=int, required=True)
    probe.add_argument("--timeout", type=float, default=5.0)
    probe.add_argument("--output", type=Path)

    record = sub.add_parser("record")
    record.add_argument("--matrix", type=Path, default=DEFAULT_MATRIX)
    record.add_argument("--case", required=True)
    record.add_argument("--result", choices=sorted(RESULT_VALUES), required=True)
    record.add_argument("--evidence", type=Path)
    record.add_argument("--notes")
    record.add_argument("--output", type=Path)
    return parser


def main() -> int:
    args = make_parser().parse_args()
    try:
        if args.command == "validate":
            matrix = validate_matrix(args.matrix)
            output({"valid": True, "matrix_id": matrix["matrix_id"], "case_count": len(matrix["cases"])}, None)
            return 0
        if args.command == "plan":
            return plan(args)
        if args.command == "status-probe":
            return status_probe(args)
        if args.command == "record":
            return record_case(args)
        raise ValueError(f"unsupported command: {args.command}")
    except (OSError, ValueError, RuntimeError, json.JSONDecodeError) as exc:
        print(str(exc), file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
