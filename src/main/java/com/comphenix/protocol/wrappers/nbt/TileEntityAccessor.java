package com.comphenix.protocol.wrappers.nbt;

import org.bukkit.block.BlockState;

final class TileEntityAccessor<T extends BlockState> {
    private TileEntityAccessor() { }
    public static <T extends BlockState> TileEntityAccessor<T> getAccessor(T state){return new TileEntityAccessor<>();}
    public NbtCompound readBlockState(T state){return NbtFactory.ofCompound("tag");}
    public void writeBlockState(T state,NbtCompound tag){ }
}
