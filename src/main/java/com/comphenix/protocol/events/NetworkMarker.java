/*
 * ProtocolLib2PacketEvents - clean-room network marker contract.
 */
package com.comphenix.protocol.events;

import com.comphenix.protocol.PacketType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Metadata and post-processing state attached to one network packet. */
public class NetworkMarker {
    private final PacketType type;
    private final ConnectionSide side;
    private boolean requireOutputHeader;
    private final Set<PacketPostListener> postListeners = new LinkedHashSet<>();
    private final Deque<ScheduledPacket> scheduledPackets = new ArrayDeque<>();
    private final List<PacketOutputHandler> outputHandlers = new ArrayList<>();

    public NetworkMarker(ConnectionSide side, PacketType type) {
        if (side == null) {
            throw new IllegalArgumentException("side cannot be null");
        }
        this.side = side;
        this.type = type;
    }

    /** Convenience marker for callers that do not have a packet type yet. */
    public NetworkMarker() {
        this(ConnectionSide.BOTH, null);
    }

    public static boolean hasPostListeners(NetworkMarker marker) {
        return marker != null && !marker.getPostListeners().isEmpty();
    }

    public static NetworkMarker getNetworkMarker(PacketEvent event) {
        return event == null ? null : event.getNetworkMarker();
    }

    public static Deque<ScheduledPacket> readScheduledPackets(NetworkMarker marker) {
        return marker == null ? null : marker.scheduledPackets;
    }

    public ConnectionSide getSide() {
        return side;
    }

    public PacketType getType() {
        return type;
    }

    public boolean addPostListener(PacketPostListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        return postListeners.add(listener);
    }

    public boolean removePostListener(PacketPostListener listener) {
        return postListeners.remove(listener);
    }

    public Set<PacketPostListener> getPostListeners() {
        return Collections.unmodifiableSet(postListeners);
    }

    public Deque<ScheduledPacket> getScheduledPackets() {
        return scheduledPackets;
    }

    public void requireOutputHeader() {
        requireOutputHeader = true;
    }

    public boolean isOutputHeaderRequired() {
        return requireOutputHeader;
    }

    /** P2P extension used by the raw fallback backend. */
    public boolean addOutputHandler(PacketOutputHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }
        outputHandlers.add(handler);
        outputHandlers.sort((left, right) -> Integer.compare(
                right.getPriority().ordinal(), left.getPriority().ordinal()));
        return true;
    }

    public boolean removeOutputHandler(PacketOutputHandler handler) {
        return outputHandlers.remove(handler);
    }

    public List<PacketOutputHandler> getOutputHandlers() {
        return Collections.unmodifiableList(outputHandlers);
    }
}
