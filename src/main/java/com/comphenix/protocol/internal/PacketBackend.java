package com.comphenix.protocol.internal;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

/** Internal transport boundary between the ProtocolLib API and packet backends. */
public interface PacketBackend {
    String name();
    void send(Player player, PacketContainer packet, boolean filters);
    void receive(Player player, PacketContainer packet, boolean filters);
    void sendWire(Player player, int packetId, byte[] payload);
}
