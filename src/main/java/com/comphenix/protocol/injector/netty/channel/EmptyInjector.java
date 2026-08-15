package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.WirePacket;
import org.bukkit.entity.Player;

import java.net.SocketAddress;

/** Explicit unsupported-connection injector used before a player/channel exists. */
final class EmptyInjector implements Injector {
    public static final Injector WITHOUT_PLAYER = new EmptyInjector(null);
    private Player player;
    private boolean closed;
    public EmptyInjector(Player player) { this.player = player; }
    public SocketAddress getAddress() { return null; }
    public int getProtocolVersion() { return -1; }
    public void inject() { if (closed) throw new IllegalStateException("injector is closed"); }
    public void close() { closed = true; }
    public void sendClientboundPacket(Object packet, NetworkMarker marker, boolean filtered) { throw unsupported(); }
    public void readServerboundPacket(Object packet) { throw unsupported(); }
    public void sendWirePacket(WirePacket packet) { throw unsupported(); }
    public void disconnect(String message) { closed = true; }
    public PacketType.Protocol getCurrentProtocol(PacketType.Sender sender) { return PacketType.Protocol.UNKNOWN; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public boolean isConnected() { return !closed && player != null && player.isOnline(); }
    public boolean isInjected() { return false; }
    public boolean isClosed() { return closed; }
    public String getPlayerName() { return player == null ? null : player.getName(); }
    public java.util.UUID getPlayerUniqueId() { return player == null ? null : player.getUniqueId(); }
    private UnsupportedOperationException unsupported() { return new UnsupportedOperationException("No Netty channel is available for this injector"); }
}
