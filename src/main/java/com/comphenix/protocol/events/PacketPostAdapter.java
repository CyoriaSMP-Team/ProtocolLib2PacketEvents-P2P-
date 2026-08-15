package com.comphenix.protocol.events;

import org.bukkit.plugin.Plugin;

/** Convenience base class for post listeners. */
public abstract class PacketPostAdapter implements PacketPostListener {
    private final Plugin plugin;

    public PacketPostAdapter(Plugin plugin) {
        if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
        this.plugin = plugin;
    }

    @Override public Plugin getPlugin() { return plugin; }
}
