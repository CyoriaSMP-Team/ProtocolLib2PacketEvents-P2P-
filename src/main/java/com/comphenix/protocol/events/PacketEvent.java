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
package com.comphenix.protocol.events;

import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.error.ReportType;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.entity.Player;

import java.util.EventObject;

/**
 * A single packet being sent or received, mirroring ProtocolLib's {@code PacketEvent}.
 * <p>
 * Cancelling the event cancels the underlying packet. Modifications made through
 * {@link #getPacket()} are written back to the wire automatically.
 */
public class PacketEvent extends EventObject implements Cancellable {

    public static final ReportType REPORT_CHANGING_PACKET_TYPE_IS_CONFUSING =
            new ReportType("Changing the packet type after creation is confusing");

    private PacketContainer packet;
    private final Player player;
    private final boolean serverPacket;
    private final transient ProtocolPacketEvent handle;
    private AsyncMarker asyncMarker;
    private NetworkMarker networkMarker;
    private boolean cancelled;
    private boolean asynchronous;
    private boolean readOnly;
    private boolean filtered = true;
    private PacketEvent bundle;

    public PacketEvent(Object source) {
        super(source);
        this.packet = null;
        this.player = null;
        this.serverPacket = false;
        this.handle = null;
    }

    private PacketEvent(Object source, PacketContainer packet, Player player,
                        boolean serverPacket, ProtocolPacketEvent handle) {
        this(source, packet, null, player, serverPacket, true, null, handle);
    }

    private PacketEvent(Object source, PacketContainer packet, NetworkMarker marker, Player player,
                        boolean serverPacket, boolean filtered, PacketEvent bundle,
                        ProtocolPacketEvent handle) {
        super(source);
        if (packet == null) {
            throw new IllegalArgumentException("packet cannot be null");
        }
        this.packet = packet;
        this.player = player;
        this.serverPacket = serverPacket;
        this.networkMarker = marker;
        this.filtered = filtered;
        this.bundle = bundle;
        this.handle = handle;
    }

    private PacketEvent(PacketEvent original, AsyncMarker marker) {
        this(original.source, original.packet, original.networkMarker, original.player,
                original.serverPacket, original.filtered, original.bundle, original.handle);
        this.cancelled = original.cancelled;
        this.readOnly = original.readOnly;
        this.asyncMarker = marker;
        this.asynchronous = true;
    }

    public static PacketEvent fromServer(Object source, PacketContainer packet, Player receiver) {
        return new PacketEvent(source, packet, receiver, true, null);
    }

    public static PacketEvent fromServer(Object source, PacketContainer packet, Player receiver,
                                         ProtocolPacketEvent handle) {
        return new PacketEvent(source, packet, receiver, true, handle);
    }

    public static PacketEvent fromServer(Object source, PacketContainer packet,
                                         NetworkMarker marker, Player receiver) {
        return new PacketEvent(source, packet, marker, receiver, true, true, null, null);
    }

    public static PacketEvent fromServer(Object source, PacketContainer packet,
                                         NetworkMarker marker, Player receiver, boolean filtered) {
        return new PacketEvent(source, packet, marker, receiver, true, filtered, null, null);
    }

    public static PacketEvent fromServer(Object source, PacketContainer packet,
                                         NetworkMarker marker, Player receiver, boolean filtered,
                                         PacketEvent bundle) {
        return new PacketEvent(source, packet, marker, receiver, true, filtered, bundle, null);
    }

    public static PacketEvent fromClient(Object source, PacketContainer packet, Player sender) {
        return new PacketEvent(source, packet, sender, false, null);
    }

    public static PacketEvent fromClient(Object source, PacketContainer packet, Player sender,
                                         ProtocolPacketEvent handle) {
        return new PacketEvent(source, packet, sender, false, handle);
    }

    public static PacketEvent fromClient(Object source, PacketContainer packet,
                                         NetworkMarker marker, Player sender) {
        return new PacketEvent(source, packet, marker, sender, false, true, null, null);
    }

    public static PacketEvent fromClient(Object source, PacketContainer packet,
                                         NetworkMarker marker, Player sender, boolean filtered) {
        return new PacketEvent(source, packet, marker, sender, false, filtered, null, null);
    }

    public static PacketEvent fromSynchronous(PacketEvent event, AsyncMarker marker) {
        if (event == null || marker == null) {
            throw new IllegalArgumentException("event and marker cannot be null");
        }
        return new PacketEvent(event, marker);
    }

    public PacketContainer getPacket() {
        return packet;
    }

    public void setPacket(PacketContainer packet) {
        if (packet == null) {
            throw new IllegalArgumentException("packet cannot be null");
        }
        if (readOnly) {
            throw new IllegalStateException("The packet event is read-only.");
        }
        this.packet = packet;
    }

    @Deprecated
    public int getPacketID() {
        return packet == null ? -1 : packet.getId();
    }

    /** Convenience accessor matching ProtocolLib's {@code getPacketType()}. */
    public com.comphenix.protocol.PacketType getPacketType() {
        return packet.getType();
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isServerPacket() {
        return serverPacket;
    }

    public boolean isClientPacket() {
        return !serverPacket;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        if (readOnly) {
            throw new IllegalStateException("The packet event is read-only.");
        }
        this.cancelled = cancelled;
    }

    public boolean isAsync() {
        return asynchronous;
    }

    public void setAsync(boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    public boolean isAsynchronous() {
        return asynchronous;
    }

    /** Compatibility marker used by legacy asynchronous ProtocolLib listeners. */
    public AsyncMarker getAsyncMarker() {
        return asyncMarker;
    }

    public void setAsyncMarker(AsyncMarker asyncMarker) {
        if (asynchronous) {
            throw new IllegalStateException("The marker is immutable for asynchronous events");
        }
        if (readOnly) {
            throw new IllegalStateException("The packet event is read-only.");
        }
        this.asyncMarker = asyncMarker;
    }

    public NetworkMarker getNetworkMarker() {
        if (networkMarker == null && packet != null) {
            networkMarker = new NetworkMarker(
                    serverPacket ? ConnectionSide.SERVER_SIDE : ConnectionSide.CLIENT_SIDE,
                    packet.getType());
        }
        return networkMarker;
    }

    public void setNetworkMarker(NetworkMarker networkMarker) {
        if (networkMarker == null) {
            throw new IllegalArgumentException("marker cannot be null");
        }
        this.networkMarker = networkMarker;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void schedule(ScheduledPacket scheduled) {
        if (scheduled == null) throw new IllegalArgumentException("scheduled cannot be null");
        getNetworkMarker().getScheduledPackets().add(scheduled);
    }

    public boolean unschedule(ScheduledPacket scheduled) {
        return networkMarker != null && networkMarker.getScheduledPackets().remove(scheduled);
    }

    public PacketEvent getBundle() {
        return bundle;
    }

    /** PacketEvents only exposes Bukkit players after login, so intercepted players are real. */
    public boolean isPlayerTemporary() {
        return player instanceof com.comphenix.protocol.injector.temporary.TemporaryPlayer;
    }

    /**
     * The underlying PacketEvents event, for code that needs capabilities this compatibility
     * layer does not expose. {@code null} for events constructed outside packet interception.
     */
    public ProtocolPacketEvent getPacketEventsHandle() {
        return handle;
    }

    @Override
    public String toString() {
        return "PacketEvent[" + (serverPacket ? "server" : "client") + " " + packet.getType()
                + ", player=" + (player == null ? "none" : player.getName())
                + (cancelled ? ", cancelled" : "") + "]";
    }
}
