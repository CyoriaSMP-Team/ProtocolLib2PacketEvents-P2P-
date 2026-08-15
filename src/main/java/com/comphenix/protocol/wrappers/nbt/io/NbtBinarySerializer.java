package com.comphenix.protocol.wrappers.nbt.io;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

/** Standard NBT binary codec for the logical wrapper model. */
public class NbtBinarySerializer {
    public static final NbtBinarySerializer DEFAULT = new NbtBinarySerializer();

    public <T> void serialize(NbtBase<T> value, DataOutput destination) {
        if (value == null || destination == null) {
            throw new IllegalArgumentException("value and destination are required");
        }
        try {
            writeTag(value, destination, true);
        } catch (IOException ex) {
            throw new FieldAccessException("Unable to write NBT", ex);
        }
    }

    public <TType> NbtWrapper<TType> deserialize(DataInput source) {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be NULL");
        }
        try {
            int id = source.readUnsignedByte();
            NbtType type = NbtType.getTypeFromID(id);
            if (type == NbtType.TAG_END) {
                throw new IOException("Root NBT tag cannot be TAG_END");
            }
            String name = source.readUTF();
            return (NbtWrapper<TType>) readTag(type, name, source);
        } catch (IOException | RuntimeException ex) {
            if (ex instanceof FieldAccessException) {
                throw (FieldAccessException) ex;
            }
            throw new FieldAccessException("Unable to read NBT", ex);
        }
    }

    public NbtCompound deserializeCompound(DataInput source) {
        NbtWrapper<?> result = deserialize(source);
        return NbtFactory.asCompound(result);
    }

    @SuppressWarnings("unchecked")
    public <T> NbtList<T> deserializeList(DataInput source) {
        NbtWrapper<?> result = deserialize(source);
        return (NbtList<T>) NbtFactory.asList(result);
    }

    interface CodecMethod {
        Object loadNbt(DataInput input);
        void writeNbt(Object value, DataOutput output);
    }

    static class LoadMethodConfigPhaseUpdate implements CodecMethod {
        public LoadMethodConfigPhaseUpdate() { }
        @Override public Object loadNbt(DataInput input) { return NbtBinarySerializer.DEFAULT.deserialize(input); }
        @Override public void writeNbt(Object value, DataOutput output) {
            if (!(value instanceof NbtBase<?> tag)) throw new IllegalArgumentException("value is not an NBT tag");
            NbtBinarySerializer.DEFAULT.serialize(tag, output);
        }
    }

    static class LoadMethodSkinUpdate implements CodecMethod {
        @Override public Object loadNbt(DataInput input) { return NbtBinarySerializer.DEFAULT.deserialize(input); }
        @Override public void writeNbt(Object value, DataOutput output) {
            if (!(value instanceof NbtBase<?> tag)) throw new IllegalArgumentException("value is not an NBT tag");
            NbtBinarySerializer.DEFAULT.serialize(tag, output);
        }
    }

    private static void writeTag(NbtBase<?> tag, DataOutput output, boolean named) throws IOException {
        NbtType type = tag.getType();
        output.writeByte(type.getRawID());
        if (named) {
            output.writeUTF(tag.getName() == null ? "" : tag.getName());
        }
        writePayload(tag, output);
    }

    private static void writePayload(NbtBase<?> tag, DataOutput output) throws IOException {
        Object value = tag.getValue();
        switch (tag.getType()) {
            case TAG_BYTE -> output.writeByte(((Number) value).byteValue());
            case TAG_SHORT -> output.writeShort(((Number) value).shortValue());
            case TAG_INT -> output.writeInt(((Number) value).intValue());
            case TAG_LONG -> output.writeLong(((Number) value).longValue());
            case TAG_FLOAT -> output.writeFloat(((Number) value).floatValue());
            case TAG_DOUBLE -> output.writeDouble(((Number) value).doubleValue());
            case TAG_BYTE_ARRAY -> {
                byte[] bytes = (byte[]) value;
                output.writeInt(bytes.length);
                output.write(bytes);
            }
            case TAG_STRING -> output.writeUTF(String.valueOf(value));
            case TAG_LIST -> {
                NbtList<?> list = (NbtList<?>) tag;
                NbtType elementType = list.getElementType();
                output.writeByte(elementType.getRawID());
                output.writeInt(list.size());
                for (NbtBase<?> entry : list.asCollection()) {
                    writePayload(entry, output);
                }
            }
            case TAG_COMPOUND -> {
                NbtCompound compound = (NbtCompound) tag;
                for (NbtBase<?> entry : compound) {
                    writeTag(entry, output, true);
                }
                output.writeByte(NbtType.TAG_END.getRawID());
            }
            case TAG_INT_ARRAY -> {
                int[] ints = (int[]) value;
                output.writeInt(ints.length);
                for (int entry : ints) {
                    output.writeInt(entry);
                }
            }
            case TAG_LONG_ARRAY -> {
                long[] longs = (long[]) value;
                output.writeInt(longs.length);
                for (long entry : longs) {
                    output.writeLong(entry);
                }
            }
            case TAG_END -> throw new IOException("Cannot serialize TAG_END");
        }
    }

    private static NbtBase<?> readTag(NbtType type, String name, DataInput input) throws IOException {
        return switch (type) {
            case TAG_COMPOUND -> {
                NbtCompound compound = NbtFactory.ofCompound(name);
                while (true) {
                    int id = input.readUnsignedByte();
                    NbtType childType = NbtType.getTypeFromID(id);
                    if (childType == NbtType.TAG_END) {
                        break;
                    }
                    String childName = input.readUTF();
                    compound.put(readTag(childType, childName, input));
                }
                yield compound;
            }
            case TAG_LIST -> {
                NbtType elementType = NbtType.getTypeFromID(input.readUnsignedByte());
                int length = input.readInt();
                if (length < 0) {
                    throw new IOException("Negative NBT list length");
                }
                NbtList<Object> list = NbtFactory.ofList(name);
                if (elementType != NbtType.TAG_END) {
                    list.setElementType(elementType);
                }
                for (int index = 0; index < length; index++) {
                    NbtBase<?> entry = readTag(elementType, NbtList.EMPTY_NAME, input);
                    list.add((NbtBase<Object>) entry);
                }
                yield list;
            }
            case TAG_BYTE -> NbtFactory.of(name, input.readByte());
            case TAG_SHORT -> NbtFactory.of(name, input.readShort());
            case TAG_INT -> NbtFactory.of(name, input.readInt());
            case TAG_LONG -> NbtFactory.of(name, input.readLong());
            case TAG_FLOAT -> NbtFactory.of(name, input.readFloat());
            case TAG_DOUBLE -> NbtFactory.of(name, input.readDouble());
            case TAG_BYTE_ARRAY -> {
                int length = input.readInt();
                if (length < 0) {
                    throw new IOException("Negative NBT byte-array length");
                }
                byte[] bytes = new byte[length];
                input.readFully(bytes);
                yield NbtFactory.of(name, bytes);
            }
            case TAG_STRING -> NbtFactory.of(name, input.readUTF());
            case TAG_INT_ARRAY -> {
                int length = input.readInt();
                if (length < 0) {
                    throw new IOException("Negative NBT int-array length");
                }
                int[] ints = new int[length];
                for (int index = 0; index < length; index++) {
                    ints[index] = input.readInt();
                }
                yield NbtFactory.of(name, ints);
            }
            case TAG_LONG_ARRAY -> {
                int length = input.readInt();
                if (length < 0) {
                    throw new IOException("Negative NBT long-array length");
                }
                long[] longs = new long[length];
                for (int index = 0; index < length; index++) {
                    longs[index] = input.readLong();
                }
                yield NbtFactory.of(name, longs);
            }
            case TAG_END -> throw new EOFException("Unexpected TAG_END");
        };
    }
}
