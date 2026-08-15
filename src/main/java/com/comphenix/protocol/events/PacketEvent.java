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
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import org.bukkit.entity.Player;

import java.util.EventObject;

/**
 * A single packet being sent or received, mirroring ProtocolLib's {@code PacketEvent}.
 * <p>
 * Cancelling the event cancels the underlying packet. Modifications made through
 * {@link #getPacket()} are written back to the wire automatically.
 */
public class PacketEvent extends EventObject {

    private final PacketContainer packet;
    private final Player player;
    private final boolean serverPacket;
    private final transient ProtocolPacketEvent handle;
    private final AsyncMarker asyncMarker = new AsyncMarker();
    private boolean cancelled;
    private boolean asynchronous;

    private PacketEvent(Object source, PacketContainer packet, Player player,
                        boolean serverPacket, ProtocolPacketEvent handle) {
        super(source);
        this.packet = packet;
        this.player = player;
        this.serverPacket = serverPacket;
        this.handle = handle;
    }

    public static PacketEvent fromServer(Object source, PacketContainer packet, Player receiver) {
        return new PacketEvent(source, packet, receiver, true, null);
    }

    public static PacketEvent fromServer(Object source, PacketContainer packet, Player receiver,
                                         ProtocolPacketEvent handle) {
        return new PacketEvent(source, packet, receiver, true, handle);
    }

    public static PacketEvent fromClient(Object source, PacketContainer packet, Player sender) {
        return new PacketEvent(source, packet, sender, false, null);
    }

    public static PacketEvent fromClient(Object source, PacketContainer packet, Player sender,
                                         ProtocolPacketEvent handle) {
        return new PacketEvent(source, packet, sender, false, handle);
    }

    public PacketContainer getPacket() {
        return packet;
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
        this.cancelled = cancelled;
    }

    public boolean isAsync() {
        return asynchronous;
    }

    public void setAsync(boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    /** Compatibility marker used by legacy asynchronous ProtocolLib listeners. */
    public AsyncMarker getAsyncMarker() {
        return asyncMarker;
    }

    /** PacketEvents only exposes Bukkit players after login, so intercepted players are real. */
    public boolean isPlayerTemporary() {
        return false;
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
