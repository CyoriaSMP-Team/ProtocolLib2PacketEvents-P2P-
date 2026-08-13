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
package com.comphenix.protocol.utility;

import com.github.retrooper.packetevents.manager.server.ServerVersion;

/**
 * ProtocolLib-shaped view of the running server's Minecraft version, derived from
 * PacketEvents' own {@link ServerVersion} - PacketEvents does the actual version
 * detection, this just re-exposes it under ProtocolLib's API shape.
 */
public class MinecraftVersion implements Comparable<MinecraftVersion> {

    private final ServerVersion handle;

    public MinecraftVersion(ServerVersion handle) {
        this.handle = handle;
    }

    public static MinecraftVersion current() {
        return new MinecraftVersion(com.github.retrooper.packetevents.PacketEvents.getAPI().getServerManager().getVersion());
    }

    public String getVersion() {
        return handle.getReleaseName();
    }

    public int getProtocolVersion() {
        return handle.getProtocolVersion();
    }

    public ServerVersion toPacketEvents() {
        return handle;
    }

    public boolean isAtLeast(MinecraftVersion other) {
        return handle.isNewerThanOrEquals(other.handle);
    }

    @Override
    public int compareTo(MinecraftVersion other) {
        return Integer.compare(handle.getProtocolVersion(), other.handle.getProtocolVersion());
    }

    @Override
    public String toString() {
        return handle.getReleaseName();
    }
}
