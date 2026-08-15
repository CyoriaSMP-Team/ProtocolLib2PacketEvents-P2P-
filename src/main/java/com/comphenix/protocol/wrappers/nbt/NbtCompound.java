package com.comphenix.protocol.wrappers.nbt;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/** A named map of NBT values. */
public interface NbtCompound extends NbtBase<Map<String, NbtBase<?>>>, Iterable<NbtBase<?>> {
    @Override
    @Deprecated
    Map<String, NbtBase<?>> getValue();

    boolean containsKey(String key);

    Set<String> getKeys();

    <T> NbtBase<T> getValue(String key);

    NbtBase<?> getValueOrDefault(String key, NbtType type);

    <T> NbtCompound put(@Nonnull NbtBase<T> entry);

    String getString(String key);
    String getStringOrDefault(String key);
    NbtCompound put(String key, String value);
    NbtCompound put(String key, NbtBase<?> entry);

    byte getByte(String key);
    byte getByteOrDefault(String key);
    NbtCompound put(String key, byte value);

    Short getShort(String key);
    short getShortOrDefault(String key);
    NbtCompound put(String key, short value);

    int getInteger(String key);
    int getIntegerOrDefault(String key);
    NbtCompound put(String key, int value);

    long getLong(String key);
    long getLongOrDefault(String key);
    NbtCompound put(String key, long value);

    float getFloat(String key);
    float getFloatOrDefault(String key);
    NbtCompound put(String key, float value);

    double getDouble(String key);
    double getDoubleOrDefault(String key);
    NbtCompound put(String key, double value);

    byte[] getByteArray(String key);
    NbtCompound put(String key, byte[] value);

    int[] getIntegerArray(String key);
    NbtCompound put(String key, int[] value);

    NbtCompound putObject(String key, Object value);
    Object getObject(String key);

    NbtCompound getCompound(String key);
    NbtCompound getCompoundOrDefault(String key);
    NbtCompound put(NbtCompound compound);

    <T> NbtList<T> getList(String key);
    <T> NbtList<T> getListOrDefault(String key);
    <T> NbtCompound put(NbtList<T> list);
    <T> NbtCompound put(String key, Collection<? extends NbtBase<T>> list);

    <T> NbtBase<?> remove(String key);

    @Override
    Iterator<NbtBase<?>> iterator();
}
