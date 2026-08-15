package com.comphenix.protocol.wrappers.nbt;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** A list of unnamed NBT values. */
public interface NbtList<TType> extends NbtBase<List<NbtBase<TType>>>, Iterable<TType> {
    String EMPTY_NAME = "";

    NbtType getElementType();

    @Deprecated
    void setElementType(NbtType type);

    @Deprecated
    void addClosest(Object value);

    void addClosest(Object value, NbtType type);

    void add(NbtBase<TType> element);
    void add(String value);
    void add(byte value);
    void add(short value);
    void add(int value);
    void add(long value);
    void add(double value);
    void add(byte[] value);
    void add(int[] value);
    void remove(Object remove);
    TType getValue(int index);
    int size();
    Collection<NbtBase<TType>> asCollection();

    @Override
    Iterator<TType> iterator();
}
