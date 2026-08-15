package com.comphenix.protocol.internal;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

/** Chooses structured PacketEvents transport and direct wire fallback without duplicating dispatch. */
public final class BackendCoordinator {
    private final PacketBackend packetEvents = new PacketEventsBackend();
    private final PacketBackend directNetty = new DirectPacketBackend();

    public PacketBackend backendFor(PacketContainer packet) {
        // Only a real PacketEvents wrapper may enter the PacketEvents API.  Native
        // NMS objects and already-encoded buffers must bypass it, otherwise a
        // second decode/dispatch path can fire and the packet can be sent twice.
        return packet != null && packet.getPacketWrapper() != null ? packetEvents : directNetty;
    }

    public void send(Player player, PacketContainer packet, boolean filters) {
        backendFor(packet).send(player, packet, filters);
    }

    public void receive(Player player, PacketContainer packet, boolean filters) {
        backendFor(packet).receive(player, packet, filters);
    }

    public void sendWire(Player player, int packetId, byte[] payload) {
        directNetty.sendWire(player, packetId, payload);
    }

    public String describe() {
        return "structured=PacketEvents, raw=DirectNetty";
    }
}
