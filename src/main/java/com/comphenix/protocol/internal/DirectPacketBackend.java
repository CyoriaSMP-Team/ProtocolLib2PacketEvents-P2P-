package com.comphenix.protocol.internal;

import com.comphenix.protocol.events.PacketContainer;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import org.bukkit.entity.Player;

/**
 * Direct transport for a packet that PacketEvents cannot represent as a wrapper.
 * It deliberately fails loudly when the container has neither wire bytes nor a
 * native packet object; silently dropping such packets would break ProtocolLib's
 * cancellation/send contract.
 */
public final class DirectPacketBackend implements PacketBackend {
    @Override public String name() { return "DirectNetty"; }

    @Override public void send(Player player, PacketContainer packet, boolean filters) {
        require(packet, "send");
        Object raw = packet.getRawBuffer();
        if (raw != null) {
            DirectNettyBackend.sendWire(player, packet.getId(), copyBytes(raw));
            return;
        }
        Object nativePacket = packet.getHandle();
        if (nativePacket == null) throw unsupported(packet);
        DirectNettyBackend.sendNative(player, nativePacket);
    }

    @Override public void receive(Player player, PacketContainer packet, boolean filters) {
        require(packet, "receive");
        Object raw = packet.getRawBuffer();
        if (raw != null) {
            DirectNettyBackend.receiveWire(player, packet.getId(), copyBytes(raw));
            return;
        }
        Object nativePacket = packet.getHandle();
        if (nativePacket == null) throw unsupported(packet);
        DirectNettyBackend.receiveNative(player, nativePacket);
    }

    @Override public void sendWire(Player player, int packetId, byte[] payload) {
        DirectNettyBackend.sendWire(player, packetId, payload);
    }

    private static void require(PacketContainer packet, String operation) {
        if (packet == null) throw new IllegalArgumentException("Cannot " + operation + " a null packet");
    }

    private static byte[] copyBytes(Object raw) {
        if (raw instanceof byte[] bytes) return bytes.clone();
        try {
            return ByteBufHelper.copyBytes(raw);
        } catch (RuntimeException error) {
            throw new IllegalStateException("Raw packet buffer is not readable by PacketEvents' ByteBuf adapter", error);
        }
    }

    private static IllegalStateException unsupported(PacketContainer packet) {
        return new IllegalStateException("No direct representation is available for " + packet.getType());
    }
}
