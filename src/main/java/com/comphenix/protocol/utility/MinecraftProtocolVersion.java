package com.comphenix.protocol.utility;

/** Maps the PacketEvents server-version model to ProtocolLib's integer API. */
public final class MinecraftProtocolVersion {
    public MinecraftProtocolVersion() { }
    public static int getCurrentVersion() { return MinecraftVersion.current().getProtocolVersion(); }
    public static int getVersion(MinecraftVersion version) { return version == null ? -1 : version.getProtocolVersion(); }
}
