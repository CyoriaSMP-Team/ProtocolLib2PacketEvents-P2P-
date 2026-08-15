package com.comphenix.protocol.injector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;

public interface ListenerManager {
    boolean hasInboundListener(PacketType type);
    boolean hasOutboundListener(PacketType type);
    boolean hasMainThreadListener(PacketType type);
    void invokeInboundPacketListeners(PacketEvent event);
    void invokeOutboundPacketListeners(PacketEvent event);

    /**
     * Dispatch a packet that was intercepted before PacketEvents could decode it. Implementations
     * with the full ProtocolManager bridge override this to include async ordering and post
     * processing; older integrations retain the simple listener-manager behavior by default.
     */
    default boolean dispatchInboundPacket(PacketEvent event) {
        invokeInboundPacketListeners(event);
        return event == null || !event.isCancelled();
    }

    default boolean dispatchOutboundPacket(PacketEvent event) {
        invokeOutboundPacketListeners(event);
        return event == null || !event.isCancelled();
    }
}
