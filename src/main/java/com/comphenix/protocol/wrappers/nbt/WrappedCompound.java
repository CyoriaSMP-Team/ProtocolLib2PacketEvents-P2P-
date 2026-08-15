package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import java.io.DataOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/** In-memory compound implementation used by the clean-room NBT backend. */
class WrappedCompound implements NbtWrapper<Map<String, NbtBase<?>>>, NbtCompound {
    private String name;
    private final Map<String, NbtBase<?>> values = new LinkedHashMap<>();
    private final Object nativeHandle;

    public static WrappedCompound fromName(String name) {
        return new WrappedCompound(name);
    }

    public static NbtCompound fromList(String name, Collection<? extends NbtBase<?>> entries) {
        WrappedCompound result = fromName(name);
        if (entries != null) {
            for (NbtBase<?> entry : entries) {
                result.put(entry);
            }
        }
        return result;
    }

    private WrappedCompound(String name) {
        this.name = name == null ? "" : name;
        this.nativeHandle = null;
    }

    public WrappedCompound(Object handle) {
        this(handle, "");
    }

    @SuppressWarnings("unchecked")
    public WrappedCompound(Object handle, String name) {
        this.name = name == null ? "" : name;
        this.nativeHandle = handle instanceof NbtCompound ? ((NbtCompound) handle).getHandle() : handle;
        if (handle instanceof NbtCompound) {
            setValue(((NbtCompound) handle).getValue());
        } else if (handle instanceof Map<?, ?>) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) handle).entrySet()) {
                putObject(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
    }

    @Override
    public boolean accept(NbtVisitor visitor) {
        if (visitor.visitEnter(this)) {
            for (NbtBase<?> entry : values.values()) {
                if (!entry.accept(visitor)) {
                    break;
                }
            }
        }
        return visitor.visitLeave(this);
    }

    @Override
    public Object getHandle() {
        return nativeHandle == null ? this : nativeHandle;
    }

    @Override
    public NbtType getType() {
        return NbtType.TAG_COMPOUND;
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
    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    @Override
    public Set<String> getKeys() {
        return values.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> NbtBase<T> getValue(String key) {
        return (NbtBase<T>) values.get(key);
    }

    @Override
    public NbtBase<?> getValueOrDefault(String key, NbtType type) {
        NbtBase<?> value = values.get(key);
        if (value == null) {
            value = NbtFactory.ofWrapper(type, key);
            values.put(key, value);
        } else if (value.getType() != type) {
            throw new IllegalArgumentException("Cannot get " + key + " as " + type);
        }
        return value;
    }

    @Override
    public <T> NbtCompound put(@Nonnull NbtBase<T> entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Entry cannot be NULL.");
        }
        @SuppressWarnings("unchecked")
        NbtBase<T> copy = (NbtBase<T>) entry.deepClone();
        copy.setName(entry.getName());
        values.put(copy.getName(), copy);
        return this;
    }

    @Override
    public String getString(String key) {
        return valueAs(key, NbtType.TAG_STRING, String.class);
    }

    @Override
    public String getStringOrDefault(String key) {
        NbtBase<?> value = values.get(key);
        return value == null ? "" : String.valueOf(value.getValue());
    }

    @Override
    public NbtCompound put(String key, String value) {
        return putObject(key, value);
    }

    @Override
    public NbtCompound put(String key, NbtBase<?> entry) {
        if (entry == null) {
            values.remove(key);
            return this;
        }
        NbtBase<?> copy = entry.deepClone();
        copy.setName(key);
        values.put(key, copy);
        return this;
    }

    @Override
    public byte getByte(String key) {
        return valueAsNumber(key, NbtType.TAG_BYTE).byteValue();
    }

    @Override
    public byte getByteOrDefault(String key) {
        NbtBase<?> value = values.get(key);
        return value == null ? 0 : ((Number) value.getValue()).byteValue();
    }

    @Override
    public NbtCompound put(String key, byte value) {
        return putObject(key, value);
    }

    @Override
    public Short getShort(String key) {
        return valueAsNumber(key, NbtType.TAG_SHORT).shortValue();
    }

    @Override
    public short getShortOrDefault(String key) {
        NbtBase<?> value = values.get(key);
        return value == null ? 0 : ((Number) value.getValue()).shortValue();
    }

    @Override
    public NbtCompound put(String key, short value) {
        return putObject(key, value);
    }

    @Override
    public int getInteger(String key) {
        return valueAsNumber(key, NbtType.TAG_INT).intValue();
    }

    @Override
    public int getIntegerOrDefault(String key) {
        NbtBase<?> value = values.get(key);
        return value == null ? 0 : ((Number) value.getValue()).intValue();
    }

    @Override
    public NbtCompound put(String key, int value) {
        return putObject(key, value);
    }

    @Override
    public long getLong(String key) {
        return valueAsNumber(key, NbtType.TAG_LONG).longValue();
    }

    @Override
    public long getLongOrDefault(String key) {
        NbtBase<?> value = values.get(key);
        return value == null ? 0L : ((Number) value.getValue()).longValue();
    }

    @Override
    public NbtCompound put(String key, long value) {
        return putObject(key, value);
    }

    @Override
    public float getFloat(String key) {
        return valueAsNumber(key, NbtType.TAG_FLOAT).floatValue();
    }

    @Override
    public float getFloatOrDefault(String key) {
        NbtBase<?> value = values.get(key);
        return value == null ? 0F : ((Number) value.getValue()).floatValue();
    }

    @Override
    public NbtCompound put(String key, float value) {
        return putObject(key, value);
    }

    @Override
    public double getDouble(String key) {
        return valueAsNumber(key, NbtType.TAG_DOUBLE).doubleValue();
    }

    @Override
    public double getDoubleOrDefault(String key) {
        NbtBase<?> value = values.get(key);
        return value == null ? 0D : ((Number) value.getValue()).doubleValue();
    }

    @Override
    public NbtCompound put(String key, double value) {
        return putObject(key, value);
    }

    @Override
    public byte[] getByteArray(String key) {
        return valueAs(key, NbtType.TAG_BYTE_ARRAY, byte[].class);
    }

    @Override
    public NbtCompound put(String key, byte[] value) {
        return putObject(key, value);
    }

    @Override
    public int[] getIntegerArray(String key) {
        return valueAs(key, NbtType.TAG_INT_ARRAY, int[].class);
    }

    @Override
    public NbtCompound put(String key, int[] value) {
        return putObject(key, value);
    }

    @Override
    public NbtCompound putObject(String key, Object value) {
        if (value == null) {
            values.remove(key);
            return this;
        }
        NbtBase<?> converted = NbtValues.fromObject(key, value);
        if (converted == null) {
            values.remove(key);
        } else {
            values.put(key, converted);
        }
        return this;
    }

    @Override
    public Object getObject(String key) {
        NbtBase<?> value = values.get(key);
        if (value == null) {
            return null;
        }
        return value.getType().isComposite() ? value : value.getValue();
    }

    @Override
    public NbtCompound getCompound(String key) {
        NbtBase<?> value = getExact(key);
        if (!(value instanceof NbtCompound)) {
            throw new IllegalArgumentException("Key " + key + " is not a compound");
        }
        return (NbtCompound) value;
    }

    @Override
    public NbtCompound getCompoundOrDefault(String key) {
        NbtBase<?> value = values.get(key);
        if (value == null) {
            NbtCompound created = NbtFactory.ofCompound(key);
            values.put(key, created);
            return created;
        }
        return getCompound(key);
    }

    @Override
    public NbtCompound put(NbtCompound compound) {
        return put((NbtBase<?>) compound);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> NbtList<T> getList(String key) {
        NbtBase<?> value = getExact(key);
        if (!(value instanceof NbtList<?>)) {
            throw new IllegalArgumentException("Key " + key + " is not a list");
        }
        return (NbtList<T>) value;
    }

    @Override
    public <T> NbtList<T> getListOrDefault(String key) {
        NbtBase<?> value = values.get(key);
        if (value == null) {
            NbtList<T> created = NbtFactory.ofList(key);
            values.put(key, created);
            return created;
        }
        return getList(key);
    }

    @Override
    public <T> NbtCompound put(NbtList<T> list) {
        return put((NbtBase<?>) list);
    }

    @Override
    public <T> NbtCompound put(String key, Collection<? extends NbtBase<T>> list) {
        NbtList<T> created = NbtFactory.ofList(key);
        if (list != null) {
            for (NbtBase<T> entry : list) {
                created.add(entry);
            }
        }
        return put(created);
    }

    @Override
    public <T> NbtBase<?> remove(String key) {
        return values.remove(key);
    }

    @Override
    public Iterator<NbtBase<?>> iterator() {
        return values.values().iterator();
    }

    @Override
    public Map<String, NbtBase<?>> getValue() {
        return values;
    }

    @Override
    public void setValue(Map<String, NbtBase<?>> newValue) {
        values.clear();
        if (newValue != null) {
            for (NbtBase<?> entry : newValue.values()) {
                put(entry);
            }
        }
    }

    @Override
    public NbtCompound deepClone() {
        WrappedCompound clone = fromName(name);
        for (NbtBase<?> entry : values.values()) {
            clone.put(entry);
        }
        return clone;
    }

    @Override
    public void write(DataOutput destination) {
        NbtBinarySerializer.DEFAULT.serialize(this, destination);
    }

    private NbtBase<?> getExact(String key) {
        NbtBase<?> value = values.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Cannot find key " + key);
        }
        return value;
    }

    private Number valueAsNumber(String key, NbtType expected) {
        NbtBase<?> value = getExact(key);
        if (!(value.getValue() instanceof Number)) {
            throw new IllegalArgumentException("Key " + key + " is not numeric");
        }
        if (value.getType() != expected) {
            throw new IllegalArgumentException("Key " + key + " is not " + expected);
        }
        return (Number) value.getValue();
    }

    private <T> T valueAs(String key, NbtType expected, Class<T> type) {
        NbtBase<?> value = getExact(key);
        if (value.getType() != expected || !type.isInstance(value.getValue())) {
            throw new IllegalArgumentException("Key " + key + " is not " + expected);
        }
        return type.cast(value.getValue());
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override public int hashCode() { return java.util.Objects.hash(name, values); }
    @Override public boolean equals(Object other) {
        if (!(other instanceof NbtCompound compound)) return false;
        return java.util.Objects.equals(name, compound.getName())
                && java.util.Objects.equals(values, compound.getValue());
    }
}
