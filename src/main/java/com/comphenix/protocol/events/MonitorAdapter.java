package com.comphenix.protocol.events;

import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

/** Convenience monitor listener. */
public abstract class MonitorAdapter implements PacketListener {
    private final Plugin plugin;
    private final ListeningWhitelist sending;
    private final ListeningWhitelist receiving;

    public MonitorAdapter(Plugin plugin, ConnectionSide side) { this(plugin, side, Logger.getLogger("ProtocolLib2PacketEvents")); }
    public MonitorAdapter(Plugin plugin, ConnectionSide side, Logger logger) {
        this.plugin = plugin;
        ListeningWhitelist.Builder builder = ListeningWhitelist.newBuilder().monitor();
        this.sending = side != null && side.isForServer() ? builder.build() : ListeningWhitelist.EMPTY_WHITELIST;
        this.receiving = side != null && side.isForClient() ? builder.build() : ListeningWhitelist.EMPTY_WHITELIST;
    }
    @Override public ListeningWhitelist getSendingWhitelist() { return sending; }
    @Override public ListeningWhitelist getReceivingWhitelist() { return receiving; }
    @Override public Plugin getPlugin() { return plugin; }
    @Override public void onPacketSending(PacketEvent event) { }
    @Override public void onPacketReceiving(PacketEvent event) { }
}
