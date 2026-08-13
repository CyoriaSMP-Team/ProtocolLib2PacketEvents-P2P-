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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ListeningWhitelist {

    public static final ListeningWhitelist EMPTY = new ListeningWhitelist(ListenerPriority.NORMAL, Collections.emptySet());

    private final ListenerPriority priority;
    private final Set<PacketType> types;

    public ListeningWhitelist(ListenerPriority priority, PacketType... types) {
        this(priority, new LinkedHashSet<>(java.util.Arrays.asList(types)));
    }

    public ListeningWhitelist(ListenerPriority priority, Set<PacketType> types) {
        this.priority = priority;
        this.types = Collections.unmodifiableSet(new LinkedHashSet<>(types));
    }

    public ListenerPriority getPriority() {
        return priority;
    }

    public Set<PacketType> getTypes() {
        return types;
    }

    public boolean isEmpty() {
        return types.isEmpty();
    }
}
