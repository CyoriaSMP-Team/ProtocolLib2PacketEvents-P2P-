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

import com.github.retrooper.packetevents.event.PacketListenerPriority;

/**
 * Mirrors ProtocolLib's listener priority scale. PacketEvents has an extra HIGHEST
 * bucket between HIGH and MONITOR; ProtocolLib's HIGH maps onto PacketEvents' HIGH
 * (not HIGHEST), so a plugin marked HIGH here still runs before PacketEvents' own
 * internal state-tracking listener but after everything at NORMAL and below.
 */
public enum ListenerPriority {
    LOWEST(0, PacketListenerPriority.LOWEST),
    LOW(1, PacketListenerPriority.LOW),
    NORMAL(2, PacketListenerPriority.NORMAL),
    HIGH(3, PacketListenerPriority.HIGH),
    HIGHEST(4, PacketListenerPriority.HIGHEST),
    MONITOR(5, PacketListenerPriority.MONITOR);

    private final int slot;
    private final PacketListenerPriority packetEventsPriority;

    ListenerPriority(int slot, PacketListenerPriority packetEventsPriority) {
        this.slot = slot;
        this.packetEventsPriority = packetEventsPriority;
    }

    public PacketListenerPriority toPacketEvents() {
        return packetEventsPriority;
    }

    public int getSlot() {
        return slot;
    }
}
