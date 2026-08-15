/*
 * ProtocolLib2PacketEvents - clean-room asynchronous worker contract.
 */
package com.comphenix.protocol.async;

import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.google.common.base.Function;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A bounded asynchronous listener queue retained for the legacy ProtocolLib API.
 *
 * <p>The main P2P dispatcher owns the packet hold/reinject boundary and uses its
 * per-player serial executor. This class remains useful to integrations which
 * explicitly create a worker loop: queued events are consumed in FIFO order,
 * cancellation interrupts all workers, and a full queue fails loudly instead
 * of silently discarding a packet.</p>
 */
public class AsyncListenerHandler {

    public static final com.comphenix.protocol.error.ReportType REPORT_HANDLER_NOT_STARTED =
            new com.comphenix.protocol.error.ReportType(
                    "Plugin %s did not start the asynchronous handler %s by calling start() or syncStart().");

    private static final int DEFAULT_CAPACITY = 1024;
    private static final AtomicInteger NEXT_WORKER_ID = new AtomicInteger();

    private final PacketListener listener;
    private final ArrayBlockingQueue<PacketEvent> queuedPackets = new ArrayBlockingQueue<>(DEFAULT_CAPACITY);
    private final ConcurrentHashMap<Integer, Worker> workers = new ConcurrentHashMap<>();
    private final AtomicInteger nextWorkerId = new AtomicInteger();
    private volatile boolean cancelled;
    private volatile int workerCount;

    public AsyncListenerHandler(PacketListener listener) {
        if (listener == null) throw new IllegalArgumentException("listener cannot be null");
        this.listener = listener;
    }

    public PacketListener getAsyncListener() { return listener; }
    public Plugin getPlugin() { return listener.getPlugin(); }

    public synchronized void start(int workersRequested) {
        if (cancelled) throw new IllegalStateException("Cannot start a cancelled asynchronous handler");
        int desired = Math.max(1, workersRequested);
        workerCount = desired;
        while (workers.size() < desired) {
            Worker worker = new Worker(nextWorkerId.incrementAndGet());
            workers.put(worker.id, worker);
            Thread thread = new Thread(worker,
                    "Protocol Worker #" + worker.id + " - " + PacketAdapter.getPluginName(listener));
            thread.setDaemon(true);
            worker.thread = thread;
            thread.start();
        }
    }

    public synchronized void start() { start(1); }

    /** Starts one loop through a caller-provided executor. */
    public synchronized void start(Function<AsyncRunnable, Void> executor) {
        if (executor == null) throw new IllegalArgumentException("executor cannot be null");
        if (cancelled) throw new IllegalStateException("Cannot start a cancelled asynchronous handler");
        Worker worker = new Worker(nextWorkerId.incrementAndGet());
        workers.put(worker.id, worker);
        workerCount = Math.max(1, workerCount + 1);
        try {
            executor.apply(worker);
        } catch (RuntimeException | Error error) {
            workers.remove(worker.id);
            workerCount = Math.max(0, workerCount - 1);
            throw error;
        }
    }

    public boolean syncStart() { start(1); return true; }

    public boolean syncStart(long time, TimeUnit unit) {
        if (unit == null) throw new IllegalArgumentException("unit cannot be null");
        start(1);
        return true;
    }

    public boolean isCancelled() { return cancelled; }

    public void enqueuePacket(PacketEvent packet) {
        if (packet == null) throw new IllegalArgumentException("packet cannot be null");
        if (cancelled || !queuedPackets.offer(packet)) {
            throw new IllegalStateException("The asynchronous packet queue is closed or full");
        }
    }

    /** Package-private synchronous worker hook used by the ordered compatibility queue. */
    void dispatchPacket(PacketEvent packet) {
        if (packet != null && !cancelled) process(packet);
    }

    public AsyncRunnable getListenerLoop() {
        return new Worker(nextWorkerId.incrementAndGet());
    }

    public synchronized void cancel() {
        cancelled = true;
        stopWorkers(new ArrayList<>(workers.values()));
        queuedPackets.clear();
    }

    public synchronized void stop() {
        stopWorkers(new ArrayList<>(workers.values()));
        workerCount = 0;
    }

    public synchronized void stop(int count) {
        if (count <= 0) return;
        List<Worker> current = new ArrayList<>(workers.values());
        stopWorkers(current.subList(0, Math.min(count, current.size())));
        workerCount = workers.size();
    }

    public boolean syncStop() { stop(); return true; }

    public synchronized void setWorkers(int workersRequested) {
        if (workersRequested <= 0) {
            stop();
            return;
        }
        if (workersRequested > workers.size()) start(workersRequested);
        else if (workersRequested < workers.size()) stop(workers.size() - workersRequested);
        workerCount = workersRequested;
    }

    public int getWorkers() { return workerCount; }
    public int getWorkerCount() { return workerCount; }

    public String getFriendlyWorkerName(int id) {
        return String.format("Protocol Worker #%s - %s", id, PacketAdapter.getPluginName(listener));
    }

    private void stopWorkers(List<Worker> toStop) {
        for (Worker worker : toStop) worker.stopQuietly();
    }

    private void process(PacketEvent packet) {
        try {
            if (packet.isServerPacket()) listener.onPacketSending(packet);
            else listener.onPacketReceiving(packet);
        } catch (Throwable error) {
            ProtocolLogger.log(java.util.logging.Level.WARNING,
                    "Error in asynchronous listener " + PacketAdapter.getPluginName(listener), error);
        }
    }

    private final class Worker implements AsyncRunnable {
        private final int id;
        private final AtomicBoolean firstRun = new AtomicBoolean();
        private final AtomicBoolean stopped = new AtomicBoolean();
        private volatile boolean running;
        private volatile boolean finished;
        private volatile Thread thread;

        private Worker(int id) { this.id = id; }

        @Override public int getID() { return id; }

        @Override public void run() {
            if (!firstRun.compareAndSet(false, true)) {
                throw new IllegalStateException("This listener loop has already been run");
            }
            running = true;
            thread = Thread.currentThread();
            try {
                while (!cancelled && !stopped.get()) {
                    try {
                        PacketEvent packet = queuedPackets.poll(1, TimeUnit.SECONDS);
                        if (packet != null) process(packet);
                    } catch (InterruptedException error) {
                        if (cancelled || stopped.get()) break;
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } finally {
                running = false;
                finished = true;
                workers.remove(id, this);
            }
        }

        @Override public boolean stop() {
            boolean wasRunning = running;
            stopQuietly();
            return wasRunning;
        }

        private void stopQuietly() {
            stopped.set(true);
            Thread current = thread;
            if (current != null) current.interrupt();
        }

        @Override public boolean isRunning() { return running; }
        @Override public boolean isFinished() { return finished; }
    }
}
