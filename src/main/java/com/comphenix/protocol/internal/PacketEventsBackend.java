package com.comphenix.protocol.internal;

import com.comphenix.protocol.events.PacketContainer;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.entity.Player;

/** PacketEvents transport for structured packets. */
public final class PacketEventsBackend implements PacketBackend {
    @Override public String name() { return "PacketEvents"; }

    private static void requireStructured(PacketContainer packet, String action) {
        if (packet == null || !packet.hasStructuredAccess()) {
            throw new IllegalArgumentException("Cannot " + action + " an unstructured packet through PacketEvents");
        }
    }

    @Override
    public void send(Player player, PacketContainer packet, boolean filters) {
        requireStructured(packet, "send");
        Object wrapper = packet.getPacketWrapper();
        if (wrapper != null) {
            if (filters) PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet.getPacketWrapper());
            else PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet.getPacketWrapper());
        } else if (filters) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet.getHandle());
        } else {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet.getHandle());
        }
    }

    @Override
    public void receive(Player player, PacketContainer packet, boolean filters) {
        requireStructured(packet, "receive");
        Object wrapper = packet.getPacketWrapper();
        if (wrapper != null) {
            if (filters) PacketEvents.getAPI().getPlayerManager().receivePacket(player, packet.getPacketWrapper());
            else PacketEvents.getAPI().getPlayerManager().receivePacketSilently(player, packet.getPacketWrapper());
        } else if (filters) {
            PacketEvents.getAPI().getPlayerManager().receivePacket(player, packet.getHandle());
        } else {
            PacketEvents.getAPI().getPlayerManager().receivePacketSilently(player, packet.getHandle());
        }
    }

    @Override
    public void sendWire(Player player, int packetId, byte[] payload) {
        DirectNettyBackend.sendWire(player, packetId, payload);
    }
}
