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

import com.comphenix.protocol.PacketType;

/**
 * Which direction a listener sees, mirroring ProtocolLib's {@code ConnectionSide}.
 * Constant names match ProtocolLib exactly. The short aliases are retained for
 * compatibility with the first P2P release.
 */
public enum ConnectionSide {
    SERVER_SIDE,
    CLIENT_SIDE,
    BOTH;

    @Deprecated public static final ConnectionSide SERVER = SERVER_SIDE;
    @Deprecated public static final ConnectionSide CLIENT = CLIENT_SIDE;

    public boolean isForServer() {
        return this == SERVER_SIDE || this == BOTH;
    }

    public boolean isForClient() {
        return this == CLIENT_SIDE || this == BOTH;
    }

    public PacketType.Sender getSender() {
        if (this == SERVER_SIDE) return PacketType.Sender.SERVER;
        if (this == CLIENT_SIDE) return PacketType.Sender.CLIENT;
        return null;
    }

    public static ConnectionSide add(ConnectionSide first, ConnectionSide second) {
        if (first == null) return second;
        if (second == null) return first;
        boolean server = first.isForServer() || second.isForServer();
        boolean client = first.isForClient() || second.isForClient();
        return server && client ? BOTH : server ? SERVER_SIDE : CLIENT_SIDE;
    }
}
