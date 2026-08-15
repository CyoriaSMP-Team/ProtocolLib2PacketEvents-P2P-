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
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.github.retrooper.packetevents.util.Vector3i;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 * Immutable block coordinate triple, mirroring ProtocolLib's {@code BlockPosition}.
 * Backed by PacketEvents' {@link Vector3i} on the wire.
 */
public class BlockPosition {

    public static final BlockPosition ORIGIN = new BlockPosition(0, 0, 0);

    private final int x;
    private final int y;
    private final int z;

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPosition(Vector vector) {
        this(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public BlockPosition add(BlockPosition other) {
        return new BlockPosition(x + other.x, y + other.y, z + other.z);
    }

    public BlockPosition subtract(BlockPosition other) {
        return new BlockPosition(x - other.x, y - other.y, z - other.z);
    }

    public BlockPosition multiply(int factor) { return new BlockPosition(x * factor, y * factor, z * factor); }
    public BlockPosition divide(int divisor) {
        if (divisor == 0) throw new ArithmeticException("divisor is zero");
        return new BlockPosition(x / divisor, y / divisor, z / divisor);
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    public Vector3i toPacketEvents() {
        return new Vector3i(x, y, z);
    }

    public static BlockPosition fromPacketEvents(Vector3i vector) {
        return vector == null ? null : new BlockPosition(vector.getX(), vector.getY(), vector.getZ());
    }

    public static BlockPosition fromLocation(Location location) {
        return new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockPosition)) {
            return false;
        }
        BlockPosition other = (BlockPosition) o;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        return (x * 31 + y) * 31 + z;
    }

    @Override
    public String toString() {
        return "BlockPosition[x=" + x + ", y=" + y + ", z=" + z + "]";
    }

    public static EquivalentConverter<BlockPosition> getConverter() {
        return CONVERTER;
    }

    private static final EquivalentConverter<BlockPosition> CONVERTER = new EquivalentConverter<>() {
        @Override
        public BlockPosition getSpecific(Object generic) {
            return fromPacketEvents((Vector3i) generic);
        }

        @Override
        public Object getGeneric(BlockPosition specific) {
            return specific == null ? null : specific.toPacketEvents();
        }

        @Override
        public Class<BlockPosition> getSpecificType() {
            return BlockPosition.class;
        }

        @Override
        public Class<?> getGenericType() {
            return Vector3i.class;
        }
    };
}
