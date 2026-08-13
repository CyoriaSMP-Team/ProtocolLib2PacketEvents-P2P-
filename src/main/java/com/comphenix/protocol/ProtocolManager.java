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

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.utility.MinecraftVersion;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Set;

/** Packet interception and injection entry point, mirroring ProtocolLib's {@code ProtocolManager}. */
public interface ProtocolManager {

    void addPacketListener(PacketListener listener);

    void removePacketListener(PacketListener listener);

    void removePacketListeners(Plugin plugin);

    /** Every currently registered listener. */
    List<PacketListener> getPacketListeners();

    /** Sends {@code packet}, running it back through other registered packet listeners first. */
    void sendServerPacket(Player receiver, PacketContainer packet);

    /** Sends {@code packet}; when {@code filters} is false the packet bypasses other listeners. */
    void sendServerPacket(Player receiver, PacketContainer packet, boolean filters);

    /** Sends {@code packet} to every online player. */
    void broadcastServerPacket(PacketContainer packet);

    /** Injects {@code packet} as though the client had sent it. */
    void receiveClientPacket(Player sender, PacketContainer packet);

    /** Allocates an empty packet of the given type, ready to be filled in and sent. */
    PacketContainer createPacket(PacketType type);

    /** Every packet type at least one listener is currently subscribed to. */
    Set<PacketType> getListeningTypes();

    /** Resolves an entity by its network id within a world. */
    Entity getEntityFromID(World world, int entityId);

    MinecraftVersion getMinecraftVersion();
}
