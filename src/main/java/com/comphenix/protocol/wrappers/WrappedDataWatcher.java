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
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import net.kyori.adventure.text.Component;

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

    /** Wraps a raw PacketEvents metadata list when supplied through an Object-typed API. */
    @SuppressWarnings("unchecked")
    public WrappedDataWatcher(Object handle) {
        this(handle instanceof List ? (List<EntityData<?>>) handle : new ArrayList<>());
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

    /** ProtocolLib-compatible object/serializer overload. */
    public void setObject(WrappedDataWatcherObject object, Object value) {
        if (object == null) {
            throw new IllegalArgumentException("data watcher object cannot be null");
        }
        setObject(object.getIndex(), object.getSerializer().getEntityDataType(), value);
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
    public Object getHandle() {
        return handle;
    }

    /** PacketEvents-typed view used internally by converters. */
    public List<EntityData<?>> getEntityDataList() {
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
            return specific == null ? null : specific.getEntityDataList();
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

    /** PacketEvents serializer wrapper retained for ProtocolLib source/binary compatibility. */
    public static class Serializer {
        private final EntityDataType<?> handle;
        private final Class<?> type;

        public Serializer(EntityDataType<?> handle) {
            this(handle, inferJavaType(handle));
        }

        public Serializer(Object handle) {
            this(handle instanceof EntityDataType ? (EntityDataType<?>) handle : null);
        }

        public Serializer(Class<?> type, Object handle, boolean supported) {
            this(handle instanceof EntityDataType ? (EntityDataType<?>) handle : null, type);
        }

        private Serializer(EntityDataType<?> handle, Class<?> type) {
            this.handle = handle;
            this.type = type;
        }

        public Object getHandle() {
            return handle;
        }

        public Class<?> getType() {
            return type;
        }

        public boolean isSupported() {
            return handle != null;
        }

        EntityDataType<?> getEntityDataType() {
            return handle;
        }
    }

    /** Registry facade for common metadata serializers. */
    public static final class Registry {
        private Registry() {
        }

        public static Serializer get(Class<?> type) {
            if (type == null) return null;
            if (type == Byte.class || type == byte.class) return new Serializer(EntityDataTypes.BYTE);
            if (type == Short.class || type == short.class) return new Serializer(EntityDataTypes.SHORT);
            if (type == Integer.class || type == int.class) return new Serializer(EntityDataTypes.INT);
            if (type == Long.class || type == long.class) return new Serializer(EntityDataTypes.LONG);
            if (type == Float.class || type == float.class) return new Serializer(EntityDataTypes.FLOAT);
            if (type == Boolean.class || type == boolean.class) return new Serializer(EntityDataTypes.BOOLEAN);
            if (type == String.class) return new Serializer(EntityDataTypes.STRING);
            if (type == Component.class) return new Serializer(EntityDataTypes.ADV_COMPONENT);
            if (type == ItemStack.class || type.getName().equals("org.bukkit.inventory.ItemStack")) {
                return new Serializer(EntityDataTypes.ITEMSTACK);
            }
            return null;
        }

        public static Serializer fromHandle(Object handle) {
            return handle instanceof EntityDataType ? new Serializer((EntityDataType<?>) handle) : null;
        }

        public static Serializer getChatComponentSerializer() {
            return new Serializer(EntityDataTypes.ADV_COMPONENT);
        }

        public static Serializer getChatComponentSerializer(boolean optional) {
            return new Serializer(optional ? EntityDataTypes.OPTIONAL_ADV_COMPONENT : EntityDataTypes.ADV_COMPONENT);
        }

        public static Serializer getItemStackSerializer(boolean optional) {
            return new Serializer(optional ? EntityDataTypes.OPTIONAL_ITEMSTACK : EntityDataTypes.ITEMSTACK);
        }
    }

    /** Pair of an entity metadata index and its serializer. */
    public static class WrappedDataWatcherObject {
        private int index;
        private Serializer serializer;

        public WrappedDataWatcherObject(int index, Serializer serializer) {
            this.index = index;
            this.serializer = serializer;
        }

        public int getIndex() {
            return index;
        }

        public Serializer getSerializer() {
            return serializer;
        }

        public Object getHandle() {
            return this;
        }
    }

    private static Class<?> inferJavaType(EntityDataType<?> type) {
        if (type == EntityDataTypes.BYTE) return Byte.class;
        if (type == EntityDataTypes.SHORT) return Short.class;
        if (type == EntityDataTypes.INT) return Integer.class;
        if (type == EntityDataTypes.LONG) return Long.class;
        if (type == EntityDataTypes.FLOAT) return Float.class;
        if (type == EntityDataTypes.BOOLEAN) return Boolean.class;
        if (type == EntityDataTypes.STRING || type == EntityDataTypes.COMPONENT) return String.class;
        if (type == EntityDataTypes.ADV_COMPONENT) return Component.class;
        return Object.class;
    }
}
