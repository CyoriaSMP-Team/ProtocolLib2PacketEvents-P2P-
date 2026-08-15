/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 *
 * Copyright (C) 2026 CyoriaSMP Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Build-time source generator for {@code com.comphenix.protocol.PacketType}.
 * <p>
 * Real ProtocolLib exposes packet types as compile-time constants
 * ({@code PacketType.Play.Server.ENTITY_ANIMATION}), and plugins compiled against
 * ProtocolLib reference those constants directly, so a compatibility layer that only
 * offered runtime name lookups would not actually link against existing plugin jars.
 * Java cannot add static fields to a class reflectively, so the fields have to be real -
 * which means generating them.
 * <p>
 * This generator reflects over the {@code PacketType} class of whichever PacketEvents
 * release is on the build classpath, enumerating its per-phase {@code Client}/{@code Server}
 * enums, and substitutes one field per constant into {@code PacketType.java.template}.
 * Bumping the PacketEvents dependency therefore refreshes the constant set with no manual
 * edits, which is the whole point of layering on PacketEvents in the first place.
 * <p>
 * Run as a single-file source program (Java 11+):
 * {@code java -cp <packetevents> P2PPacketTypeGenerator.java <outputDir> <templateFile>}
 */
public final class P2PPacketTypeGenerator {

    private static final String PE_PACKET_TYPE =
            "com.github.retrooper.packetevents.protocol.packettype.PacketType";
    private static final String PLACEHOLDER = "//__GENERATED_PHASES__";

    /** ProtocolLib's own protocol-phase class names, keyed by our Protocol enum constant. */
    private static final Map<String, String> PROTOCOL_CLASS_NAMES = new LinkedHashMap<>();
    /** Extra alias classes emitted alongside the primary name, for source compatibility. */
    private static final Map<String, String> PROTOCOL_ALIASES = new LinkedHashMap<>();

