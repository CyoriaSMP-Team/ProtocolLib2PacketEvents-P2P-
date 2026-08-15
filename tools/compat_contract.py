#!/usr/bin/env python3
"""Build and verify the ProtocolLib2PacketEvents compatibility contract.

The checker deliberately consumes the upstream source tree as a *contract* only:
it records class names and, when an upstream jar is supplied, public bytecode
signatures. It never copies upstream implementation code into P2P.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
from pathlib import Path


CLASS_RE = re.compile(
    r"(?P<mods>(?:(?:public|protected|private|abstract|final|static|sealed|non-sealed|strictfp)\s+)*)"
    r"(?P<kind>class|interface|enum|record)\s+(?P<name>[A-Za-z_$][\w$]*)"
)
PACKAGE_RE = re.compile(r"\bpackage\s+([\w.]+)\s*;")


def mask_java(text: str) -> str:
    """Blank comments and literals while preserving positions and braces."""

    out = list(text)
    i = 0
    n = len(text)
    state = "code"
    while i < n:
        c = text[i]
        nxt = text[i + 1] if i + 1 < n else ""
        if state == "code":
            if c == "/" and nxt == "/":
                out[i] = out[i + 1] = " "
                i += 2
                state = "line"
                continue
            if c == "/" and nxt == "*":
                out[i] = out[i + 1] = " "
                i += 2
                state = "block"
                continue
            if c == '"':
                out[i] = " "
                i += 1
                state = "string"
                continue
            if c == "'":
                out[i] = " "
                i += 1
                state = "char"
                continue
            i += 1
            continue
        if state == "line":
            if c == "\n":
                state = "code"
            else:
                out[i] = " "
            i += 1
            continue
        if state == "block":
            if c == "*" and nxt == "/":
                out[i] = out[i + 1] = " "
                i += 2
                state = "code"
            else:
                if c != "\n":
                    out[i] = " "
                i += 1
            continue
        if state in ("string", "char"):
            if c == "\\":
                out[i] = " "
                if i + 1 < n and text[i + 1] != "\n":
                    out[i + 1] = " "
                    i += 2
                else:
                    i += 1
                continue
            if (state == "string" and c == '"') or (state == "char" and c == "'"):
                out[i] = " "
                i += 1
                state = "code"
            else:
                if c != "\n":
                    out[i] = " "
                i += 1
    return "".join(out)


def brace_depths(masked: str) -> list[int]:
    depths = [0] * (len(masked) + 1)
    depth = 0
    for i, c in enumerate(masked):
        depths[i] = depth
        if c == "{":
            depth += 1
        elif c == "}":
            depth = max(0, depth - 1)
    depths[len(masked)] = depth
    return depths


def source_classes(path: Path) -> set[str]:
    text = path.read_text(encoding="utf-8", errors="replace")
    masked = mask_java(text)
    package_match = PACKAGE_RE.search(masked)
    package = package_match.group(1) if package_match else ""
    depths = brace_depths(masked)
    declarations = []
    for match in CLASS_RE.finditer(masked):
        opening = masked.find("{", match.end())
        if opening < 0:
            continue
        declarations.append((match.start(), opening, match.group("name"), depths[match.start()]))

    result: set[str] = set()
    for position, _opening, name, depth in declarations:
        # A source file can contain several sibling classes at the same lexical
        # depth.  The old implementation treated every earlier sibling as a
        # parent, producing nonsense names such as PacketType$Handshake$Play.
        # Pick the most recent declaration at each enclosing depth instead.
        latest_by_depth: dict[int, str] = {}
        for parent_position, _parent_opening, parent_name, parent_depth in declarations:
            if parent_position < position and parent_depth < depth:
                latest_by_depth[parent_depth] = parent_name
        parents = [latest_by_depth[level] for level in sorted(latest_by_depth)]
        binary = "$".join(parents + [name])
        result.add(".".join(x for x in (package, binary) if x))
    return result


def source_inventory(upstream: Path) -> set[str]:
    roots = [upstream / "src/main/java", upstream / "paper/src/main/java", upstream / "spigot/src/main/java"]
    classes: set[str] = set()
    for root in roots:
        if root.exists():
            for java_file in root.rglob("*.java"):
                classes.update(source_classes(java_file))
    return classes


def jar_inventory(jar: Path, package_prefix: str | None = None) -> set[str]:
    output = subprocess.check_output(["jar", "tf", str(jar)], text=True)
    classes = {
        line[:-6].replace("/", ".")
        for line in output.splitlines()
        if line.endswith(".class") and not line.endswith("module-info.class")
    }
    if package_prefix:
        classes = {name for name in classes if name.startswith(package_prefix)}
    return classes


def git_sha(path: Path) -> str | None:
    try:
        return subprocess.check_output(["git", "-C", str(path), "rev-parse", "HEAD"], text=True).strip()
    except (OSError, subprocess.CalledProcessError):
        return None


def javap_public(jar: Path, class_name: str, dependency_classpath: str | None = None) -> list[str]:
    classpath = str(jar)
    if dependency_classpath:
        classpath += os.pathsep + dependency_classpath
    try:
        output = subprocess.check_output(
            ["javap", "-classpath", classpath, "-public", "-s", class_name],
            text=True,
            stderr=subprocess.DEVNULL,
        )
    except subprocess.CalledProcessError:
        return []
    lines = []
    for line in output.splitlines():
        line = line.strip()
        if line.startswith("public ") or line.startswith("protected ") or line.startswith("descriptor:"):
            lines.append(line)
    return lines


def _member_name(declaration: str) -> str:
    """Extract the JVM member name while ignoring source generic spelling.

    Generic variable names and generic parameter bounds are not part of a JVM
    descriptor.  The previous checker treated ``T`` versus ``TValue`` (and
    ``List<?>`` versus ``List<WrappedWatchableObject>``) as an ABI failure even
    when the emitted bytecode was identical.  The descriptor remains the
    authoritative type contract; this helper only supplies the member name.
    """
    declaration = re.sub(r"\s+throws\s+[^;]+;?$", "", declaration).strip()
    prefix = declaration.split("(", 1)[0] if "(" in declaration else declaration.rstrip(";")
    return prefix.split()[-1] if prefix.split() else declaration


def parse_public_abi(lines: list[str]) -> tuple[str, set[tuple[str, str]]]:
    """Return a class declaration and order-independent public ABI entries.

    ``javap`` prints members in source/class-file order, which is not part of
    the JVM ABI.  The old checker compared the raw list and consequently
    reported a mismatch whenever two equivalent classes declared methods in a
    different order.  Pairing each declaration with its descriptor lets the
    contract distinguish missing members from harmless ordering differences.
    """
    header = next((line for line in lines if re.search(r"\b(class|interface|enum|record)\b", line)), "")
    entries: set[tuple[str, str]] = set()
    pending: list[str] = []
    for line in lines:
        if line.startswith("descriptor:"):
            if pending:
                declaration = re.sub(r"\b(?:abstract|default|final|volatile|transient|synchronized|native|strictfp)\b", "", pending[-1])
                declaration = re.sub(r"\s+", " ", declaration).strip()
                entries.add((_member_name(declaration), line))
            pending = []
        elif line != header:
            pending.append(line)
    return header, entries


def declared_supertypes(header: str) -> list[str]:
    """Extract resolvable superclass/interface names from a javap header."""

    if not header:
        return []
    # Generic arguments are not part of binary names.  Strip balanced generic
    # sections so nested declarations such as Map<Player, List<T>> do not eat
    # the following extends/implements clauses.
    clean_chars: list[str] = []
    generic_depth = 0
    for char in header:
        if char == "<":
            generic_depth += 1
        elif char == ">" and generic_depth:
            generic_depth -= 1
        elif generic_depth == 0:
            clean_chars.append(char)
    clean = "".join(clean_chars)
    match = re.search(r"\b(?:class|interface|enum|record)\s+([\w.$]+)", clean)
    if not match:
        return []
    tail = clean[match.end():].split("{", 1)[0]
    names: list[str] = []
    clauses = re.findall(r"\b(?:extends|implements)\s+(.+?)(?=\b(?:extends|implements)\b|$)", tail)
    for clause in clauses:
        names.extend(part.strip() for part in clause.split(",") if part.strip())
    return [name for name in names if name not in {"java.lang.Object"}]


def effective_public_abi(
    jar: Path,
    class_name: str,
    dependency_classpath: str | None,
    memo: dict[str, set[tuple[str, str]]],
    active: set[str] | None = None,
) -> set[tuple[str, str]]:
    """Return the public ABI visible through a class, including inherited members.

    ``javap`` prints only members declared by the requested class.  That is not
    enough for a compatibility check when a clean-room implementation moves a
    Netty/Bukkit implementation into a shared adapter superclass.  Recursing
    through the resolved superclass/interfaces makes ZeroBuffer-style adapters
    verify their effective binary surface rather than their source layout.
    """

    if class_name in memo:
        return memo[class_name]
    active = set() if active is None else active
    if class_name in active:
        return set()
    active.add(class_name)
    lines = javap_public(jar, class_name, dependency_classpath)
    header, declared = parse_public_abi(lines)
    result = set(declared)
    for parent in declared_supertypes(header):
        result.update(effective_public_abi(jar, parent, dependency_classpath, memo, active))
    active.remove(class_name)
    memo[class_name] = result
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("command", choices=("manifest", "check"))
    parser.add_argument("--upstream", type=Path, required=True)
    parser.add_argument("--p2p-root", type=Path, default=Path.cwd())
    parser.add_argument("--p2p-jar", type=Path, required=True)
    parser.add_argument("--upstream-jar", type=Path)
    parser.add_argument(
        "--dependency-classpath",
        help="Additional jars used to resolve inherited public members during javap ABI scans",
    )
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()

    upstream_classes = source_inventory(args.upstream)
    # The P2P jar may bundle implementation dependencies (currently Byte Buddy).
    # Count only the compatibility namespace so the manifest does not present
    # third-party implementation classes as ProtocolLib API classes.
    p2p_classes = jar_inventory(args.p2p_jar, "com.comphenix.protocol.")
    missing_classes = sorted(upstream_classes - p2p_classes)

    contract = {
        "upstream_sha": git_sha(args.upstream),
        "p2p_sha": git_sha(args.p2p_root),
        "upstream_class_count": len(upstream_classes),
        "p2p_class_count": len(p2p_classes),
        "missing_classes": missing_classes,
    }

    if args.upstream_jar:
        missing_members = {}
        extra_members = {}
        descriptor_mismatches = {}
        class_headers = {}
        actual_abi_memo: dict[str, set[tuple[str, str]]] = {}
        for class_name in sorted(upstream_classes):
            expected_header, expected = parse_public_abi(
                javap_public(args.upstream_jar, class_name, args.dependency_classpath)
            )
            actual_lines = javap_public(args.p2p_jar, class_name, args.dependency_classpath)
            actual_header, _actual_declared = parse_public_abi(actual_lines)
            actual = effective_public_abi(
                args.p2p_jar, class_name, args.dependency_classpath, actual_abi_memo
            )
            missing = sorted(expected - actual)
            extra = sorted(actual - expected)
            if missing:
                missing_members[class_name] = missing
            if extra:
                extra_members[class_name] = extra
            if expected_header != actual_header:
                class_headers[class_name] = {"upstream": expected_header, "p2p": actual_header}

            # If a member name is present on both sides but javap reports a
            # different binary descriptor, preserve it as a dedicated failure.
            # Multiple overloads are compared as descriptor sets.
            expected_by_decl = {}
            actual_by_decl = {}
            for declaration, descriptor in expected:
                expected_by_decl.setdefault(declaration, set()).add(descriptor)
            for declaration, descriptor in actual:
                actual_by_decl.setdefault(declaration, set()).add(descriptor)
            # An implementation may deliberately expose a superset of the
            # upstream overloads (for example a PacketEvents-native overload)
            # without changing the binary contract.  A descriptor mismatch is
            # only real when an expected descriptor is replaced or absent
            # while the member name is still present.  Truly absent members
            # are reported in ``missing_members`` above.
            mismatched = {
                declaration: {"upstream": sorted(expected_by_decl[declaration]), "p2p": sorted(actual_by_decl[declaration])}
                for declaration in sorted(expected_by_decl.keys() & actual_by_decl.keys())
                if not expected_by_decl[declaration].issubset(actual_by_decl[declaration])
            }
            if mismatched:
                descriptor_mismatches[class_name] = mismatched
        contract["missing_members"] = missing_members
        contract["extra_members"] = extra_members
        contract["descriptor_mismatches"] = descriptor_mismatches
        contract["class_header_mismatches"] = class_headers
    else:
        contract["missing_members"] = "not_checked_without_upstream_jar"
        contract["extra_members"] = "not_checked_without_upstream_jar"
        contract["descriptor_mismatches"] = "not_checked_without_upstream_jar"
        contract["class_header_mismatches"] = "not_checked_without_upstream_jar"

    serialized = json.dumps(contract, indent=2, sort_keys=True) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(serialized, encoding="utf-8")
    else:
        print(serialized, end="")

    if args.command == "check" and (
        missing_classes
        or isinstance(contract["missing_members"], dict) and contract["missing_members"]
        or isinstance(contract["descriptor_mismatches"], dict) and contract["descriptor_mismatches"]
        or isinstance(contract["class_header_mismatches"], dict) and contract["class_header_mismatches"]
    ):
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
