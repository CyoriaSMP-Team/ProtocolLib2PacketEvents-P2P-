/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol;

import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.netty.WirePacket;
import org.bukkit.entity.Player;

/** Common send/receive operations exposed by ProtocolLib. */
public interface PacketStream {

    void sendServerPacket(Player receiver, PacketContainer packet);

    void sendServerPacket(Player receiver, PacketContainer packet, boolean filters);

    default void sendServerPacket(Player receiver, PacketContainer packet,
                                  NetworkMarker marker, boolean filters) {
        sendServerPacket(receiver, packet, filters);
    }

    void sendWirePacket(Player receiver, int id, byte[] bytes);

    default void sendWirePacket(Player receiver, WirePacket packet) {
        if (packet == null) {
            throw new IllegalArgumentException("packet cannot be null");
        }
        sendWirePacket(receiver, packet.getId(), packet.getBytes());
    }

    void receiveClientPacket(Player sender, PacketContainer packet);

    default void receiveClientPacket(Player sender, PacketContainer packet, boolean filters) {
        receiveClientPacket(sender, packet);
    }

    default void receiveClientPacket(Player sender, PacketContainer packet,
                                     NetworkMarker marker, boolean filters) {
        receiveClientPacket(sender, packet, filters);
    }
}
