package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.wrappers.ClonableWrapper;

/**
 * Clean-room representation of a Minecraft NBT tag.
 *
 * <p>The implementation deliberately keeps the public ProtocolLib shape while
 * allowing the backend to use an in-memory value when the server does not
 * expose an NMS tag constructor.</p>
 */
public interface NbtBase<TType> extends ClonableWrapper {
    boolean accept(NbtVisitor visitor);

    NbtType getType();

    String getName();

    void setName(String name);

    TType getValue();

    void setValue(TType newValue);

    NbtBase<TType> deepClone();

    @Override
    default Object getHandle() {
        return null;
    }
}
