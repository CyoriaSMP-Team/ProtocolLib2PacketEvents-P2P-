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

/**
 * Which direction a listener sees, mirroring ProtocolLib's {@code ConnectionSide}.
 * Constant names match ProtocolLib exactly ({@code SERVER}/{@code CLIENT}/{@code BOTH})
 * so plugins compiled against real ProtocolLib link unchanged.
 */
public enum ConnectionSide {
    SERVER,
    CLIENT,
    BOTH;

    public boolean isForServer() {
        return this == SERVER || this == BOTH;
    }

    public boolean isForClient() {
        return this == CLIENT || this == BOTH;
    }
}
