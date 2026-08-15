/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.util.Objects;

/** Chunk coordinate pair used by chunk packet APIs. */
public class ChunkCoordIntPair {
    protected final int chunkX;
    protected final int chunkZ;

    public ChunkCoordIntPair(int x, int z) {
        this.chunkX = x;
        this.chunkZ = z;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public static EquivalentConverter<ChunkCoordIntPair> getConverter() {
        return CONVERTER;
    }

    private static final EquivalentConverter<ChunkCoordIntPair> CONVERTER = new EquivalentConverter<>() {
        @Override
        public ChunkCoordIntPair getSpecific(Object generic) {
            if (generic instanceof ChunkCoordIntPair) return (ChunkCoordIntPair) generic;
            if (generic instanceof Long) {
                long key = (Long) generic;
                return new ChunkCoordIntPair(PacketWrapper.getChunkX(key), PacketWrapper.getChunkZ(key));
            }
            return null;
        }

        @Override
        public Object getGeneric(ChunkCoordIntPair specific) {
            return specific == null ? null : PacketWrapper.getChunkKey(specific.chunkX, specific.chunkZ);
        }

        @Override
        public Class<ChunkCoordIntPair> getSpecificType() {
            return ChunkCoordIntPair.class;
        }

        @Override
        public Class<?> getGenericType() {
            return long.class;
        }
    };

    @Override
    public boolean equals(Object other) {
        return other instanceof ChunkCoordIntPair
                && chunkX == ((ChunkCoordIntPair) other).chunkX
                && chunkZ == ((ChunkCoordIntPair) other).chunkZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunkX, chunkZ);
    }

    @Override
    public String toString() {
        return "ChunkCoordIntPair[x=" + chunkX + ", z=" + chunkZ + "]";
    }
}
