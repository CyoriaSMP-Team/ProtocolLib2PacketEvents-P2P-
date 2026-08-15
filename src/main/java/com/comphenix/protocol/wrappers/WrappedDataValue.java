/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.wrappers;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;

/** Metadata value wrapper for the 1.19.3+ DataValue shape. */
public class WrappedDataValue {
    private final EntityData<Object> handle;

    @SuppressWarnings("unchecked")
    public WrappedDataValue(Object handle) {
        if (!(handle instanceof EntityData)) {
            throw new IllegalArgumentException("Expected a PacketEvents EntityData handle");
        }
        this.handle = (EntityData<Object>) handle;
    }

    public WrappedDataValue(int index, WrappedDataWatcher.Serializer serializer, Object value) {
        this(index, serializer == null ? null : serializer.getEntityDataType(), value);
    }

    @SuppressWarnings("unchecked")
    public WrappedDataValue(int index, EntityDataType<?> serializer, Object value) {
        this.handle = new EntityData<>(index, (EntityDataType<Object>) serializer, value);
    }

    public static WrappedDataValue fromWrappedValue(int index, WrappedDataWatcher.Serializer serializer, Object value) {
        return new WrappedDataValue(index, serializer, value);
    }

    public int getIndex() {
        return handle.getIndex();
    }

    public void setIndex(int index) {
        handle.setIndex(index);
    }

    public WrappedDataWatcher.Serializer getSerializer() {
        return handle.getType() == null ? null : new WrappedDataWatcher.Serializer(handle.getType());
    }

    public void setSerializer(WrappedDataWatcher.Serializer serializer) {
        @SuppressWarnings("unchecked")
        EntityDataType<Object> type = serializer == null ? null
                : (EntityDataType<Object>) serializer.getEntityDataType();
        handle.setType(type);
    }

    public Object getValue() {
        return handle.getValue();
    }

    public Object getRawValue() {
        return handle.getValue();
    }

    public void setValue(Object value) {
        handle.setValue(value);
    }

    public void setRawValue(Object value) {
        handle.setValue(value);
    }

    public Object getHandle() {
        return handle;
    }
}
