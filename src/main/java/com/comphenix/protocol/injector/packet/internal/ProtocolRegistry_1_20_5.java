package com.comphenix.protocol.injector.packet.internal;

import com.comphenix.protocol.injector.packet.PacketRegistry;

import java.util.List;
import java.util.function.Supplier;

public class ProtocolRegistry_1_20_5 {
    public static final List<String> PACKET_TYPES_CLASS_NAMES = List.of(
            "common.CommonPacketTypes", "configuration.ConfigurationPacketTypes",
            "cookie.CookiePacketTypes", "game.GamePacketTypes", "handshake.HandshakePacketTypes",
            "login.LoginPacketTypes", "ping.PingPacketTypes", "status.StatusPacketTypes");
    public static final List<Protocol> PROTOCOLS = List.of(
            new Protocol("configuration.ConfigurationProtocols", null),
            new Protocol("game.GameProtocols", ProtocolContexts::createGameProtocolContext),
            new Protocol("handshake.HandshakeProtocols", null),
            new Protocol("login.LoginProtocols", null),
            new Protocol("status.StatusProtocols", null));
    public ProtocolRegistry_1_20_5() { }
    public static void fillRegister(PacketRegistry.Register register) {
        if (register == null) throw new IllegalArgumentException("register cannot be null");
        // PacketEvents already resolved the version-specific protocol codecs. Reuse that
        // authoritative registry here instead of pretending to reflect a 1.20.5 NMS layout.
        for (com.comphenix.protocol.PacketType type : com.comphenix.protocol.PacketType.values()) {
            if (type != null && type.isSupported() && type.getPacketClass() != null) {
                register.registerPacket(type, type.getPacketClass(), type.getSender(), null);
            }
        }
    }
    static record Protocol(String className, Supplier<Object> context) { }
}
