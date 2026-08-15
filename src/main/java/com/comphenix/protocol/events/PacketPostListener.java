/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.events;

import org.bukkit.plugin.Plugin;

/** Listener invoked after a packet has passed through the network pipeline. */
public interface PacketPostListener {
    Plugin getPlugin();
    void onPostEvent(PacketEvent event);
}
