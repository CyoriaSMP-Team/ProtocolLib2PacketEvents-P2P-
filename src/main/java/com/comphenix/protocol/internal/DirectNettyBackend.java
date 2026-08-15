package com.comphenix.protocol.internal;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import org.bukkit.entity.Player;

/**
 * Direct fallback for payloads that do not have a PacketEvents wrapper. The bytes are the
 * packet body; this class adds the protocol VarInt packet id before writing to the player's
 * channel. It intentionally uses PacketEvents' Netty operators rather than depending on a
 * particular shaded Netty package or Minecraft's obfuscated classes.
 */
public final class DirectNettyBackend {
    private DirectNettyBackend() {}

    public static void sendWire(Player player, int packetId, byte[] payload) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (packetId < 0) throw new IllegalArgumentException("packet id must be non-negative");
        if (payload == null) payload = new byte[0];
        var api = PacketEvents.getAPI();
        if (api == null) throw new IllegalStateException("PacketEvents is not initialized");
        Object channel = api.getPlayerManager().getChannel(player);
        if (channel == null) throw new IllegalStateException("No Netty channel for " + player.getName());
        Object buffer = api.getNettyManager().getByteBufAllocationOperator().buffer(payload.length + 5);
        ByteBufHelper.writeVarInt(buffer, packetId);
        ByteBufHelper.writeBytes(buffer, payload);
        ChannelHelper.writeAndFlush(channel, buffer);
    }

    public static void receiveWire(Player player, int packetId, byte[] payload) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (packetId < 0) throw new IllegalArgumentException("packet id must be non-negative");
        if (payload == null) payload = new byte[0];
        var api = PacketEvents.getAPI();
        if (api == null) throw new IllegalStateException("PacketEvents is not initialized");
        Object channel = api.getPlayerManager().getChannel(player);
        if (channel == null) throw new IllegalStateException("No Netty channel for " + player.getName());
        Object buffer = api.getNettyManager().getByteBufAllocationOperator().buffer(payload.length + 5);
        ByteBufHelper.writeVarInt(buffer, packetId);
        ByteBufHelper.writeBytes(buffer, payload);
        ChannelHelper.fireChannelRead(channel, buffer);
    }

    /** Write an already-created native packet through the server encoder. */
    public static void sendNative(Player player, Object nativePacket) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (nativePacket == null) throw new IllegalArgumentException("nativePacket cannot be null");
        var api = PacketEvents.getAPI();
        if (api == null) throw new IllegalStateException("PacketEvents is not initialized");
        Object channel = api.getPlayerManager().getChannel(player);
        if (channel == null) throw new IllegalStateException("No Netty channel for " + player.getName());
        ChannelHelper.writeAndFlush(channel, nativePacket);
    }

    /** Feed an already-created native packet through the inbound pipeline. */
    public static void receiveNative(Player player, Object nativePacket) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (nativePacket == null) throw new IllegalArgumentException("nativePacket cannot be null");
        var api = PacketEvents.getAPI();
        if (api == null) throw new IllegalStateException("PacketEvents is not initialized");
        Object channel = api.getPlayerManager().getChannel(player);
        if (channel == null) throw new IllegalStateException("No Netty channel for " + player.getName());
        ChannelHelper.fireChannelRead(channel, nativePacket);
    }
}
