/*
 * ProtocolLib2PacketEvents - clean-room compatibility API.
 */
package com.comphenix.protocol.scheduler;

import org.bukkit.scheduler.BukkitScheduler;

/** Bukkit-backed task handle. */
public final class DefaultTask implements Task {
    private final BukkitScheduler scheduler;
    private final int taskId;

    public DefaultTask(BukkitScheduler scheduler, int taskId) {
        if (scheduler == null) {
            throw new IllegalArgumentException("scheduler cannot be null");
        }
        this.scheduler = scheduler;
        this.taskId = taskId;
    }

    public int getTaskId() {
        return taskId;
    }

    @Override
    public void cancel() {
        scheduler.cancelTask(taskId);
    }
}
