/* ProtocolLib2PacketEvents - logical entity metadata entry. */
package com.comphenix.protocol.wrappers;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import java.util.Objects;

/** ProtocolLib-compatible metadata entry backed by PacketEvents' EntityData. */
public final class WrappedWatchableObject extends AbstractWrapper {
    private final EntityData<Object> entityData;
    private boolean dirty;

    @SuppressWarnings("unchecked")
    public WrappedWatchableObject(Object handle) {
        super(EntityData.class);
        if (handle instanceof WrappedWatchableObject watchable) {
            this.entityData = watchable.entityData;
            this.dirty = watchable.dirty;
        } else if (handle instanceof EntityData<?> data) {
            this.entityData = (EntityData<Object>) data;
        } else {
            throw new IllegalArgumentException("Expected PacketEvents EntityData handle, got "
                    + (handle == null ? "null" : handle.getClass().getName()));
        }
        this.handle = this.entityData;
    }

    public WrappedWatchableObject(int index, Object value) {
        this(index, inferType(value), value);
    }

    public WrappedWatchableObject(WrappedDataWatcher.WrappedDataWatcherObject watcherObject,
                                  Object value) {
        this(watcherObject == null ? -1 : watcherObject.getIndex(),
                watcherObject == null || watcherObject.getSerializer() == null
                        ? inferType(value) : watcherObject.getSerializer().getEntityDataType(), value);
        if (watcherObject == null) throw new IllegalArgumentException("watcherObject cannot be null");
    }

    @SuppressWarnings("unchecked")
    private WrappedWatchableObject(int index, EntityDataType<?> type, Object value) {
        super(EntityData.class);
        if (index < 0) throw new IllegalArgumentException("metadata index cannot be negative");
        if (type == null) throw new IllegalArgumentException("metadata serializer cannot be inferred");
        this.entityData = new EntityData<>(index, (EntityDataType<Object>) type, value);
        this.handle = this.entityData;
        this.dirty = true;
    }

    public WrappedDataWatcher.WrappedDataWatcherObject getWatcherObject() {
        return new WrappedDataWatcher.WrappedDataWatcherObject(getIndex(),
                new WrappedDataWatcher.Serializer(entityData.getType()));
    }

    public int getIndex() { return entityData.getIndex(); }
    public Object getValue() { return entityData.getValue(); }
    public void setValue(Object value) { setValue(value, true); }
    public Object getRawValue() { return entityData.getValue(); }

    public void setValue(Object value, boolean update) {
        entityData.setValue(value);
        if (update) dirty = true;
    }

    public boolean getDirtyState() { return dirty; }
    public void setDirtyState(boolean state) { dirty = state; }
    public EntityDataType<?> getType() { return entityData.getType(); }
    public EntityData<?> getEntityData() { return entityData; }
    @Override public Object getHandle() { return entityData; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof WrappedWatchableObject value)) return false;
        return getIndex() == value.getIndex() && Objects.equals(getType(), value.getType())
                && Objects.equals(getValue(), value.getValue());
    }

    @Override public int hashCode() { return Objects.hash(getIndex(), getType(), getValue()); }

    @Override
    public String toString() {
        return "WrappedWatchableObject[index=" + getIndex() + ", value=" + getValue()
                + ", dirty=" + dirty + "]";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static EntityData<Object> copy(EntityData<?> source) {
        Object value = source.getValue();
        if (value instanceof byte[] bytes) value = bytes.clone();
        if (value instanceof int[] ints) value = ints.clone();
        if (value instanceof long[] longs) value = longs.clone();
        return new EntityData(source.getIndex(), source.getType(), value);
    }

    private static EntityDataType<?> inferType(Object value) {
        if (value instanceof Byte) return EntityDataTypes.BYTE;
        if (value instanceof Short) return EntityDataTypes.SHORT;
        if (value instanceof Integer) return EntityDataTypes.INT;
        if (value instanceof Long) return EntityDataTypes.LONG;
        if (value instanceof Float) return EntityDataTypes.FLOAT;
        if (value instanceof Boolean) return EntityDataTypes.BOOLEAN;
        if (value instanceof String) return EntityDataTypes.STRING;
        throw new IllegalArgumentException("Cannot infer entity data serializer for "
                + (value == null ? "null" : value.getClass().getName()));
    }
}
