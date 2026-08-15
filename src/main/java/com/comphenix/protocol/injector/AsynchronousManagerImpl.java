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
import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListeningWhitelist;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Worker-pool implementation of {@link AsynchronousManager}.
 * <p>
 * Each player gets a serial execution lane on a shared pool, so a player's packets are handled
 * in arrival order while different players proceed in parallel. Lanes are created on demand and
 * dropped when a player disconnects.
 * <p>
 * PacketEvents calls the bridge before it continues with the channel operation.  The bridge
 * therefore submits the async work to a per-player serial lane and waits on that lane before
 * returning.  This is the hold/release point: a listener may mutate or cancel the shared
 * PacketEvent, and the PacketEvents event resumes only after the result is available.
 */
public class AsynchronousManagerImpl implements AsynchronousManager {

    private final ErrorReporter errorReporter;
    private final ExecutorService pool;
    private final Set<PacketListener> handlers = ConcurrentHashMap.newKeySet();
    private final Set<PacketListener> timeoutHandlers = ConcurrentHashMap.newKeySet();
    private final Map<PacketListener, AsyncListenerHandler> handlerHandles = new ConcurrentHashMap<>();

    /** Per-player serial lanes, so one player's packets stay ordered relative to each other. */
    private final Map<UUID, Executor> lanes = new ConcurrentHashMap<>();
    private final AtomicInteger queued = new AtomicInteger();
    private final AtomicInteger sequence = new AtomicInteger();

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
    public AsyncListenerHandler registerAsyncHandler(PacketListener listener) {
        if (listener == null) throw new IllegalArgumentException("listener cannot be null");
        handlers.add(listener);
        return handlerHandles.computeIfAbsent(listener, key -> {
            AsyncListenerHandler handler = new AsyncListenerHandler(key);
            handler.start(1);
            return handler;
        });
    }

    @Override
    public void unregisterAsyncHandler(AsyncListenerHandler handler) {
        if (handler != null) {
            unregisterAsyncHandler(handler.getAsyncListener());
            handler.cancel();
        }
    }

    @Override
    public void unregisterAsyncHandler(PacketListener listener) {
        handlers.remove(listener);
        AsyncListenerHandler handler = handlerHandles.remove(listener);
        if (handler != null) handler.cancel();
    }

    @Override
    public void unregisterAsyncHandlers(Plugin plugin) {
        for (PacketListener listener : new java.util.ArrayList<>(handlers)) {
            if (plugin.equals(listener.getPlugin())) unregisterAsyncHandler(listener);
        }
    }

    @Override
    public Set<PacketListener> getAsyncHandlers() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(handlers));
    }

    @Override public Set<PacketType> getSendingTypes() { return typesFor(true); }
    @Override public Set<PacketType> getReceivingTypes() { return typesFor(false); }

    private Set<PacketType> typesFor(boolean sending) {
        Set<PacketType> result = new LinkedHashSet<>();
        for (PacketListener listener : handlers) {
            ListeningWhitelist whitelist = sending ? listener.getSendingWhitelist() : listener.getReceivingWhitelist();
            if (whitelist != null && !whitelist.isEmpty()) result.addAll(whitelist.getTypes());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override public boolean hasAsynchronousListeners(PacketEvent event) {
        if (event == null) return false;
        boolean sending = event.isServerPacket();
        PacketType type = event.getPacketType();
        for (PacketListener listener : handlers) {
            ListeningWhitelist whitelist = sending ? listener.getSendingWhitelist() : listener.getReceivingWhitelist();
            if (whitelist != null && whitelist.getTypes().contains(type)) return true;
        }
        return false;
    }

    @Override public PacketStream getPacketStream() {
        try { return ProtocolLibrary.getProtocolManager(); }
        catch (IllegalStateException ignored) { return null; }
    }

    @Override public ErrorReporter getErrorReporter() { return errorReporter; }
    @Override public void cleanupAll() {
        lanes.clear();
        timeoutHandlers.clear();
        for (AsyncListenerHandler handler : handlerHandles.values()) handler.cancel();
        handlerHandles.clear();
    }
    @Override public void registerTimeoutHandler(PacketListener listener) { if (listener != null) timeoutHandlers.add(listener); }
    @Override public void unregisterTimeoutHandler(PacketListener listener) { timeoutHandlers.remove(listener); }
    @Override public Set<PacketListener> getTimeoutHandlers() { return Collections.unmodifiableSet(new LinkedHashSet<>(timeoutHandlers)); }

    @Override public void signalPacketTransmission(PacketEvent packet) {
        if (packet != null && packet.getAsyncMarker() != null) packet.getAsyncMarker().signal();
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
        submit(event, false);
    }

    /**
     * Hold the current packet callback until async listeners and any explicit processing delays
     * have completed.  The return value is false when the async stage cancelled or timed out.
     */
    public boolean processAndWait(PacketEvent event) {
        if (event == null) {
            return true;
        }
        CompletableFuture<Void> completion = submit(event, true);
        if (completion == null) {
            return !event.isCancelled();
        }

        AsyncMarker marker = event.getAsyncMarker();
        long remaining = Math.max(1L, marker.getTimeout() - System.currentTimeMillis());
        try {
            completion.get(remaining, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            marker.setAsyncCancelled(true);
            event.setCancelled(true);
            errorReporter.reportWarning(this, "Interrupted while holding " + event.getPacketType(), e);
        } catch (TimeoutException e) {
            marker.setAsyncCancelled(true);
            event.setCancelled(true);
            errorReporter.reportWarning(this, "Async packet processing timed out for " + event.getPacketType(), e);
        } catch (ExecutionException e) {
            marker.setAsyncCancelled(true);
            event.setCancelled(true);
            errorReporter.reportDetailed(this, "Async packet processing failed for " + event.getPacketType(), e.getCause());
        }
        return !event.isCancelled() && !marker.isAsyncCancelled();
    }

    private CompletableFuture<Void> submit(PacketEvent event, boolean hold) {
        if (handlers.isEmpty()) {
            return null;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return null;
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
            return null;
        }

        AsyncMarker selectedMarker = event.getAsyncMarker();
        if (selectedMarker == null) {
            selectedMarker = new AsyncMarker();
            event.setAsyncMarker(selectedMarker);
        }
        final AsyncMarker marker = selectedMarker;
        event.setAsync(true);
        marker.setQueuedSendingIndex((long) sequence.incrementAndGet());
        CompletableFuture<Void> completion = new CompletableFuture<>();
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
                marker.setProcessed(true);
                if (hold) {
                    waitForRelease(marker);
                }
                if (marker.hasExpired() || marker.isAsyncCancelled()) {
                    event.setCancelled(true);
                }
                completion.complete(null);
            } catch (Throwable error) {
                completion.completeExceptionally(error);
            } finally {
                queued.decrementAndGet();
            }
        });
        return completion;
    }

    private static void waitForRelease(AsyncMarker marker) {
        Object lock = marker.getProcessingLock();
        synchronized (lock) {
            while (marker.getProcessingDelay() > 0 && !marker.isAsyncCancelled()
                    && !marker.hasExpired()) {
                long remaining = marker.getTimeout() - System.currentTimeMillis();
                if (remaining <= 0) {
                    marker.setAsyncCancelled(true);
                    break;
                }
                try {
                    lock.wait(Math.min(remaining, 1000L));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    marker.setAsyncCancelled(true);
                    break;
                }
            }
        }
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
        for (AsyncListenerHandler handler : handlerHandles.values()) handler.cancel();
        handlerHandles.clear();
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
