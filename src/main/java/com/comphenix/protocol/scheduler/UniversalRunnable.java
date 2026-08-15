/*
 * ProtocolLib2PacketEvents - clean-room compatibility API.
 */
package com.comphenix.protocol.scheduler;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.plugin.Plugin;

/** Runnable convenience base class used by older ProtocolLib integrations. */
public abstract class UniversalRunnable implements Runnable {
    private Task task;

    public synchronized void cancel() {
        if (task != null) task.cancel();
    }

    public synchronized Task runTask(Plugin plugin) {
        requirePlugin(plugin);
        return task = ProtocolLibrary.getScheduler().runTask(this);
    }

    public synchronized Task runTaskLater(Plugin plugin, long delay) {
        requirePlugin(plugin);
        return task = ProtocolLibrary.getScheduler().scheduleSyncDelayedTask(this, delay);
    }

    public Task runTask() {
        return runTask(ProtocolLibrary.getPlugin());
    }

    public Task runTaskLater(long delay) {
        return runTaskLater(ProtocolLibrary.getPlugin(), delay);
    }

    public Task runTaskTimer(long delay, long period) {
        return ProtocolLibrary.getScheduler().scheduleSyncRepeatingTask(this, delay, period);
    }

    public Task runTaskAsynchronously() {
        return ProtocolLibrary.getScheduler().runTaskAsync(this);
    }

    private static void requirePlugin(Plugin plugin) {
        if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
        if (ProtocolLibrary.getScheduler() == null) throw new IllegalStateException("ProtocolLib scheduler is unavailable");
    }
}
