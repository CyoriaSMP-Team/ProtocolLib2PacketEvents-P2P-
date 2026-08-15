package com.comphenix.protocol.async;

import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Ordered release queue for asynchronous packet events.
 *
 * <p>Events remain queued until their marker is processed, cancelled or
 * expired. The queue never silently discards a delayed packet: expiration is
 * handed to the timeout hook and the packet is then either released or
 * dropped according to the marker state.</p>
 */
abstract class PacketSendingQueue {
    public static final int INITIAL_CAPACITY = 10;
    protected final PriorityBlockingQueue<PacketEventHolder> sendingQueue = new PriorityBlockingQueue<>(INITIAL_CAPACITY);
    protected final boolean notThreadSafe;
    protected final Executor asynchronousSender;
    protected volatile boolean cleanedUp;

    public PacketSendingQueue(boolean notThreadSafe, Executor asynchronousSender) {
        this.notThreadSafe = notThreadSafe;
        this.asynchronousSender = asynchronousSender == null ? Runnable::run : asynchronousSender;
    }

    public int size() { return sendingQueue.size(); }
    public void enqueue(PacketEvent packet) {
        if (packet == null) throw new IllegalArgumentException("packet cannot be null");
        if (!cleanedUp) sendingQueue.offer(new PacketEventHolder(packet));
    }

    public synchronized void signalPacketUpdate(PacketEvent packetUpdated, boolean onMainThread) {
        if (packetUpdated == null) return;
        AsyncMarker marker = packetUpdated.getAsyncMarker();
        if (marker == null) {
            try { marker = new AsyncMarker(); packetUpdated.setAsyncMarker(marker); }
            catch (IllegalStateException ignored) { }
        }
        if (marker != null && marker.getQueuedSendingIndex() != marker.getNewSendingIndex()
                && !marker.hasExpired()) {
            PacketEvent copy = PacketEvent.fromSynchronous(packetUpdated, marker);
            packetUpdated.setReadOnly(false);
            packetUpdated.setCancelled(true);
            enqueue(copy);
        }
        if (marker != null) marker.setProcessed(true);
        trySendPackets(onMainThread);
    }

    public synchronized void signalPacketUpdate(List<PacketType> packets, boolean onMainThread) {
        if (packets == null || packets.isEmpty()) return;
        Set<PacketType> lookup = new HashSet<>(packets);
        for (PacketEventHolder holder : sendingQueue) {
            if (lookup.contains(holder.getEvent().getPacketType())) {
                AsyncMarker marker = holder.getEvent().getAsyncMarker();
                if (marker != null) marker.setProcessed(true);
            }
        }
        trySendPackets(onMainThread);
    }

    public synchronized void trySendPackets(boolean onMainThread) {
        if (cleanedUp) return;
        while (true) {
            PacketEventHolder holder = sendingQueue.poll();
            if (holder == null) return;
            if (!processPacketHolder(onMainThread, holder)) {
                sendingQueue.offer(holder);
                return;
            }
        }
    }

    private boolean processPacketHolder(boolean onMainThread, PacketEventHolder holder) {
        PacketEvent event = holder.getEvent();
        AsyncMarker marker = event.getAsyncMarker();
        if (marker == null) {
            sendPacket(event);
            return true;
        }

        boolean expired = marker.hasExpired();
        if ((!marker.isProcessed() || marker.getProcessingDelay() > 0) && !expired) return false;
        if (expired) {
            onPacketTimeout(event);
            marker = event.getAsyncMarker();
            expired = marker == null || marker.hasExpired();
            if (marker != null && !marker.isProcessed() && !expired) return false;
        }
        if (event.isCancelled() || expired || event.getPlayer() == null || !event.getPlayer().isOnline()) return true;

        if (notThreadSafe) {
            try {
                boolean wantAsync = marker.isMinecraftAsync(event);
                if (!onMainThread && !wantAsync) return false;
                if (onMainThread && wantAsync) {
                    AsyncMarker finalMarker = marker;
                    asynchronousSender.execute(() -> sendIfNeeded(event, finalMarker));
                    return true;
                }
            } catch (FieldAccessException error) {
                return true;
            }
        }
        sendIfNeeded(event, marker);
        return true;
    }

    private void sendIfNeeded(PacketEvent event, AsyncMarker marker) {
        if (event.isCancelled() || marker.isTransmitted()) return;
        try { marker.sendPacket(event); }
        catch (IOException error) { onPacketSendFailure(event, error); }
    }

    private void sendPacket(PacketEvent event) {
        PacketStream stream = null;
        try { stream = ProtocolLibrary.getProtocolManager(); }
        catch (IllegalStateException ignored) { }
        if (stream == null || event.getPlayer() == null) return;
        if (event.isServerPacket()) stream.sendServerPacket(event.getPlayer(), event.getPacket(), false);
        else stream.receiveClientPacket(event.getPlayer(), event.getPacket(), false);
    }

    protected void onPacketSendFailure(PacketEvent event, IOException error) { }
    protected abstract void onPacketTimeout(PacketEvent event);

    public boolean isSynchronizeMain() { return notThreadSafe; }

    public synchronized void cleanupAll() {
        if (cleanedUp) return;
        cleanedUp = true;
        while (true) {
            PacketEventHolder holder = sendingQueue.poll();
            if (holder == null) break;
            PacketEvent event = holder.getEvent();
            AsyncMarker marker = event.getAsyncMarker();
            if (marker != null) marker.setProcessed(true);
            if (!event.isCancelled()) sendPacket(event);
        }
    }
}
