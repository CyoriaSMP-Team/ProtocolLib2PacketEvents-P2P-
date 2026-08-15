/*
 * ProtocolLib2PacketEvents - clean-room network post-processing bridge.
 */
package com.comphenix.protocol.internal;

import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketOutputHandler;
import com.comphenix.protocol.events.PacketPostListener;
import com.comphenix.protocol.events.ScheduledPacket;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/** Shared output-handler, post-listener, and scheduled-packet processing. */
public final class PacketNetworkProcessor {
    private PacketNetworkProcessor() {
    }

    /**
     * Applies byte-level output handlers to the current event buffer.  PacketEvents exposes the
     * buffer as Object specifically so this code remains independent of Netty's shaded package.
     */
    public static void applyOutputHandlers(ProtocolPacketEvent rawEvent, PacketEvent event) {
        if (rawEvent == null || event == null || !event.isServerPacket()) {
            return;
        }
        NetworkMarker marker = event.getNetworkMarker();
        if (marker == null || marker.getOutputHandlers().isEmpty()) {
            return;
        }
        Object source = rawEvent.getByteBuf();
        if (source == null) {
            source = event.getPacket().serializeToBuffer();
        }
        if (source == null) {
            throw new IllegalStateException("No encoded buffer is available for " + event.getPacketType());
        }
        byte[] bytes = ByteBufHelper.copyBytes(source);
        for (PacketOutputHandler handler : marker.getOutputHandlers()) {
            byte[] transformed = handler.handle(event, bytes);
            if (transformed == null) {
                throw new IllegalStateException("PacketOutputHandler " + handler + " returned null");
            }
            bytes = transformed;
        }
        Object replacement = PacketEvents.getAPI().getNettyManager()
                .getByteBufAllocationOperator().wrappedBuffer(bytes);
        rawEvent.setByteBuf(replacement);
        rawEvent.markForReEncode(false);
    }

    /** Invokes post listeners away from the packet callback, then schedules child packets. */
    public static void complete(PacketEvent event, PacketStream stream) {
        if (event == null) {
            return;
        }
        NetworkMarker marker = event.getNetworkMarker();
        if (marker == null) {
            return;
        }
        if (!marker.getPostListeners().isEmpty()) {
            for (PacketPostListener listener : marker.getPostListeners()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        listener.onPostEvent(event);
                    } catch (Throwable ignored) {
                        // A post listener cannot change a packet already in flight.
                    }
                });
            }
        }
        if (stream != null && !marker.getScheduledPackets().isEmpty()) {
            for (ScheduledPacket packet : new ArrayList<>(marker.getScheduledPackets())) {
                packet.schedule(stream);
            }
            marker.getScheduledPackets().clear();
        }
    }
}
