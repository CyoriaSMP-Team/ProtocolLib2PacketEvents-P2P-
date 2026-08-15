package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import java.io.DataOutput;

/** Logical leaf wrapper. Native handles can be attached by a version adapter. */
class WrappedElement<TType> implements NbtWrapper<TType> {
    private String name;
    private final NbtType type;
    private TType value;
    private final Object nativeHandle;

    WrappedElement(NbtType type, String name, TType value) {
        if (type == null || type == NbtType.TAG_END) {
            throw new IllegalArgumentException("Invalid NBT type");
        }
        this.type = type;
        this.name = name == null ? "" : name;
        this.value = value;
        this.nativeHandle = null;
    }

    public WrappedElement(Object handle) {
        this(handle, "");
    }

    @SuppressWarnings("unchecked")
    public WrappedElement(Object handle, String name) {
        if (handle instanceof WrappedElement<?>) {
            WrappedElement<?> other = (WrappedElement<?>) handle;
            this.type = other.type;
            this.value = (TType) other.value;
            this.nativeHandle = other.nativeHandle;
            this.name = name == null ? other.name : name;
            return;
        }
        if (handle instanceof NbtBase<?>) {
            NbtBase<?> other = (NbtBase<?>) handle;
            this.type = other.getType();
            this.value = (TType) other.getValue();
            this.nativeHandle = other.getHandle();
            this.name = name == null ? other.getName() : name;
            return;
        }
        this.nativeHandle = handle;
        this.name = name == null ? "" : name;
        try {
            this.type = NbtType.getTypeFromClass(handle.getClass());
            this.value = (TType) handle;
        } catch (RuntimeException unsupported) {
            throw new IllegalArgumentException("Unsupported logical NBT handle: " + handle.getClass(), unsupported);
        }
    }

    @Override
    public boolean accept(NbtVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public Object getHandle() {
        return nativeHandle == null ? this : nativeHandle;
    }

    @Override
    public NbtType getType() {
        return type;
    }

    public NbtType getSubType() { return type; }
    public void setSubType(NbtType ignored) { }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    @Override
    public TType getValue() {
        return value;
    }

    @Override
    public void setValue(TType newValue) {
        this.value = newValue;
    }

    @Override
    public NbtWrapper<TType> deepClone() {
        @SuppressWarnings("unchecked")
        TType copy = (TType) NbtValues.copyValue(value);
        return new WrappedElement<>(type, name, copy);
    }

    @Override
    public void write(DataOutput destination) {
        NbtBinarySerializer.DEFAULT.serialize(this, destination);
    }

    @Override
    public String toString() {
        return name + "=" + String.valueOf(value);
    }

    @Override public int hashCode() { return java.util.Objects.hash(type, name, value); }
    @Override public boolean equals(Object other) {
        if (!(other instanceof NbtBase<?> value)) return false;
        return type == value.getType() && java.util.Objects.equals(name, value.getName())
                && java.util.Objects.deepEquals(this.value, value.getValue());
    }
}
