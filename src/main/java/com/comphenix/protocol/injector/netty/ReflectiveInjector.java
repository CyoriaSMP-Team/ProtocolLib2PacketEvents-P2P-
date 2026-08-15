/*
 * ProtocolLib2PacketEvents - clean-room PacketEvents injector adapter.
 */
package com.comphenix.protocol.injector.netty;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.internal.DirectNettyBackend;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.entity.Player;

import java.net.SocketAddress;

/**
 * Lightweight injector used by the legacy socket API.  PacketEvents owns the actual channel;
 * this object forwards operations to its public player/channel manager rather than modifying
 * Netty pipeline ownership behind PacketEvents' back.
 */
public final class ReflectiveInjector implements Injector {
    private volatile Player player;
    private final Object networkManager;
    private volatile boolean injected;
    private volatile boolean closed;

    public ReflectiveInjector(Player player, Object networkManager) {
        this.player = player;
        this.networkManager = networkManager;
    }

    @Override public SocketAddress getAddress() { return player == null ? null : player.getAddress(); }
    @Override public int getProtocolVersion() {
        if (player == null || PacketEvents.getAPI() == null) return Integer.MIN_VALUE;
        var version = PacketEvents.getAPI().getPlayerManager().getClientVersion(player);
        return version == null ? Integer.MIN_VALUE : version.getProtocolVersion();
    }
    @Override public void inject() { injected = true; }
    @Override public void close() { closed = true; injected = false; }
    @Override public void sendClientboundPacket(Object packet, NetworkMarker marker, boolean filtered) {
        if (player == null) throw new IllegalStateException("No player is attached to injector");
        if (filtered) PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        else PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
    }
    @Override public void readServerboundPacket(Object packet) {
        if (player == null) throw new IllegalStateException("No player is attached to injector");
        PacketEvents.getAPI().getPlayerManager().receivePacket(player, packet);
    }
    @Override public void sendWirePacket(WirePacket packet) {
        if (player == null) throw new IllegalStateException("No player is attached to injector");
        DirectNettyBackend.sendWire(player, packet.getId(), packet.getBytes());
    }
    @Override public void disconnect(String message) { if (player != null) player.kickPlayer(message); }
    @Override public PacketType.Protocol getCurrentProtocol(PacketType.Sender sender) { return PacketType.Protocol.PLAY; }
    @Override public Player getPlayer() { return player; }
    @Override public void setPlayer(Player player) { this.player = player; }
    @Override public boolean isConnected() { return !closed && player != null && player.isOnline(); }
    @Override public boolean isInjected() { return injected; }
    @Override public boolean isClosed() { return closed; }
    public Object getNetworkManager() { return networkManager; }
}
