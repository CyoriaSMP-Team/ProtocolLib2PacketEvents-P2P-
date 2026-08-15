/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.injector.netty;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Arrays;

/** Immutable raw packet payload used by ProtocolLib's wire-level API. */
public final class WirePacket {
    private final int id;
    private final byte[] bytes;
    private PacketType type;

    public WirePacket(int id, byte[] bytes) {
        if (id < 0) {
            throw new IllegalArgumentException("packet id must be non-negative");
        }
        this.id = id;
        this.bytes = bytes == null ? new byte[0] : bytes.clone();
        this.type = null;
    }

    public WirePacket(PacketType type, byte[] bytes) {
        this(type == null ? -1 : type.getCurrentId(), bytes);
        if (this.id < 0) throw new IllegalArgumentException("packet type has no current id: " + type);
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public byte[] getBytes() {
        return bytes.clone();
    }

    public ByteBuf serialize() { ByteBuf output = Unpooled.buffer(); writeFully(output); return output; }
    public static byte[] bytesFromPacket(PacketContainer packet) {
        if (packet == null) return new byte[0];
        Object raw = packet.serializeToBuffer();
        if (raw instanceof ByteBuf buffer) return com.github.retrooper.packetevents.netty.buffer.ByteBufHelper.copyBytes(buffer);
        if (raw instanceof byte[] bytes) return bytes.clone();
        throw new IllegalArgumentException("Packet has no raw buffer: " + packet.getType());
    }
    public static WirePacket fromPacket(PacketContainer packet) { return new WirePacket(packet.getType(), bytesFromPacket(packet)); }
    public static WirePacket fromPacket(Object packet) {
        if (packet instanceof WirePacket wire) return wire;
        if (packet instanceof PacketContainer container) return fromPacket(container);
        if (packet instanceof byte[] bytes) return new WirePacket(0, bytes);
        throw new IllegalArgumentException("Unsupported packet object: " + packet);
    }
    public static int readVarInt(ByteBuf input) {
        int result=0, shift=0; for(int i=0;i<5;i++){int value=input.readUnsignedByte();result|=(value&0x7f)<<shift;if((value&0x80)==0)return result;shift+=7;}throw new IllegalArgumentException("VarInt too large");
    }
    public static void writeVarInt(ByteBuf output, int value) { while((value&~0x7f)!=0){output.writeByte((value&0x7f)|0x80);value>>>=7;}output.writeByte(value); }
    public void writeBytes(ByteBuf output) { output.writeBytes(bytes); }
    public void writeId(ByteBuf output) { writeVarInt(output, id); }
    public void writeFully(ByteBuf output) { writeId(output); writeBytes(output); }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WirePacket packet)) return false;
        return id == packet.id && Arrays.equals(bytes, packet.bytes);
    }

    @Override
    public int hashCode() {
        return 31 * id + Arrays.hashCode(bytes);
    }

    @Override public String toString() { return "WirePacket{id=" + id + ", bytes=" + bytes.length + "}"; }
}
