package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.wrappers.ClonableWrapper;
import java.io.DataOutput;

/** NBT value backed by a native or logical handle. */
public interface NbtWrapper<TType> extends NbtBase<TType>, ClonableWrapper {
    @Override
    Object getHandle();

    void write(DataOutput destination);
}