    static {
        // ProtocolLib calls the handshake phase "Handshake"; PacketEvents calls it "Handshaking".
        // Emit ProtocolLib's name as primary and keep PacketEvents' name as an alias so code
        // written against either spelling compiles.
        PROTOCOL_CLASS_NAMES.put("HANDSHAKING", "Handshake");
        PROTOCOL_CLASS_NAMES.put("STATUS", "Status");
        PROTOCOL_CLASS_NAMES.put("LOGIN", "Login");
        PROTOCOL_CLASS_NAMES.put("PLAY", "Play");
        PROTOCOL_CLASS_NAMES.put("CONFIGURATION", "Configuration");
        PROTOCOL_ALIASES.put("HANDSHAKING", "Handshaking");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "usage: P2PPacketTypeGenerator <outputDir> <templateFile> <aliasFile>");
        }
        Path outputDir = Paths.get(args[0]);
        Path template = Paths.get(args[1]);
        Path aliasFile = Paths.get(args[2]);

        Map<String, Map<String, List<String>>> constants = collectConstants();
        ALIASES.putAll(loadAliases(aliasFile, constants));

        int total = 0;
        for (Map<String, List<String>> bySender : constants.values()) {
            for (List<String> names : bySender.values()) {
                total += names.size();
            }
        }
        if (total == 0) {
            throw new IllegalStateException(
                    "Reflected zero packet constants from " + PE_PACKET_TYPE
                            + " - refusing to generate an empty PacketType. Is packetevents on the generator classpath?");
        }

        String source = Files.readString(template, StandardCharsets.UTF_8)
                .replace(PLACEHOLDER, renderPhases(constants) + renderInitHolders());

        Path target = outputDir.resolve("com/comphenix/protocol");
        Files.createDirectories(target);
        Files.writeString(target.resolve("PacketType.java"), source, StandardCharsets.UTF_8);

        int aliasCount = 0;
        for (Map<String, String> bucket : ALIASES.values()) {
            aliasCount += bucket.size();
        }
        System.out.println("[p2p-codegen] generated " + total + " PacketEvents constants + "
                + aliasCount + " ProtocolLib-named aliases across "
                + constants.size() + " protocol phases -> " + target.resolve("PacketType.java"));
        for (Map.Entry<String, Map<String, List<String>>> phase : constants.entrySet()) {
            for (Map.Entry<String, List<String>> side : phase.getValue().entrySet()) {
                System.out.println("[p2p-codegen]   " + phase.getKey() + "." + side.getKey()
                        + " = " + side.getValue().size());
            }
        }
    }

    /** (protocol, sender) -> ProtocolLib name -> PacketEvents name. */
    private static final Map<String, Map<String, String>> ALIASES = new LinkedHashMap<>();

    /**
     * Loads the curated ProtocolLib-name table and validates every row against the packet
     * constants actually present in PacketEvents.
     * <p>
     * Validation is strict on purpose. A stale alias would otherwise resolve to nothing (or,
     * worse, silently keep pointing at a renamed packet), and a compatibility layer that maps
     * a plugin's listener onto the wrong packet corrupts behaviour without any error. Failing
     * the build turns that into something a maintainer sees immediately after a PacketEvents
     * upgrade.
     */
    private static Map<String, Map<String, String>> loadAliases(
            Path aliasFile, Map<String, Map<String, List<String>>> constants) throws IOException {
        Map<String, Map<String, String>> out = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();
        int lineNo = 0;

        for (String raw : Files.readAllLines(aliasFile, StandardCharsets.UTF_8)) {
            lineNo++;
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\t+");
            if (parts.length != 4) {
                problems.add("line " + lineNo + ": expected 4 tab-separated columns, got " + parts.length);
                continue;
            }
            String protocol = parts[0].trim();
            String sender = parts[1].trim();
            String plName = parts[2].trim();
            String peName = parts[3].trim();

            List<String> known = constants.getOrDefault(protocol, new LinkedHashMap<>()).get(sender);
            if (known == null) {
                problems.add("line " + lineNo + ": no such protocol/sender in PacketEvents: "
                        + protocol + "." + sender);
                continue;
            }
            if (!known.contains(peName)) {
                problems.add("line " + lineNo + ": PacketEvents has no " + protocol + "." + sender
                        + " constant named '" + peName + "' (mapped from ProtocolLib's '" + plName + "')");
                continue;
            }
            if (known.contains(plName)) {
                // The names already agree, so the automatic matcher covers it; an alias row
                // would emit a duplicate field and fail to compile.
                problems.add("line " + lineNo + ": redundant alias - PacketEvents already defines '"
                        + plName + "' in " + protocol + "." + sender);
                continue;
            }
            String key = protocol + "." + sender;
            String previous = out.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(plName, peName);
            if (previous != null) {
                problems.add("line " + lineNo + ": duplicate alias for " + key + "." + plName);
            }
        }

        if (!problems.isEmpty()) {
            throw new IllegalStateException("Invalid ProtocolLib alias table (" + aliasFile + "):\n  "
                    + String.join("\n  ", problems));
        }
        return out;
    }

    /** protocol constant name -> sender constant name -> ordered packet constant names. */
    private static Map<String, Map<String, List<String>>> collectConstants() throws ClassNotFoundException {
        Map<String, Map<String, List<String>>> out = new LinkedHashMap<>();
        Class<?> root = Class.forName(PE_PACKET_TYPE);

        for (Class<?> phaseClass : root.getDeclaredClasses()) {
            String protocol = matchProtocol(phaseClass.getSimpleName());
            if (protocol == null) {
                continue;
            }
            for (Class<?> sideClass : phaseClass.getDeclaredClasses()) {
                String sender = matchSender(sideClass.getSimpleName());
                if (sender == null || !sideClass.isEnum()) {
                    continue;
                }
                // LinkedHashSet: preserve PacketEvents' declaration order, drop any duplicates.
                Set<String> names = new LinkedHashSet<>();
                for (Object constant : sideClass.getEnumConstants()) {
                    names.add(((Enum<?>) constant).name());
                }
                out.computeIfAbsent(protocol, p -> new LinkedHashMap<>())
                        .computeIfAbsent(sender, s -> new ArrayList<>())
                        .addAll(names);
            }
        }
        return out;
    }

    private static String renderPhases(Map<String, Map<String, List<String>>> constants) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : PROTOCOL_CLASS_NAMES.entrySet()) {
            String protocol = entry.getKey();
            Map<String, List<String>> bySender = constants.getOrDefault(protocol, new LinkedHashMap<>());
            sb.append(renderPhase(entry.getValue(), protocol, bySender, false));
            String alias = PROTOCOL_ALIASES.get(protocol);
            if (alias != null) {
                sb.append(renderPhase(alias, protocol, bySender, true));
            }
        }
        return sb.toString();
    }

    /** Emits the body of {@code initHolders()}, touching every generated holder class. */
    private static String renderInitHolders() {
        StringBuilder sb = new StringBuilder();
        sb.append("    private static void initHolders() {\n");
        for (Map.Entry<String, String> entry : PROTOCOL_CLASS_NAMES.entrySet()) {
            for (String side : new String[]{"Client", "Server"}) {
                sb.append("        touch(").append(entry.getValue()).append(".").append(side).append(".class);\n");
            }
        }
        sb.append("    }\n");
        return sb.toString();
    }

    private static String renderPhase(String className, String protocol,
                                      Map<String, List<String>> bySender, boolean alias) {
        StringBuilder sb = new StringBuilder();
        sb.append("    /** ").append(protocol).append("-phase packet types");
        if (alias) {
            sb.append(" (alias of the PacketEvents spelling)");
        }
        sb.append(". */\n");
        sb.append("    public static class ").append(className).append(" {\n\n");
        sb.append("        public ").append(className).append("() {\n        }\n\n");
        sb.append("        public static Protocol getProtocol() { return Protocol.").append(protocol).append("; }\n\n");
        sb.append(renderSide("Client", protocol, "CLIENT", bySender.get("CLIENT")));
        sb.append("\n");
        sb.append(renderSide("Server", protocol, "SERVER", bySender.get("SERVER")));
        sb.append("    }\n\n");
        return sb.toString();
    }

    private static String renderSide(String className, String protocol, String sender, List<String> names) {
        StringBuilder sb = new StringBuilder();
        sb.append("        public static class ").append(className).append(" extends PacketTypeEnum {\n\n");
        sb.append("            private static final Sender SENDER = Sender.").append(sender).append(";\n\n");
        if (names != null) {
            for (String name : names) {
                sb.append("            public static final PacketType ").append(name)
                        .append(" = reg(Protocol.").append(protocol)
                        .append(", Sender.").append(sender)
                        .append(", \"").append(name).append("\");\n");
            }
            sb.append("\n");
        }

        // Constructing the PacketTypeEnum after the fields have been initialized is
        // important: its reflection-based registerAll() must see real PacketType values.
        sb.append("            private static final ").append(className).append(" INSTANCE = new ").append(className).append("();\n");
        sb.append("            private ").append(className).append("() {\n            }\n\n");
        sb.append("            public static Sender getSender() { return SENDER; }\n");
        sb.append("            public static ").append(className).append(" getInstance() { return INSTANCE; }\n\n");

        // ProtocolLib spells a number of packets differently to PacketEvents. Emit those names
        // too, pointing at the same packet type, so plugins compiled against ProtocolLib link
        // against the identifiers they actually reference.
        Map<String, String> aliases = ALIASES.get(protocol + "." + sender);
        if (aliases != null && !aliases.isEmpty()) {
            sb.append("            // ProtocolLib-compatible names for packets PacketEvents spells differently.\n");
            for (Map.Entry<String, String> alias : aliases.entrySet()) {
                sb.append("            public static final PacketType ").append(alias.getKey())
                        .append(" = regAlias(Protocol.").append(protocol)
                        .append(", Sender.").append(sender)
                        .append(", \"").append(alias.getKey())
                        .append("\", \"").append(alias.getValue()).append("\");\n");
            }
            sb.append("\n");
        }
        sb.append("            /** Name-based lookup, for code that resolves packet types dynamically. */\n");
        sb.append("            public static PacketType get(String name) {\n");
        sb.append("                return getOrThrow(Protocol.").append(protocol)
                .append(", Sender.").append(sender).append(", name);\n");
        sb.append("            }\n\n");
        sb.append("        }\n");
        return sb.toString();
    }

    private static String matchProtocol(String simpleName) {
        for (String protocol : PROTOCOL_CLASS_NAMES.keySet()) {
            if (protocol.equalsIgnoreCase(simpleName)) {
                return protocol;
            }
        }
        return null;
    }

    private static String matchSender(String simpleName) {
        if ("Client".equals(simpleName)) {
            return "CLIENT";
        }
        if ("Server".equals(simpleName)) {
            return "SERVER";
        }
        return null;
    }

    private P2PPacketTypeGenerator() {
    }
}
