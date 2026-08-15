/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.events;

import org.bukkit.plugin.Plugin;

/** Allows a listener to transform the encoded wire representation of a packet. */
public interface PacketOutputHandler {
    ListenerPriority getPriority();
    Plugin getPlugin();
    byte[] handle(PacketEvent event, byte[] buffer);
}
