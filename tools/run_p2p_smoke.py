#!/usr/bin/env python3
"""Build and validate ProtocolLib2PacketEvents evidence artifacts.

The runner is intentionally evidence-first: it never upgrades a result based
on a compile alone, and it never marks FULLY_TESTED without explicit lifecycle
and reconnect/restart checks in the same pinned environment.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MANIFEST = ROOT / "compatibility" / "plugins.yml"
RESULT_VALUES = {"PASS", "FAIL", "BLOCKED", "NOT_RUN"}
LEVELS = ["BOOT", "CORE", "PACKET_BEHAVIOR", "FULLY_TESTED"]
NOT_CERTIFIED = "NOT_CERTIFIED"
ERROR_PATTERNS = (
    "NoSuchMethodError",
    "NoClassDefFoundError",
    "ClassNotFoundException",
    "LinkageError",
    "P2P linkage error",
)


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")


def load_yaml(path: Path) -> dict[str, Any]:
    try:
        import yaml  # type: ignore
    except ImportError as exc:
        raise RuntimeError("PyYAML is required to validate the evidence manifest") from exc
    with path.open("r", encoding="utf-8") as handle:
        value = yaml.safe_load(handle)
    if not isinstance(value, dict):
        raise ValueError(f"{path} must contain a YAML object")
    return value


def dump_json(value: Any) -> str:
    return json.dumps(value, ensure_ascii=False, indent=2, sort_keys=True) + "\n"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def environment_map(manifest: dict[str, Any]) -> dict[str, dict[str, Any]]:
    environments = manifest.get("environments")
    if not isinstance(environments, list):
        raise ValueError("manifest.environments must be a list")
    result: dict[str, dict[str, Any]] = {}
    for environment in environments:
        if not isinstance(environment, dict) or not isinstance(environment.get("id"), str):
            raise ValueError("every environment must have a string id")
        environment_id = environment["id"]
        if environment_id in result:
            raise ValueError(f"duplicate environment id: {environment_id}")
        result[environment_id] = environment
    return result


def validate_manifest(path: Path) -> dict[str, Any]:
    manifest = load_yaml(path)
    errors: list[str] = []
    if manifest.get("schema_version") != 1:
        errors.append("schema_version must be 1")

    status_levels = manifest.get("status_levels")
    if not isinstance(status_levels, dict):
        errors.append("status_levels must be an object")
    else:
        for level in LEVELS:
            definition = status_levels.get(level)
            if not isinstance(definition, dict) or not definition.get("definition"):
                errors.append(f"status level {level} has no definition")
            if not isinstance(definition, dict) or not isinstance(definition.get("required_checks"), list):
                errors.append(f"status level {level} has no required_checks list")

    contract = manifest.get("evidence_contract")
    if not isinstance(contract, dict):
        errors.append("evidence_contract must be an object")
    else:
        if not set(RESULT_VALUES).issubset(set(contract.get("result_values", []))):
            errors.append("evidence_contract.result_values is missing an allowed result")
        required_fields = set(contract.get("required_fields", []))
        required_contract_fields = {
            "schema_version", "run_id", "started_at", "environment", "plugin",
            "result", "status_level", "checks", "artifact_sha256", "log_sha256",
        }
        missing = required_contract_fields - required_fields
        if missing:
            errors.append("evidence_contract.required_fields missing: " + ", ".join(sorted(missing)))

    try:
        environments = environment_map(manifest)
    except ValueError as exc:
        errors.append(str(exc))
        environments = {}

    for environment_id, environment in environments.items():
        for field in ("server", "server_version", "server_build", "java", "packetevents", "p2p", "scope"):
            if environment.get(field) in (None, ""):
                errors.append(f"environment {environment_id} is missing pinned {field}")
        if environment.get("certification") == "FULLY_TESTED":
            errors.append(f"environment {environment_id} cannot be FULLY_TESTED in the manifest without evidence")

    plugins = manifest.get("plugins")
    if not isinstance(plugins, list) or not plugins:
        errors.append("plugins must be a non-empty list")
    else:
        plugin_ids: set[str] = set()
        allowed_statuses = set(LEVELS) | {NOT_CERTIFIED, "NOT_TESTED"}
        for plugin in plugins:
            if not isinstance(plugin, dict):
                errors.append("each plugin entry must be an object")
                continue
            plugin_id = plugin.get("id")
            if not isinstance(plugin_id, str) or not plugin_id:
                errors.append("each plugin must have a string id")
                continue
            if plugin_id in plugin_ids:
                errors.append(f"duplicate plugin id: {plugin_id}")
            plugin_ids.add(plugin_id)
            environment_id = plugin.get("environment_id")
            if environment_id not in environments:
                errors.append(f"plugin {plugin_id} references unknown environment {environment_id}")
            status = plugin.get("status")
            if status not in allowed_statuses:
                errors.append(f"plugin {plugin_id} has invalid status {status}")
            if status == "FULLY_TESTED":
                evidence = plugin.get("evidence")
                if not isinstance(evidence, list) or not evidence:
                    errors.append(f"plugin {plugin_id} is FULLY_TESTED without evidence")
            if status in LEVELS and plugin.get("version") in (None, ""):
                errors.append(f"plugin {plugin_id} is certified without a pinned version")

    if errors:
        raise ValueError("manifest validation failed:\n- " + "\n- ".join(errors))
    return manifest


def print_or_write(value: Any, output: Path | None) -> None:
    rendered = dump_json(value)
    if output is None:
        sys.stdout.write(rendered)
    else:
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(rendered, encoding="utf-8")
        print(output)


def run_command(command: list[str], cwd: Path) -> subprocess.CompletedProcess[str]:
    print("$ " + " ".join(command))
    process = subprocess.run(command, cwd=cwd, text=True, capture_output=True)
    if process.stdout:
        print(process.stdout, end="")
    if process.returncode != 0 and process.stderr:
        print(process.stderr, file=sys.stderr, end="")
    return process


def build_smoke(args: argparse.Namespace) -> int:
    root_build = run_command(["mvn", "-q", "-DskipTests", "install"], ROOT)
    if root_build.returncode != 0:
        return root_build.returncode
    smoke_pom = ROOT / "evidence" / "smoke-plugin" / "pom.xml"
    smoke_build = run_command(["mvn", "-q", "-f", str(smoke_pom), "package"], ROOT)
    if smoke_build.returncode != 0:
        return smoke_build.returncode
    jar = ROOT / "evidence" / "smoke-plugin" / "target" / "P2PSmokeTest-1.0.0.jar"
    if not jar.is_file():
        print(f"smoke jar was not produced: {jar}", file=sys.stderr)
        return 2
    result = {
        "schema_version": 1,
        "built_at": utc_now(),
        "artifact": str(jar),
        "artifact_sha256": sha256(jar),
    }
    print_or_write(result, args.output)
    return 0


def parse_evidence_lines(log_path: Path) -> tuple[list[dict[str, Any]], list[str]]:
    events: list[dict[str, Any]] = []
    errors: list[str] = []
    marker = "P2P_EVIDENCE_JSON "
    for raw_line in log_path.read_text(encoding="utf-8", errors="replace").splitlines():
        if any(pattern in raw_line for pattern in ERROR_PATTERNS):
            errors.append(raw_line.strip())
        position = raw_line.find(marker)
        if position < 0:
            continue
        payload = raw_line[position + len(marker):].strip()
        try:
            event = json.loads(payload)
        except json.JSONDecodeError:
            errors.append("invalid P2P_EVIDENCE_JSON line: " + raw_line.strip())
            continue
        if isinstance(event, dict):
            events.append(event)
    return events, errors


def load_report(path: Path | None) -> dict[str, Any]:
    if path is None:
        return {}
    value = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(value, dict):
        raise ValueError(f"report {path} must be a JSON object")
    return value


def bool_check(report: dict[str, Any], checks: dict[str, bool], name: str, *aliases: str) -> None:
    report_checks = report.get("checks")
    if isinstance(report_checks, dict):
        candidates = (name,) + aliases
        if any(report_checks.get(candidate) is True for candidate in candidates):
            checks[name] = True


def calculate_level(checks: dict[str, bool]) -> str:
    boot = all(checks.get(name, False) for name in ("enable_clean", "disable_clean", "reload_clean", "no_linkage_errors"))
    if not boot:
        return NOT_CERTIFIED
    core = boot and checks.get("core_api", False)
    if not core:
        return "BOOT"
    packet = all(checks.get(name, False) for name in (
        "send", "receive", "cancel", "modify", "async", "ordering"))
    if not packet:
        return "CORE"
    fully = all(checks.get(name, False) for name in (
        "reconnect", "restart", "no_known_regression"))
    return "FULLY_TESTED" if fully else "PACKET_BEHAVIOR"


def parse_log(args: argparse.Namespace) -> int:
    manifest = validate_manifest(args.manifest)
    environments = environment_map(manifest)
    if args.environment not in environments:
        raise ValueError(f"unknown environment id: {args.environment}")
    if not args.log.is_file():
        raise FileNotFoundError(args.log)

    events, log_errors = parse_evidence_lines(args.log)
    report = load_report(args.report)
    checks: dict[str, bool] = {name: False for name in (
        "enable_clean", "disable_clean", "reload_clean", "no_linkage_errors",
        "core_api", "send", "receive", "cancel", "modify", "async", "ordering",
        "reconnect", "restart", "no_known_regression",
    )}
    checks["no_linkage_errors"] = not log_errors
    event_results = {(event.get("event"), event.get("result")) for event in events}
    checks["enable_clean"] = ("ENABLE", "PASS") in event_results
    checks["disable_clean"] = ("DISABLE", "PASS") in event_results
    checks["reload_clean"] = ("RELOAD_MARK", "PASS") in event_results
    checks["core_api"] = ("CORE_READY", "PASS") in event_results
    checks["send"] = ("PACKET_SEND", "PASS") in event_results
    checks["modify"] = ("PACKET_MODIFY", "PASS") in event_results
    checks["cancel"] = ("PACKET_CANCEL", "PASS") in event_results
    for name in ("enable_clean", "disable_clean", "reload_clean", "core_api"):
        bool_check(report, checks, name)
    bool_check(report, checks, "send", "send_observed")
    bool_check(report, checks, "receive", "receive_observed")
    bool_check(report, checks, "cancel", "cancel_observed")
    bool_check(report, checks, "modify", "modify_observed")
    bool_check(report, checks, "async", "async_observed", "async_off_main")
    bool_check(report, checks, "ordering")
    for name in ("reconnect", "restart", "no_known_regression"):
        bool_check(report, checks, name)

    status_level = calculate_level(checks)
    positive_observations = any(
        value for name, value in checks.items() if name != "no_linkage_errors"
    )
    if not events and not positive_observations:
        result = "NOT_RUN"
    elif log_errors:
        result = "FAIL"
    else:
        result = "PASS" if any(checks.values()) else "NOT_RUN"

    environment = environments[args.environment]
    plugin = report.get("plugin") if isinstance(report.get("plugin"), dict) else {
        "name": "P2PSmokeTest",
        "version": "1.0.0",
    }
    evidence = {
        "schema_version": 1,
        "run_id": report.get("run_id") or f"parsed-{args.environment}-{args.log.stat().st_mtime_ns}",
        "started_at": report.get("started_at") or utc_now(),
        "stopped_at": report.get("stopped_at"),
        "environment": environment,
        "plugin": plugin,
        "result": result,
        "status_level": status_level,
        "checks": checks,
        "observations": {
            "events": events,
            "log_errors": log_errors,
            "report": report,
        },
        "artifact_sha256": sha256(args.artifact) if args.artifact else report.get("artifact_sha256"),
        "log_sha256": sha256(args.log),
    }
    print_or_write(evidence, args.output)
    return 0 if result != "FAIL" else 1


def validate_report(args: argparse.Namespace) -> int:
    manifest = validate_manifest(args.manifest)
    report = load_report(args.input)
    required = set(manifest["evidence_contract"]["required_fields"])
    missing = sorted(field for field in required if field not in report)
    errors: list[str] = []
    if missing:
        errors.append("missing required fields: " + ", ".join(missing))
    if report.get("result") not in RESULT_VALUES:
        errors.append(f"invalid result: {report.get('result')}")
    if report.get("status_level") not in set(LEVELS) | {NOT_CERTIFIED}:
        errors.append(f"invalid status_level: {report.get('status_level')}")
    if report.get("status_level") == "FULLY_TESTED":
        checks = report.get("checks", {})
        if not all(isinstance(checks, dict) and checks.get(name) is True for name in (
            "reconnect", "restart", "no_known_regression")):
            errors.append("FULLY_TESTED requires reconnect, restart, and no_known_regression=true")
    if errors:
        raise ValueError("report validation failed:\n- " + "\n- ".join(errors))
    print_or_write({"valid": True, "run_id": report.get("run_id"), "status_level": report.get("status_level")}, args.output)
    return 0


def make_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    subparsers = parser.add_subparsers(dest="command", required=True)

    validate = subparsers.add_parser("validate", help="validate compatibility/plugins.yml")
    validate.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)

    build = subparsers.add_parser("build", help="install P2P and build the smoke plugin")
    build.add_argument("--output", type=Path)

    parse = subparsers.add_parser("parse-log", help="parse P2P_EVIDENCE_JSON log markers")
    parse.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parse.add_argument("--environment", required=True)
    parse.add_argument("--log", type=Path, required=True)
    parse.add_argument("--report", type=Path)
    parse.add_argument("--artifact", type=Path)
    parse.add_argument("--output", type=Path)

    report = subparsers.add_parser("validate-report", help="validate a normalized evidence JSON")
    report.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    report.add_argument("--input", type=Path, required=True)
    report.add_argument("--output", type=Path)
    return parser


def main() -> int:
    args = make_parser().parse_args()
    try:
        if args.command == "validate":
            manifest = validate_manifest(args.manifest)
            print_or_write({
                "valid": True,
                "schema_version": manifest["schema_version"],
                "environments": [environment["id"] for environment in manifest["environments"]],
                "plugins": [plugin["id"] for plugin in manifest["plugins"]],
            }, None)
            return 0
        if args.command == "build":
            return build_smoke(args)
        if args.command == "parse-log":
            return parse_log(args)
        if args.command == "validate-report":
            return validate_report(args)
        raise ValueError(f"unsupported command: {args.command}")
    except (OSError, ValueError, RuntimeError, json.JSONDecodeError) as exc:
        print(str(exc), file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
