/*
 * ProtocolLib2PacketEvents - clean-room compatibility API.
 */
package com.comphenix.protocol.scheduler;

/**
 * Small scheduler abstraction used by ProtocolLib-facing plugins.  Keeping Bukkit scheduling
 * behind this interface also makes packet code testable without a running server.
 */
public interface ProtocolScheduler {
    Task scheduleSyncRepeatingTask(Runnable task, long delay, long period);

    Task runTask(Runnable task);

    Task scheduleSyncDelayedTask(Runnable task, long delay);

    Task runTaskAsync(Runnable task);
}
