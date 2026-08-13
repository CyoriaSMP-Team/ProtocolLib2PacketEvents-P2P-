/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 *
 * Copyright (C) 2026 CyoriaSMP Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.comphenix.protocol.injector;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Worker-pool implementation of {@link AsynchronousManager}.
 * <p>
 * Each player gets a serial execution lane on a shared pool, so a player's packets are handled
 * in arrival order while different players proceed in parallel. Lanes are created on demand and
 * dropped when a player disconnects.
 * <p>
 * <strong>Semantic caveat.</strong> Real ProtocolLib can suspend a packet mid-pipeline and
 * re-inject it once an async listener finishes, which is what lets its async listeners cancel
 * and mutate packets. PacketEvents exposes no equivalent hold-and-reinject hook, so this
 * implementation dispatches a snapshot to the worker pool and lets the packet continue
 * immediately. Async listeners therefore observe packets; they cannot alter them. Cancellation
 * or mutation requires a synchronous listener. This is documented rather than silently
 * approximated, because an async listener that appeared to cancel packets but did not would be
 * a much worse failure than an explicit limitation.
 */
public class AsynchronousManagerImpl implements AsynchronousManager {

    private final ErrorReporter errorReporter;
    private final ExecutorService pool;
    private final Set<PacketListener> handlers = ConcurrentHashMap.newKeySet();

    /** Per-player serial lanes, so one player's packets stay ordered relative to each other. */
    private final Map<UUID, Executor> lanes = new ConcurrentHashMap<>();
    private final AtomicInteger queued = new AtomicInteger();

    public AsynchronousManagerImpl(ErrorReporter errorReporter) {
        this(errorReporter, defaultPool());
    }

    public AsynchronousManagerImpl(ErrorReporter errorReporter, ExecutorService pool) {
        this.errorReporter = errorReporter;
        this.pool = pool;
    }

    private static ExecutorService defaultPool() {
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "P2P-async-" + counter.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };
        int size = Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() / 2));
        return Executors.newFixedThreadPool(size, factory);
    }

    @Override
    public void registerAsyncHandler(PacketListener listener) {
        handlers.add(listener);
    }

    @Override
    public void unregisterAsyncHandler(PacketListener listener) {
        handlers.remove(listener);
    }

    @Override
    public void unregisterAsyncHandlers(Plugin plugin) {
        handlers.removeIf(listener -> plugin.equals(listener.getPlugin()));
    }

    @Override
    public Set<PacketListener> getAsyncHandlers() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(handlers));
    }

    @Override
    public int getQueuedPacketCount() {
        return queued.get();
    }

    /** Drops a player's execution lane. Called when they disconnect. */
    public void releasePlayer(UUID playerId) {
        lanes.remove(playerId);
    }

    /**
     * Hands a packet to the async listeners interested in it. Called from the dispatcher after
     * the synchronous listeners have run; returns immediately.
     */
    public void enqueue(PacketEvent event) {
        if (handlers.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        PacketType type = event.getPacketType();
        boolean sending = event.isServerPacket();

        Set<PacketListener> interested = new LinkedHashSet<>();
        for (PacketListener listener : handlers) {
            var whitelist = sending ? listener.getSendingWhitelist() : listener.getReceivingWhitelist();
            if (whitelist != null && whitelist.getTypes().contains(type)) {
                interested.add(listener);
            }
        }
        if (interested.isEmpty()) {
            return;
        }

        event.setAsync(true);
        queued.incrementAndGet();
        laneFor(player.getUniqueId()).execute(() -> {
            try {
                for (PacketListener listener : interested) {
                    try {
                        if (sending) {
                            listener.onPacketSending(event);
                        } else {
                            listener.onPacketReceiving(event);
                        }
                    } catch (Exception e) {
                        errorReporter.reportDetailed(listener,
                                "Error in asynchronous handling of " + type, e);
                    }
                }
            } finally {
                queued.decrementAndGet();
            }
        });
    }

    /**
     * A serial lane on the shared pool. Tasks submitted for the same player run one at a time,
     * in submission order, without dedicating a thread to each player.
     */
    private Executor laneFor(UUID playerId) {
        return lanes.computeIfAbsent(playerId, id -> new SerialExecutor(pool));
    }

    @Override
    public void shutdown() {
        lanes.clear();
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /** Runs submitted tasks one at a time, in order, on a shared executor. */
    private static final class SerialExecutor implements Executor {

        private final Executor delegate;
        private final java.util.ArrayDeque<Runnable> tasks = new java.util.ArrayDeque<>();
        private boolean running;

        private SerialExecutor(Executor delegate) {
            this.delegate = delegate;
        }

        @Override
        public void execute(Runnable command) {
            synchronized (this) {
                tasks.add(command);
                if (running) {
                    return;
                }
                running = true;
            }
            scheduleNext();
        }

        private void scheduleNext() {
            Runnable next;
            synchronized (this) {
                next = tasks.poll();
                if (next == null) {
                    running = false;
                    return;
                }
            }
            Runnable task = next;
            delegate.execute(() -> {
                try {
                    task.run();
                } finally {
                    scheduleNext();
                }
            });
        }
    }
}
