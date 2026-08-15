package com.comphenix.protocol.injector.netty;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

/**
 * Small Netty adapter retained for integrations that use ProtocolLib's stream helpers.
 *
 * <p>PacketEvents normally supplies the actual buffer.  This adapter is deliberately
 * self-contained and delegates storage to a normal Netty buffer, so it remains usable
 * without ProtocolLib or Minecraft internals on the runtime class path.</p>
 */
public class NettyByteBufAdapter extends AbstractByteBuf {
    private final ByteBuf delegate;
    private final OutputStream output;

    protected NettyByteBufAdapter(ByteBuf delegate, OutputStream output) {
        super(delegate.maxCapacity());
        this.delegate = delegate;
        this.output = output;
    }

    public static ByteBuf packetReader(DataInputStream input) {
        if (input == null) throw new NullPointerException("input");
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            input.transferTo(bytes);
            return new NettyByteBufAdapter(Unpooled.wrappedBuffer(bytes.toByteArray()), null);
        } catch (IOException error) {
            throw new IllegalStateException("Unable to read packet stream", error);
        }
    }

    public static ByteBuf packetWriter(DataOutputStream output) {
        if (output == null) throw new NullPointerException("output");
        return new NettyByteBufAdapter(Unpooled.buffer(), output);
    }

    private ByteBuf d() { return delegate; }
    private void flushOutput() {
        if (output == null || !delegate.isReadable()) return;
        try {
            byte[] bytes = new byte[delegate.readableBytes()];
            delegate.getBytes(delegate.readerIndex(), bytes);
            output.write(bytes);
        } catch (IOException error) {
            throw new IllegalStateException("Unable to write packet stream", error);
        }
    }

    @Override protected byte _getByte(int index) { return d().getByte(index); }
    @Override protected short _getShort(int index) { return d().getShort(index); }
    @Override protected short _getShortLE(int index) { return d().getShortLE(index); }
    @Override protected int _getUnsignedMedium(int index) { return d().getUnsignedMedium(index); }
    @Override protected int _getUnsignedMediumLE(int index) { return d().getUnsignedMediumLE(index); }
    @Override protected int _getInt(int index) { return d().getInt(index); }
    @Override protected int _getIntLE(int index) { return d().getIntLE(index); }
    @Override protected long _getLong(int index) { return d().getLong(index); }
    @Override protected long _getLongLE(int index) { return d().getLongLE(index); }
    @Override protected void _setByte(int index, int value) { d().setByte(index, value); }
    @Override protected void _setShort(int index, int value) { d().setShort(index, value); }
    @Override protected void _setShortLE(int index, int value) { d().setShortLE(index, value); }
    @Override protected void _setMedium(int index, int value) { d().setMedium(index, value); }
    @Override protected void _setMediumLE(int index, int value) { d().setMediumLE(index, value); }
    @Override protected void _setInt(int index, int value) { d().setInt(index, value); }
    @Override protected void _setIntLE(int index, int value) { d().setIntLE(index, value); }
    @Override protected void _setLong(int index, long value) { d().setLong(index, value); }
    @Override protected void _setLongLE(int index, long value) { d().setLongLE(index, value); }

    @Override public int capacity() { return d().capacity(); }
    @Override public ByteBuf capacity(int value) { d().capacity(value); return this; }
    @Override public ByteBufAllocator alloc() { return d().alloc(); }
    @Override public ByteOrder order() { return d().order(); }
    @Override public ByteBuf unwrap() { return d(); }
    @Override public boolean isDirect() { return d().isDirect(); }
    @Override public boolean isReadOnly() { return d().isReadOnly(); }
    @Override public ByteBuf asReadOnly() { return d().asReadOnly(); }
    @Override public int refCnt() { return d().refCnt(); }
    @Override public boolean release() { flushOutput(); return d().release(); }
    @Override public boolean release(int decrement) { flushOutput(); return d().release(decrement); }
    @Override public ByteBuf retain() { d().retain(); return this; }
    @Override public ByteBuf retain(int increment) { d().retain(increment); return this; }
    @Override public ByteBuf touch() { d().touch(); return this; }
    @Override public ByteBuf touch(Object hint) { d().touch(hint); return this; }

    @Override public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) { d().getBytes(index, dst, dstIndex, length); return this; }
    @Override public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) { d().getBytes(index, dst, dstIndex, length); return this; }
    @Override public ByteBuf getBytes(int index, ByteBuffer dst) { d().getBytes(index, dst); return this; }
    @Override public ByteBuf getBytes(int index, OutputStream dst, int length) throws IOException { d().getBytes(index, dst, length); return this; }
    @Override public int getBytes(int index, GatheringByteChannel dst, int length) throws IOException { return d().getBytes(index, dst, length); }
    @Override public int getBytes(int index, FileChannel dst, long position, int length) throws IOException { return d().getBytes(index, dst, position, length); }
    @Override public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) { d().setBytes(index, src, srcIndex, length); return this; }
    @Override public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) { d().setBytes(index, src, srcIndex, length); return this; }
    @Override public ByteBuf setBytes(int index, ByteBuffer src) { d().setBytes(index, src); return this; }
    @Override public int setBytes(int index, InputStream src, int length) throws IOException { return d().setBytes(index, src, length); }
    @Override public int setBytes(int index, ScatteringByteChannel src, int length) throws IOException { return d().setBytes(index, src, length); }
    @Override public int setBytes(int index, FileChannel src, long position, int length) throws IOException { return d().setBytes(index, src, position, length); }
    @Override public ByteBuf copy(int index, int length) { return new NettyByteBufAdapter(d().copy(index, length), null); }
    @Override public int nioBufferCount() { return d().nioBufferCount(); }
    @Override public ByteBuffer nioBuffer(int index, int length) { return d().nioBuffer(index, length); }
    @Override public ByteBuffer internalNioBuffer(int index, int length) { return d().internalNioBuffer(index, length); }
    @Override public ByteBuffer[] nioBuffers(int index, int length) { return d().nioBuffers(index, length); }
    @Override public boolean hasArray() { return d().hasArray(); }
    @Override public byte[] array() { return d().array(); }
    @Override public int arrayOffset() { return d().arrayOffset(); }
    @Override public boolean hasMemoryAddress() { return d().hasMemoryAddress(); }
    @Override public long memoryAddress() { return d().memoryAddress(); }

    @Override public String toString(Charset charset) { return d().toString(charset); }
    @Override public String toString(int index, int length, Charset charset) { return d().toString(index, length, charset); }
    @Override public int hashCode() { return d().hashCode(); }
    @Override public boolean equals(Object other) { return other == this || d().equals(other); }
    @Override public int compareTo(ByteBuf other) { return d().compareTo(other); }
    @Override public String toString() { return d().toString(); }
}
