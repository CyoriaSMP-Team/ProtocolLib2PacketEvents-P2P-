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
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base class for packet listeners, mirroring ProtocolLib's {@code PacketAdapter}.
 * Subclasses override whichever of {@link #onPacketSending} / {@link #onPacketReceiving}
 * they care about; the other stays a no-op.
 */
public abstract class PacketAdapter implements PacketListener {

    private final Plugin plugin;
    private final ListeningWhitelist sendingWhitelist;
    private final ListeningWhitelist receivingWhitelist;

    public PacketAdapter(Plugin plugin, PacketType... types) {
        this(plugin, ListenerPriority.NORMAL, ConnectionSide.BOTH, types);
    }

    public PacketAdapter(Plugin plugin, ListenerPriority priority, PacketType... types) {
        this(plugin, priority, ConnectionSide.BOTH, types);
    }

    public PacketAdapter(Plugin plugin, ConnectionSide side, PacketType... types) {
        this(plugin, ListenerPriority.NORMAL, side, types);
    }

    public PacketAdapter(Plugin plugin, ListenerPriority priority, ConnectionSide side, PacketType... types) {
        this.plugin = plugin;

        Set<PacketType> serverTypes = new LinkedHashSet<>();
        Set<PacketType> clientTypes = new LinkedHashSet<>();
        for (PacketType type : types) {
            if (type.getSender() == PacketType.Sender.SERVER) {
                serverTypes.add(type);
            } else {
                clientTypes.add(type);
            }
        }

        this.sendingWhitelist = side.isForServer()
                ? new ListeningWhitelist(priority, serverTypes)
                : ListeningWhitelist.EMPTY;
        this.receivingWhitelist = side.isForClient()
                ? new ListeningWhitelist(priority, clientTypes)
                : ListeningWhitelist.EMPTY;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
    }

    @Override
    public ListeningWhitelist getSendingWhitelist() {
        return sendingWhitelist;
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist() {
        return receivingWhitelist;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}
