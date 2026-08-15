/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.utility;

import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import io.netty.buffer.ByteBuf;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Bukkit-native item serializer compatible with the legacy StreamSerializer method surface. */
public class StreamSerializer {
    private static final StreamSerializer DEFAULT = new StreamSerializer();

    public StreamSerializer() { }

    public static StreamSerializer getDefault() {
        return DEFAULT;
    }

    public String serializeItemStack(ItemStack stack) {
        return stack == null ? null : Base64.getEncoder().encodeToString(stack.serializeAsBytes());
    }

    public ItemStack deserializeItemStack(String encoded) {
        if (encoded == null || encoded.isEmpty()) return null;
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded));
    }

    public void serializeVarInt(DataOutputStream output, int value) throws IOException {
        while ((value & ~0x7F) != 0) { output.writeByte((value & 0x7F) | 0x80); value >>>= 7; }
        output.writeByte(value);
    }

    public int deserializeVarInt(DataInputStream input) throws IOException {
        int result = 0; int shift = 0;
        for (int i = 0; i < 5; i++) {
            int next = input.readUnsignedByte(); result |= (next & 0x7F) << shift;
            if ((next & 0x80) == 0) return result; shift += 7;
        }
        throw new IOException("VarInt is too large");
    }

    public void serializeCompound(DataOutputStream output, NbtCompound compound) {
        NbtBinarySerializer.DEFAULT.serialize(compound, output);
    }

    public NbtCompound deserializeCompound(DataInputStream input) {
        return NbtBinarySerializer.DEFAULT.deserializeCompound(input);
    }

    public void serializeString(DataOutputStream output, String value) {
        try {
            byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
            serializeVarInt(output, bytes.length); output.write(bytes);
        } catch (IOException e) { throw new IllegalStateException("Unable to serialize string", e); }
    }

    public String deserializeString(DataInputStream input, int maximumLength) {
        try {
            int length = deserializeVarInt(input);
            if (length < 0 || length > maximumLength * 4) throw new IllegalArgumentException("String is too long");
            byte[] bytes = input.readNBytes(length);
            if (bytes.length != length) throw new IOException("Unexpected end of string");
            String result = new String(bytes, StandardCharsets.UTF_8);
            if (result.length() > maximumLength) throw new IllegalArgumentException("String is too long");
            return result;
        } catch (IOException e) { throw new IllegalStateException("Unable to deserialize string", e); }
    }

    public byte[] serializeItemStackToByteArray(ItemStack stack) throws IOException {
        return stack == null ? new byte[0] : stack.serializeAsBytes();
    }

    public ItemStack deserializeItemStackFromByteArray(byte[] bytes) {
        return bytes == null || bytes.length == 0 ? null : ItemStack.deserializeBytes(bytes);
    }

    public void serializeItemStack(DataOutputStream output, ItemStack stack) throws IOException {
        byte[] bytes = serializeItemStackToByteArray(stack); serializeVarInt(output, bytes.length); output.write(bytes);
    }

    public byte[] getBytesAndRelease(ByteBuf buffer) {
        if (buffer == null) return new byte[0];
        try { return com.github.retrooper.packetevents.netty.buffer.ByteBufHelper.copyBytes(buffer); }
        finally { if (buffer.refCnt() > 0) buffer.release(); }
    }
}
