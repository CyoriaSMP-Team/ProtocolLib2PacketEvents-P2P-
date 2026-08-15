/*
 * ProtocolLib2PacketEvents - clean-room async listener helper.
 */
package com.comphenix.protocol.async;

import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.plugin.Plugin;

/** Internal no-op listener carrying an async whitelist. */
public class NullPacketListener implements PacketListener {
    private final PacketListener source;
    public NullPacketListener(PacketListener source) {
        if (source == null) throw new IllegalArgumentException("source cannot be null");
        this.source = source;
    }
    @Override public void onPacketSending(PacketEvent event) { }
    @Override public void onPacketReceiving(PacketEvent event) { }
    @Override public ListeningWhitelist getSendingWhitelist() {
        return copy(source.getSendingWhitelist());
    }
    @Override public ListeningWhitelist getReceivingWhitelist() {
        return copy(source.getReceivingWhitelist());
    }
    @Override public Plugin getPlugin() { return source.getPlugin(); }
    private static ListeningWhitelist copy(ListeningWhitelist original) {
        return original == null ? null : ListeningWhitelist.newBuilder(original)
                .priority(ListenerPriority.LOW).mergeOptions(ListenerOptions.ASYNC).build();
    }
}
