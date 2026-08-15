package com.comphenix.protocol.utility;

import com.comphenix.protocol.injector.netty.NettyByteBufAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** A zero-capacity buffer used by compatibility code that needs an empty payload. */
public class ZeroBuffer extends NettyByteBufAdapter {
    public ZeroBuffer() {
        super(Unpooled.buffer(0, 0), null);
    }

    @Override public int capacity() {
        return 0;
    }

    @Override public ByteBuf capacity(int capacity) {
        if (capacity != 0) throw new IllegalArgumentException("ZeroBuffer cannot grow");
        return this;
    }

    @Override public ByteBuf ensureWritable(int minWritableBytes) {
        if (minWritableBytes != 0) throw new IndexOutOfBoundsException("ZeroBuffer is empty");
        return this;
    }

    @Override public int ensureWritable(int minWritableBytes, boolean force) {
        if (minWritableBytes != 0) return force ? 1 : 0;
        return 0;
    }
}
