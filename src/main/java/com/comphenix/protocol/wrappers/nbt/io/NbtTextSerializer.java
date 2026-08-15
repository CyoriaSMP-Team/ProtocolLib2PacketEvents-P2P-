package com.comphenix.protocol.wrappers.nbt.io;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;

/** Base64 text facade over the binary NBT codec. */
public class NbtTextSerializer {
    public static final NbtTextSerializer DEFAULT = new NbtTextSerializer();

    private final NbtBinarySerializer binarySerializer;

    public NbtTextSerializer() {
        this(new NbtBinarySerializer());
    }

    public NbtTextSerializer(NbtBinarySerializer binary) {
        this.binarySerializer = binary;
    }

    public NbtBinarySerializer getBinarySerializer() {
        return binarySerializer;
    }

    public <TType> String serialize(NbtBase<TType> value) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        binarySerializer.serialize(value, new DataOutputStream(bytes));
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    public <TType> NbtWrapper<TType> deserialize(String input) throws IOException {
        try {
            byte[] bytes = Base64.getDecoder().decode(input);
            return binarySerializer.deserialize(new DataInputStream(new ByteArrayInputStream(bytes)));
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid base64 NBT", ex);
        }
    }

    public NbtCompound deserializeCompound(String input) throws IOException {
        return (NbtCompound) (NbtBase<?>) deserialize(input);
    }

    @SuppressWarnings("unchecked")
    public <T> NbtList<T> deserializeList(String input) throws IOException {
        return (NbtList<T>) (NbtBase<?>) deserialize(input);
    }
}
