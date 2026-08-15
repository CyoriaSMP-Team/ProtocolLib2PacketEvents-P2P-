package com.comphenix.protocol.wrappers.nbt;

/** Visitor for traversing an NBT tree. */
public interface NbtVisitor {
    boolean visit(NbtBase<?> node);

    boolean visitEnter(NbtList<?> list);

    boolean visitEnter(NbtCompound compound);

    boolean visitLeave(NbtList<?> list);

    boolean visitLeave(NbtCompound compound);
}
