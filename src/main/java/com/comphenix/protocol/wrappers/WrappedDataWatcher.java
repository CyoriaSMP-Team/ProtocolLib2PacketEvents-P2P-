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
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity metadata container, mirroring ProtocolLib's {@code WrappedDataWatcher}.
 * <p>
 * ProtocolLib wraps NMS's {@code DataWatcher}; this wraps the {@code List<EntityData>} that
 * PacketEvents' entity metadata wrappers actually hold, so it works across every Minecraft
 * version PacketEvents supports without any version-specific field lookups.
 * <p>
 * One deliberate difference from ProtocolLib: because PacketEvents stores the serializer type
 * alongside each entry, writing a <em>new</em> index requires knowing the type. Use
 * {@link #setObject(int, EntityDataType, Object)} for new indices; {@link #setObject(int, Object)}
 * infers the type from the Java class of the value and is intended for updating entries that
 * already exist.
 */
public class WrappedDataWatcher implements Iterable<WrappedWatchableObject> {

    private final List<EntityData<?>> handle;

    public WrappedDataWatcher() {
        this(new ArrayList<>());
    }

    public WrappedDataWatcher(List<EntityData<?>> handle) {
        this.handle = handle;
    }

    /** Wraps the metadata list held by a PacketEvents wrapper, sharing storage with it. */
    public static WrappedDataWatcher fromHandle(List<EntityData<?>> handle) {
        return handle == null ? null : new WrappedDataWatcher(handle);
    }

    /** The value at the given metadata index, or {@code null} if the index is not present. */
    public Object getObject(int index) {
        EntityData<?> data = find(index);
        return data == null ? null : data.getValue();
    }

    /** The full entry at the given index, or {@code null} if absent. */
    public WrappedWatchableObject getWatchableObject(int index) {
        EntityData<?> data = find(index);
        return data == null ? null : new WrappedWatchableObject(data);
    }

    public boolean hasIndex(int index) {
        return find(index) != null;
    }

    /**
     * Sets the value at an index, inferring the PacketEvents serializer from the value's Java
     * type when the index does not exist yet.
     *
     * @throws IllegalArgumentException if the index is new and the type cannot be inferred
     */
    @SuppressWarnings("unchecked")
    public WrappedDataWatcher setObject(int index, Object value) {
        EntityData<?> existing = find(index);
        if (existing != null) {
            ((EntityData<Object>) existing).setValue(value);
            return this;
        }
        EntityDataType<?> type = inferType(value);
        if (type == null) {
            throw new IllegalArgumentException("Cannot infer an entity data type for "
                    + (value == null ? "null" : value.getClass().getName()) + " at index " + index
                    + "; use setObject(int, EntityDataType, Object) to state the type explicitly");
        }
        return setObject(index, type, value);
    }

    /** Sets the value at an index with an explicit PacketEvents serializer type. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public WrappedDataWatcher setObject(int index, EntityDataType<?> type, Object value) {
        EntityData<?> existing = find(index);
        if (existing != null) {
            ((EntityData<Object>) existing).setType((EntityDataType) type);
            ((EntityData<Object>) existing).setValue(value);
            return this;
        }
        handle.add(new EntityData(index, type, value));
        return this;
    }

    public WrappedDataWatcher remove(int index) {
        handle.removeIf(data -> data.getIndex() == index);
        return this;
    }

    public int size() {
        return handle.size();
    }

    /** Every entry, keyed by metadata index. */
    public Map<Integer, WrappedWatchableObject> asMap() {
        Map<Integer, WrappedWatchableObject> out = new LinkedHashMap<>();
        for (EntityData<?> data : handle) {
            out.put(data.getIndex(), new WrappedWatchableObject(data));
        }
        return out;
    }

    public List<WrappedWatchableObject> getWatchableObjects() {
        List<WrappedWatchableObject> out = new ArrayList<>(handle.size());
        for (EntityData<?> data : handle) {
            out.add(new WrappedWatchableObject(data));
        }
        return out;
    }

    /** The live PacketEvents list. Mutating it mutates the packet. */
    public List<EntityData<?>> getHandle() {
        return handle;
    }

    @Override
    public Iterator<WrappedWatchableObject> iterator() {
        return getWatchableObjects().iterator();
    }

    @Override
    public String toString() {
        return "WrappedDataWatcher" + asMap();
    }

    private EntityData<?> find(int index) {
        for (EntityData<?> data : handle) {
            if (data.getIndex() == index) {
                return data;
            }
        }
        return null;
    }

    /**
     * Maps a Java value onto the PacketEvents serializer for it. Only the unambiguous
     * primitive-ish types are inferred - anything where several entity data types share a Java
     * class (components, optionals, item stacks, particles, ...) returns null so the caller is
     * forced to be explicit rather than silently picking the wrong serializer.
     */
    private static EntityDataType<?> inferType(Object value) {
        if (value instanceof Byte) return EntityDataTypes.BYTE;
        if (value instanceof Short) return EntityDataTypes.SHORT;
        if (value instanceof Integer) return EntityDataTypes.INT;
        if (value instanceof Long) return EntityDataTypes.LONG;
        if (value instanceof Float) return EntityDataTypes.FLOAT;
        if (value instanceof Boolean) return EntityDataTypes.BOOLEAN;
        if (value instanceof String) return EntityDataTypes.STRING;
        return null;
    }

    public static EquivalentConverter<WrappedDataWatcher> getConverter() {
        return CONVERTER;
    }

    private static final EquivalentConverter<WrappedDataWatcher> CONVERTER = new EquivalentConverter<>() {
        @Override
        @SuppressWarnings("unchecked")
        public WrappedDataWatcher getSpecific(Object generic) {
            return fromHandle((List<EntityData<?>>) generic);
        }

        @Override
        public Object getGeneric(WrappedDataWatcher specific) {
            return specific == null ? null : specific.getHandle();
        }

        @Override
        public Class<WrappedDataWatcher> getSpecificType() {
            return WrappedDataWatcher.class;
        }

        @Override
        public Class<?> getGenericType() {
            return List.class;
        }
    };
}
