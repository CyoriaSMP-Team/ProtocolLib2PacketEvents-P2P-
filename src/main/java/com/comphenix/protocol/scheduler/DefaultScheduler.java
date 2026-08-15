/*
 * ProtocolLib2PacketEvents - clean-room compatibility API.
 */
package com.comphenix.protocol.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

/** Adapter from ProtocolLib's scheduler API to Bukkit's scheduler. */
public final class DefaultScheduler implements ProtocolScheduler {
    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    public DefaultScheduler(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public Task scheduleSyncRepeatingTask(Runnable task, long delay, long period) {
        requireTask(task);
        int id = scheduler.scheduleSyncRepeatingTask(plugin, task, delay, period);
        return id < 0 ? null : new DefaultTask(scheduler, id);
    }

    @Override
    public Task runTask(Runnable task) {
        requireTask(task);
        return new DefaultTask(scheduler, scheduler.runTask(plugin, task).getTaskId());
    }

    @Override
    public Task scheduleSyncDelayedTask(Runnable task, long delay) {
        requireTask(task);
        int id = scheduler.scheduleSyncDelayedTask(plugin, task, delay);
        return id < 0 ? null : new DefaultTask(scheduler, id);
    }

    @Override
    public Task runTaskAsync(Runnable task) {
        requireTask(task);
        return new DefaultTask(scheduler, scheduler.runTaskAsynchronously(plugin, task).getTaskId());
    }

    private static void requireTask(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("task cannot be null");
        }
    }
}
