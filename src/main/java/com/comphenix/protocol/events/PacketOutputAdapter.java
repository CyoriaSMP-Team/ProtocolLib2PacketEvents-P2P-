package com.comphenix.protocol.events;

import org.bukkit.plugin.Plugin;

/** Convenience base class for output handlers. */
public abstract class PacketOutputAdapter implements PacketOutputHandler {
    private final Plugin plugin;
    private final ListenerPriority priority;

    public PacketOutputAdapter(Plugin plugin, ListenerPriority priority) {
        this.plugin = plugin;
        this.priority = priority == null ? ListenerPriority.NORMAL : priority;
    }

    @Override public Plugin getPlugin() { return plugin; }
    @Override public ListenerPriority getPriority() { return priority; }
}
