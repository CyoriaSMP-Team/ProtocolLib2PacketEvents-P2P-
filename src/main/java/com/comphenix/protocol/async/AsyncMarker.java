/*
 * ProtocolLib2PacketEvents - clean-room asynchronous packet contract.
 */
package com.comphenix.protocol.async;

import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.injector.netty.Injector;

import java.io.Serializable;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mutable coordination state attached to an asynchronous packet.
 *
 * <p>PacketEvents delivers an event synchronously on its channel callback, so P2P can hold that
 * callback while worker listeners run and then let PacketEvents continue with the modified or
 * cancelled event.</p>
 */
public class AsyncMarker implements Serializable, Comparable<AsyncMarker> {
    private static final long serialVersionUID = -2621497386L;

    public static final int DEFAULT_TIMEOUT_DELTA = 1800 * 1000;
    public static final int DEFAULT_SENDING_DELTA = 0;

    private final long initialTime;
    private volatile long timeout;
    private final long originalSendingIndex;
    private final transient Injector injector;
    private volatile long newSendingIndex;
    private volatile Long queuedSendingIndex;
    private volatile boolean processed;
    private final AtomicBoolean transmitted = new AtomicBoolean(false);
    private volatile boolean asyncCancelled;
    private final AtomicInteger processingDelay = new AtomicInteger();
    private volatile Object processingLock = new Object();
    private transient AsyncListenerHandler listenerHandler;
    private transient int workerID;
    private transient PacketStream packetStream;
    private transient Iterator<AsyncListenerHandler> listenerTraversal;

    /** Creates a marker with a fresh send-order index. */
    public AsyncMarker() {
        this(null, 0L, System.currentTimeMillis(), DEFAULT_TIMEOUT_DELTA);
    }

    /** Internal constructor used when a caller already has a sequence number. */
    public AsyncMarker(long sendingIndex, long initialTime, long timeoutDelta) {
        this(null, sendingIndex, initialTime, timeoutDelta);
    }

    /** Internal constructor retained for the native injector path. */
    AsyncMarker(Injector injector, long sendingIndex, long initialTime, long timeoutDelta) {
        this.injector = injector;
        this.originalSendingIndex = sendingIndex;
        this.newSendingIndex = sendingIndex;
        this.initialTime = initialTime;
        this.timeout = initialTime + Math.max(0L, timeoutDelta);
    }

    public long getInitialTime() {
        return initialTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
        notifyProcessingWaiters();
    }

    public long getOriginalSendingIndex() {
        return originalSendingIndex;
    }

    public long getNewSendingIndex() {
        return newSendingIndex;
    }

    public void setNewSendingIndex(long newSendingIndex) {
        this.newSendingIndex = newSendingIndex;
    }

    @Deprecated
    public PacketStream getPacketStream() {
        if (packetStream != null) return packetStream;
        try { return ProtocolLibrary.getProtocolManager(); }
        catch (IllegalStateException ignored) { return null; }
    }

    @Deprecated
    public void setPacketStream(PacketStream packetStream) {
        this.packetStream = packetStream;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
        notifyProcessingWaiters();
    }

    public int incrementProcessingDelay() {
        return processingDelay.incrementAndGet();
    }

    int decrementProcessingDelay() {
        return processingDelay.updateAndGet(value -> value > 0 ? value - 1 : 0);
    }

    public int getProcessingDelay() {
        return processingDelay.get();
    }

    public int signal() {
        int result = processingDelay.updateAndGet(value -> value > 0 ? value - 1 : 0);
        notifyProcessingWaiters();
        return result;
    }

    public boolean isQueued() {
        return queuedSendingIndex != null;
    }

    public long getQueuedSendingIndex() {
        Long value = queuedSendingIndex;
        return value == null ? 0L : value;
    }

    public void setQueuedSendingIndex(Long queuedSendingIndex) {
        this.queuedSendingIndex = queuedSendingIndex;
    }

    public Object getProcessingLock() {
        return processingLock;
    }

    public void setProcessingLock(Object processingLock) {
        if (processingLock == null) {
            throw new IllegalArgumentException("processingLock cannot be null");
        }
        this.processingLock = processingLock;
    }

    public boolean isTransmitted() {
        return transmitted.get();
    }

    public void markTransmitted() {
        transmitted.set(true);
    }

    public boolean hasExpired() {
        return hasExpired(System.currentTimeMillis());
    }

    public boolean hasExpired(long currentTime) {
        return timeout < currentTime;
    }

    public boolean isAsyncCancelled() {
        return asyncCancelled;
    }

    public void setAsyncCancelled(boolean asyncCancelled) {
        this.asyncCancelled = asyncCancelled;
        notifyProcessingWaiters();
    }

    public AsyncListenerHandler getListenerHandler() {
        return listenerHandler;
    }

    public void setListenerHandler(AsyncListenerHandler listenerHandler) {
        this.listenerHandler = listenerHandler;
    }

    Iterator<AsyncListenerHandler> getListenerTraversal() {
        return listenerTraversal;
    }

    void setListenerTraversal(Iterator<AsyncListenerHandler> listenerTraversal) {
        this.listenerTraversal = listenerTraversal;
    }

    /** Sends a released event once, preserving the old AsyncMarker contract. */
    void sendPacket(PacketEvent event) throws IOException {
        if (event == null || !transmitted.compareAndSet(false, true)) return;
        PacketStream stream = getPacketStream();
        if (stream == null || event.getPlayer() == null) {
            transmitted.set(false);
            throw new IOException("No packet stream/player is available for asynchronous transmission");
        }
        try {
            if (event.isServerPacket()) {
                stream.sendServerPacket(event.getPlayer(), event.getPacket(), event.getNetworkMarker(), false);
            } else {
                stream.receiveClientPacket(event.getPlayer(), event.getPacket(), event.getNetworkMarker(), false);
            }
        } catch (RuntimeException error) {
            transmitted.set(false);
            throw new IOException("Unable to transmit asynchronous packet", error);
        }
    }

    public int getWorkerID() {
        return workerID;
    }

    public void setWorkerID(int workerID) {
        this.workerID = workerID;
    }

    /** PacketEvents does not expose Minecraft's old packet async marker; report sync. */
    public boolean isMinecraftAsync(PacketEvent event) throws FieldAccessException {
        return false;
    }

    private void notifyProcessingWaiters() {
        Object lock = processingLock;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    public int compareTo(AsyncMarker other) {
        return other == null ? 1 : Long.compare(newSendingIndex, other.newSendingIndex);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AsyncMarker marker
                && marker.newSendingIndex == newSendingIndex;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(newSendingIndex);
    }
}
