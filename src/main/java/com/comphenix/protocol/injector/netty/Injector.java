/*
 * ProtocolLib2PacketEvents - clean-room injector contract.
 */
package com.comphenix.protocol.injector.netty;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.injector.netty.WirePacket;
import org.bukkit.entity.Player;

import java.net.SocketAddress;
import java.util.UUID;

/** Public connection-injector surface retained for legacy ProtocolLib integrations. */
public interface Injector {
    SocketAddress getAddress();
    int getProtocolVersion();
    void inject();
    void close();
    void sendClientboundPacket(Object packet, NetworkMarker marker, boolean filtered);
    void readServerboundPacket(Object packet);
    void sendWirePacket(WirePacket packet);
    void disconnect(String message);
    PacketType.Protocol getCurrentProtocol(PacketType.Sender sender);
    Player getPlayer();
    default String getPlayerName() { return getPlayer() == null ? null : getPlayer().getName(); }
    default UUID getPlayerUniqueId() { return getPlayer() == null ? null : getPlayer().getUniqueId(); }
    void setPlayer(Player player);
    boolean isConnected();
    boolean isInjected();
    boolean isClosed();
}
