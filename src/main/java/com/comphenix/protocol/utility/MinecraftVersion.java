/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.utility;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Server;

/** ProtocolLib-shaped Minecraft version value object. */
public final class MinecraftVersion implements Comparable<MinecraftVersion>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern VERSION = Pattern.compile("(?:.*?)(\\d+)(?:[.]([0-9]+))?(?:[.]([0-9]+))?(?:[-.]?(.*))?");

    public static final MinecraftVersion v26_2 = version("26.2");
    public static final MinecraftVersion v26_1 = version("26.1");
    public static final MinecraftVersion v1_21_11 = version("1.21.11");
    public static final MinecraftVersion v1_21_10 = version("1.21.10");
    public static final MinecraftVersion v1_21_9 = version("1.21.9");
    public static final MinecraftVersion v1_21_6 = version("1.21.6");
    public static final MinecraftVersion v1_21_5 = version("1.21.5");
    public static final MinecraftVersion v1_21_4 = version("1.21.4");
    public static final MinecraftVersion v1_21_2 = version("1.21.2");
    public static final MinecraftVersion v1_21_0 = version("1.21");
    public static final MinecraftVersion v1_20_5 = version("1.20.5");
    public static final MinecraftVersion v1_20_4 = version("1.20.4");
    public static final MinecraftVersion CONFIG_PHASE_PROTOCOL_UPDATE = version("1.20.2");
    public static final MinecraftVersion TRAILS_AND_TAILS = version("1.20.1");
    public static final MinecraftVersion FEATURE_PREVIEW_2 = version("1.19.4");
    public static final MinecraftVersion FEATURE_PREVIEW_UPDATE = version("1.19.3");
    public static final MinecraftVersion WILD_UPDATE = version("1.19");
    public static final MinecraftVersion CAVES_CLIFFS_2 = version("1.18");
    public static final MinecraftVersion CAVES_CLIFFS_1 = version("1.17");
    public static final MinecraftVersion NETHER_UPDATE_4 = version("1.16.2");
    public static final MinecraftVersion NETHER_UPDATE_2 = version("1.16.2");
    public static final MinecraftVersion NETHER_UPDATE = version("1.16");
    public static final MinecraftVersion BEE_UPDATE = version("1.15");
    public static final MinecraftVersion VILLAGE_UPDATE = version("1.14");
    public static final MinecraftVersion AQUATIC_UPDATE = version("1.13");
    public static final MinecraftVersion COLOR_UPDATE = version("1.12");
    public static final MinecraftVersion EXPLORATION_UPDATE = version("1.11");
    public static final MinecraftVersion FROSTBURN_UPDATE = version("1.10");
    public static final MinecraftVersion COMBAT_UPDATE = version("1.9");
    public static final MinecraftVersion BOUNTIFUL_UPDATE = version("1.8");
    public static final MinecraftVersion SKIN_UPDATE = version("1.7.9");
    public static final MinecraftVersion WORLD_UPDATE = version("1.7.2");
    public static final MinecraftVersion HORSE_UPDATE = version("1.6.1");
    public static final MinecraftVersion REDSTONE_UPDATE = version("1.5.2");
    public static final MinecraftVersion SCARY_UPDATE = version("1.4.2");
    public static final MinecraftVersion LATEST = v26_2;

    private static volatile MinecraftVersion current;
    private final ServerVersion handle;
    private final int major;
    private final int minor;
    private final int build;
    private final String developmentStage;
    private final String version;

    public MinecraftVersion(Server server) {
        this(server == null ? null : server.getBukkitVersion());
    }

    public MinecraftVersion(ServerVersion handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
        Parsed parsed = parse(handle.getReleaseName());
        this.major = parsed.major;
        this.minor = parsed.minor;
        this.build = parsed.build;
        this.developmentStage = parsed.stage;
        this.version = handle.getReleaseName();
    }

    public MinecraftVersion(String value) {
        Parsed parsed = parse(value);
        this.handle = null;
        this.major = parsed.major;
        this.minor = parsed.minor;
        this.build = parsed.build;
        this.developmentStage = parsed.stage;
        this.version = parsed.display;
    }

    public MinecraftVersion(int major, int minor, int build) {
        this(major, minor, build, "");
    }

    public MinecraftVersion(int major, int minor, int build, String developmentStage) {
        this.handle = null;
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.developmentStage = developmentStage == null ? "" : developmentStage;
        this.version = major + "." + minor + "." + build
                + (this.developmentStage.isEmpty() ? "" : "-" + this.developmentStage);
    }

    private MinecraftVersion(int major, int minor, int build, String stage, String display) {
        this.handle = null;
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.developmentStage = stage == null ? "" : stage;
        this.version = display;
    }

    private static MinecraftVersion version(String value) {
        Parsed parsed = parse(value);
        return new MinecraftVersion(parsed.major, parsed.minor, parsed.build, parsed.stage, parsed.display);
    }

    private static Parsed parse(String input) {
        String value = input == null ? "unknown" : input.trim();
        Matcher matcher = VERSION.matcher(value);
        if (!matcher.matches()) return new Parsed(0, 0, 0, "", value);
        int first = intValue(matcher.group(1));
        int second = intValue(matcher.group(2));
        int third = intValue(matcher.group(3));
        String stage = matcher.group(4) == null ? "" : matcher.group(4).trim();
        return new Parsed(first, second, third, stage, value);
    }

    private static int intValue(String value) {
        if (value == null || value.isEmpty()) return 0;
        try { return Integer.parseInt(value); } catch (NumberFormatException ignored) { return 0; }
    }

    private record Parsed(int major, int minor, int build, String stage, String display) { }

    public static String extractVersion(String serverVersion) {
        if (serverVersion == null) return "";
        Matcher matcher = Pattern.compile("(\\d+\\.\\d+(?:\\.\\d+)?)").matcher(serverVersion);
        return matcher.find() ? matcher.group(1) : serverVersion;
    }

    public static MinecraftVersion fromServerVersion(String serverVersion) {
        return new MinecraftVersion(extractVersion(serverVersion));
    }

    public static MinecraftVersion current() {
        MinecraftVersion value = current;
        if (value != null) return value;
        try {
            value = new MinecraftVersion(com.github.retrooper.packetevents.PacketEvents.getAPI()
                    .getServerManager().getVersion());
        } catch (Throwable ignored) {
            value = LATEST;
        }
        current = value;
        return value;
    }

    public static MinecraftVersion getCurrentVersion() { return current(); }
    public static void setCurrentVersion(MinecraftVersion value) { current = value; }
    public int getMajor() { return major; }
    public int getMinor() { return minor; }
    public int getBuild() { return build; }
    public String getDevelopmentStage() { return developmentStage; }
    public SnapshotVersion getSnapshot() { return developmentStage.isEmpty() ? null : new SnapshotVersion(developmentStage); }
    public boolean isSnapshot() { return !developmentStage.isEmpty(); }
    public boolean atOrAbove() { return current().compareTo(this) >= 0; }
    public String getVersion() { return version; }

    public int getProtocolVersion() {
        if (handle != null) return handle.getProtocolVersion();
        if (major == 26) return minor == 1 ? 773 : 774;
        if (major == 1 && minor == 21) return 767 + Math.max(0, build);
        if (major == 1 && minor == 20) return 765 + Math.max(0, build - 4);
        return major * 10000 + minor * 100 + build;
    }

    public ServerVersion toPacketEvents() { return handle; }
    public boolean isAtLeast(MinecraftVersion other) { return compareTo(other) >= 0; }

    @Override public int compareTo(MinecraftVersion other) {
        if (other == null) return 1;
        int result = Integer.compare(major, other.major);
        if (result == 0) result = Integer.compare(minor, other.minor);
        if (result == 0) result = Integer.compare(build, other.build);
        if (result == 0) result = developmentStage.compareToIgnoreCase(other.developmentStage);
        return result;
    }

    @Override public boolean equals(Object other) {
        return other instanceof MinecraftVersion versionValue && compareTo(versionValue) == 0;
    }

    @Override public int hashCode() {
        return Objects.hash(major, minor, build, developmentStage.toLowerCase(Locale.ROOT));
    }

    @Override public String toString() { return version; }
}
