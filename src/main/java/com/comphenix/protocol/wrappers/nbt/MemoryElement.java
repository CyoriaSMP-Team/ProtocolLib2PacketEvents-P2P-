package com.comphenix.protocol.wrappers.nbt;

import java.util.Arrays;

/** Small value-only NBT implementation used when no NMS handle is available. */
class MemoryElement<TType> implements NbtBase<TType> {
    private String name;
    private TType value;
    private final NbtType type;

    public MemoryElement(String name, TType value) {
        this(name, value, NbtType.getTypeFromClass(value.getClass()));
    }

    public MemoryElement(String name, TType value, NbtType type) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be NULL.");
        }
        if (type == null || type == NbtType.TAG_END) {
            throw new IllegalArgumentException("Type cannot be NULL or TAG_END.");
        }
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public boolean accept(NbtVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public NbtType getType() {
        return type;
    }

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
    public NbtBase<TType> deepClone() {
        @SuppressWarnings("unchecked")
        TType copy = (TType) NbtValues.copyValue(value);
        return new MemoryElement<>(name, copy, type);
    }

    @Override
    public String toString() {
        return name + "=" + String.valueOf(value);
    }
}
