/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.async;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Compatibility marker exposed to legacy asynchronous listeners. The packet is not held by
 * PacketEvents while async work runs, but the counter and lock remain useful to code that uses
 * the ProtocolLib coordination API.
 */
public class AsyncMarker implements Serializable, Comparable<AsyncMarker> {

    public static final int DEFAULT_TIMEOUT_DELTA = 1800 * 1000;
    public static final int DEFAULT_SENDING_DELTA = 0;

    private final long initialTime = System.currentTimeMillis();
    private final Object processingLock = new Object();
    private final AtomicInteger processingDelay = new AtomicInteger();
    private volatile boolean processed;
    private volatile boolean asyncCancelled;
    private long newSendingIndex;

    public long getInitialTime() {
        return initialTime;
    }

    public long getTimeout() {
        return initialTime + DEFAULT_TIMEOUT_DELTA;
    }

    public void setTimeout(long timeout) {
        // Kept for source/binary compatibility. PacketEvents does not queue this packet.
    }

    public int incrementProcessingDelay() {
        return processingDelay.incrementAndGet();
    }

    public int getProcessingDelay() {
        return processingDelay.get();
    }

    public int signal() {
        int current = processingDelay.get();
        return current > 0 ? processingDelay.decrementAndGet() : 0;
    }

    public Object getProcessingLock() {
        return processingLock;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isAsyncCancelled() {
        return asyncCancelled;
    }

    public void setAsyncCancelled(boolean asyncCancelled) {
        this.asyncCancelled = asyncCancelled;
    }

    public long getNewSendingIndex() {
        return newSendingIndex;
    }

    public void setNewSendingIndex(long newSendingIndex) {
        this.newSendingIndex = newSendingIndex;
    }

    @Override
    public int compareTo(AsyncMarker other) {
        return Long.compare(newSendingIndex, other == null ? 0 : other.newSendingIndex);
    }
}
