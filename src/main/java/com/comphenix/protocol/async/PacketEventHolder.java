/*
 * ProtocolLib2PacketEvents - clean-room packet ordering helper.
 */
package com.comphenix.protocol.async;

import com.comphenix.protocol.events.PacketEvent;

class PacketEventHolder implements Comparable<PacketEventHolder> {
    private final PacketEvent event;
    private final long sendingIndex;
    public PacketEventHolder(PacketEvent event) {
        if (event == null) throw new IllegalArgumentException("event cannot be null");
        this.event = event;
        AsyncMarker marker = event.getAsyncMarker();
        this.sendingIndex = marker == null ? 0L : marker.getNewSendingIndex();
    }
    public PacketEvent getEvent() { return event; }
    @Override public int compareTo(PacketEventHolder other) {
        return Long.compare(sendingIndex, other.sendingIndex);
    }
    @Override public int hashCode() { return Long.hashCode(sendingIndex); }
    @Override public boolean equals(Object other) {
        return other instanceof PacketEventHolder holder && holder.sendingIndex == sendingIndex;
    }
}
