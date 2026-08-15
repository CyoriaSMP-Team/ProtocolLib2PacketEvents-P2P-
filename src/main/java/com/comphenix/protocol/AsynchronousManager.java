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
package com.comphenix.protocol;

import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

import java.util.Set;

/**
 * Asynchronous packet processing, mirroring ProtocolLib's {@code AsynchronousManager}.
 * <p>
 * Listeners registered here run on a worker pool instead of the Netty thread, so they may do
 * blocking work (database lookups, HTTP calls) without stalling the connection. Packets for a
 * given player are processed strictly in order, so a slow listener delays that player's later
 * packets rather than letting them overtake.
 * <p>
 * There is one important semantic difference from real ProtocolLib, forced by how PacketEvents
 * intercepts packets - see {@code injector.AsynchronousManagerImpl} and the README: the packet
 * is <em>not</em> held on the wire while the async listener runs. Async listeners here observe
 * packets and can act on them, but cannot retroactively cancel or mutate one that has already
 * been sent. Use a synchronous listener when the decision must affect the packet itself.
 */
public interface AsynchronousManager {

    /** Registers a listener to be run off the network thread. */
    AsyncListenerHandler registerAsyncHandler(PacketListener listener);

    /** Removes a previously registered asynchronous handler. */
    default void unregisterAsyncHandler(AsyncListenerHandler handler) {
        if (handler != null) {
            unregisterAsyncHandler(handler.getAsyncListener());
        }
    }

    void unregisterAsyncHandler(PacketListener listener);

    void unregisterAsyncHandlers(Plugin plugin);

    /** Compatibility hook for ProtocolLib listeners that release a packet asynchronously. */
    default void signalPacketTransmission(PacketEvent packet) {
        if (packet != null && packet.getAsyncMarker() != null) {
            packet.getAsyncMarker().signal();
        }
    }

    /** Every currently registered asynchronous listener. */
    Set<PacketListener> getAsyncHandlers();

    /** Packets queued but not yet processed by the worker pool. */
    int getQueuedPacketCount();

    /** Stops the worker pool. Called on plugin disable. */
    void shutdown();
}
