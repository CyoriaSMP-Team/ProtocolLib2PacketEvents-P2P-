package com.comphenix.protocol.async;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrent.PacketTypeMultiMap;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bounded, priority-ordered asynchronous processing queue.
 *
 * <p>This legacy queue is also used by {@link AsyncFilterManager}. The newer
 * PacketEvents callback path has a per-player serial executor in
 * {@code AsynchronousManagerImpl}; keeping this queue functional matters for
 * plugins that use the old queue accessors directly.</p>
 */
class PacketProcessingQueue {
    public static final com.comphenix.protocol.error.ReportType REPORT_GUAVA_CORRUPT_MISSING =
            new com.comphenix.protocol.error.ReportType("Guava collection support is unavailable");
    public static final int INITIAL_CAPACITY = 64;
    public static final int DEFAULT_MAXIMUM_CONCURRENCY = 32;
    public static final int DEFAULT_QUEUE_LIMIT = 1024 * 60;

    private final PriorityBlockingQueue<PacketEventHolder> queue =
            new PriorityBlockingQueue<>(INITIAL_CAPACITY);
    private final PacketTypeMultiMap<AsyncListenerHandler> map = new PacketTypeMultiMap<>();
    private final PlayerSendingHandler sendingHandler;
    private final int maximumConcurrency;
    private final int maximumSize;
    private final Semaphore slots;
    private final ExecutorService workers;

    public PacketProcessingQueue(PlayerSendingHandler sendingHandler) {
        this(sendingHandler, INITIAL_CAPACITY, DEFAULT_QUEUE_LIMIT, DEFAULT_MAXIMUM_CONCURRENCY);
    }

    public PacketProcessingQueue(PlayerSendingHandler sendingHandler, int initialSize,
                                 int maximumSize, int maximumConcurrency) {
        this.sendingHandler = sendingHandler;
        this.maximumSize = Math.max(1, maximumSize);
        this.maximumConcurrency = Math.max(1, maximumConcurrency);
        this.slots = new Semaphore(this.maximumConcurrency);
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger id = new AtomicInteger();
            @Override public Thread newThread(Runnable task) {
                Thread thread = new Thread(task, "P2P-processing-" + id.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        };
        this.workers = Executors.newFixedThreadPool(this.maximumConcurrency, factory);
    }

    public void addListener(AsyncListenerHandler listener, ListeningWhitelist whitelist) {
        map.put(whitelist, listener);
    }

    public List<PacketType> removeListener(AsyncListenerHandler listener, ListeningWhitelist whitelist) {
        return map.remove(whitelist, listener);
    }

    public boolean enqueue(PacketEvent event, boolean onMainThread) {
        if (event == null || queue.size() >= maximumSize) return false;
        if (!queue.offer(new PacketEventHolder(event))) return false;
        signalBeginProcessing(onMainThread);
        return true;
    }

    public int size() { return queue.size(); }

    public void signalBeginProcessing(boolean onMainThread) {
        while (slots.tryAcquire()) {
            PacketEventHolder holder = queue.poll();
            if (holder == null) {
                slots.release();
                return;
            }
            try {
                workers.execute(() -> process(holder, onMainThread));
            } catch (RuntimeException error) {
                queue.offer(holder);
                slots.release();
                return;
            }
        }
    }

    private void process(PacketEventHolder holder, boolean onMainThread) {
        PacketEvent event = holder.getEvent();
        try {
            AsyncMarker marker = event.getAsyncMarker();
            if (marker == null) {
                marker = new AsyncMarker();
                try { event.setAsyncMarker(marker); } catch (IllegalStateException ignored) { }
            }
            marker.incrementProcessingDelay();
            Iterable<AsyncListenerHandler> listeners = map.get(event.getPacketType());
            for (AsyncListenerHandler listener : listeners) listener.dispatchPacket(event);
            marker.setProcessed(true);
            marker.signal();
            if (sendingHandler != null) {
                PacketSendingQueue sending = sendingHandler.getSendingQueue(event, false);
                if (sending != null) sending.signalPacketUpdate(event, onMainThread);
            }
        } finally {
            slots.release();
            signalBeginProcessing(false);
        }
    }

    public void signalProcessingDone() {
        // Kept as a public lifecycle hook. A worker releases its slot in its
        // finally block; an external caller may release a manually reserved slot.
        if (slots.availablePermits() < maximumConcurrency) slots.release();
        signalBeginProcessing(false);
    }

    public int getMaximumConcurrency() { return maximumConcurrency; }
    public boolean contains(PacketType type) { return map.contains(type); }
    public Iterable<AsyncListenerHandler> get(PacketType type) { return map.get(type); }
    public ImmutableSet<PacketType> keySet() { return map.getPacketTypes(); }
    public Iterable<AsyncListenerHandler> values() { return map.values(); }

    public void cleanupAll() {
        queue.clear();
        map.clear();
        workers.shutdownNow();
    }
}
