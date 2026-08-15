/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.async;

import com.comphenix.protocol.events.PacketListener;
import org.bukkit.plugin.Plugin;

/**
 * Lightweight asynchronous-handler handle compatible with ProtocolLib's public API.
 * PacketEvents cannot suspend and re-inject a packet, so starting this handler controls the
 * listener lifecycle only; packets are still observed by the worker pool immediately.
 */
public class AsyncListenerHandler {

    private final PacketListener listener;
    private volatile boolean cancelled;
    private volatile int workerCount;

    public AsyncListenerHandler(PacketListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        this.listener = listener;
    }

    public PacketListener getAsyncListener() {
        return listener;
    }

    public Plugin getPlugin() {
        return listener.getPlugin();
    }

    public void start(int workerCount) {
        this.workerCount = Math.max(1, workerCount);
        this.cancelled = false;
    }

    public void syncStart() {
        start(1);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void cancel() {
        cancelled = true;
    }
}
