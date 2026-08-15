package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.reflect.FieldAccessException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nonnull;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

/** Factory for logical and native-compatible NBT wrappers. */
@SuppressWarnings({"unchecked", "rawtypes"})
public class NbtFactory {
    private static final Map<ItemStack, NbtCompound> ITEM_TAGS = new WeakHashMap<>();
    private static final Map<BlockState, NbtCompound> BLOCK_TAGS = new WeakHashMap<>();

    public NbtFactory() {
    }

    public static NbtCompound asCompound(NbtBase<?> tag) {
        if (tag instanceof NbtCompound) {
            return (NbtCompound) tag;
        }
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be NULL.");
        }
        throw new UnsupportedOperationException("Cannot cast " + tag.getType() + " to TAG_COMPOUND");
    }

    public static NbtList<?> asList(NbtBase<?> tag) {
        if (tag instanceof NbtList<?>) {
            return (NbtList<?>) tag;
        }
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be NULL.");
        }
        throw new UnsupportedOperationException("Cannot cast " + tag.getType() + " to TAG_LIST");
    }

    public static <T> NbtWrapper<T> fromBase(NbtBase<T> base) {
        if (base == null) {
            throw new IllegalArgumentException("base cannot be NULL.");
        }
        if (base instanceof NbtWrapper<?>) {
            return (NbtWrapper<T>) base;
        }
        return (NbtWrapper<T>) fromLogicalCopy(base);
    }

    public static void setItemTag(ItemStack stack, NbtCompound compound) {
        checkItemStack(stack);
        synchronized (ITEM_TAGS) {
            if (compound == null) {
                ITEM_TAGS.remove(stack);
            } else {
                ITEM_TAGS.put(stack, (NbtCompound) compound.deepClone());
            }
        }
    }

    public static NbtWrapper<?> fromItemTag(ItemStack stack) {
        checkItemStack(stack);
        synchronized (ITEM_TAGS) {
            NbtCompound value = ITEM_TAGS.get(stack);
            if (value == null) {
                value = ofCompound("tag");
                ITEM_TAGS.put(stack, value);
            }
            return fromBase(value);
        }
    }

    public static Optional<NbtWrapper<?>> fromItemOptional(ItemStack stack) {
        checkItemStack(stack);
        synchronized (ITEM_TAGS) {
            NbtCompound value = ITEM_TAGS.get(stack);
            return value == null ? Optional.empty() : Optional.of(fromBase(value));
        }
    }

    public static NbtCompound fromFile(String file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be NULL");
        }
        try (DataInputStream input = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            return NbtBinarySerializerBridge.deserializeCompound(input);
        }
    }

    public static void toFile(NbtCompound compound, String file) throws IOException {
        if (compound == null || file == null) {
            throw new IllegalArgumentException("compound and file are required");
        }
        try (DataOutputStream output = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            NbtBinarySerializerBridge.serialize(compound, output);
        }
    }

    public static NbtCompound readBlockState(Block block) {
        if (block == null) {
            return null;
        }
        BlockState state = block.getState();
        synchronized (BLOCK_TAGS) {
            NbtCompound result = BLOCK_TAGS.get(state);
            return result == null ? null : (NbtCompound) result.deepClone();
        }
    }

    public static void writeBlockState(Block target, NbtCompound blockState) {
        if (target == null) {
            throw new IllegalArgumentException("target cannot be NULL");
        }
        synchronized (BLOCK_TAGS) {
            if (blockState == null) {
                BLOCK_TAGS.remove(target.getState());
            } else {
                BLOCK_TAGS.put(target.getState(), (NbtCompound) blockState.deepClone());
            }
        }
    }

    @Deprecated
    public static <T> NbtWrapper<T> fromNMS(Object handle) {
        return fromNMS(handle, "");
    }

    public static <T> NbtWrapper<T> fromNMS(Object handle, String name) {
        if (handle == null) {
            throw new IllegalArgumentException("handle cannot be NULL");
        }
        if (handle instanceof NbtWrapper<?>) {
            return (NbtWrapper<T>) handle;
        }
        if (handle instanceof NbtCompound || handle instanceof Map<?, ?>) {
            return (NbtWrapper<T>) new WrappedCompound(handle, name);
        }
        if (handle instanceof NbtList<?> || handle instanceof java.util.List<?>) {
            return (NbtWrapper<T>) new WrappedList(handle, name);
        }
        return new WrappedElement<>(handle, name);
    }

    @Nonnull
    public static NbtCompound fromNMSCompound(@Nonnull Object handle) {
        return asCompound(fromNMS(handle));
    }

    public static NbtBase<String> of(String name, String value) {
        return ofWrapper(NbtType.TAG_STRING, name, value);
    }

    public static NbtBase<Byte> of(String name, byte value) {
        return ofWrapper(NbtType.TAG_BYTE, name, value);
    }

    public static NbtBase<Short> of(String name, short value) {
        return ofWrapper(NbtType.TAG_SHORT, name, value);
    }

    public static NbtBase<Integer> of(String name, int value) {
        return ofWrapper(NbtType.TAG_INT, name, value);
    }

    public static NbtBase<Long> of(String name, long value) {
        return ofWrapper(NbtType.TAG_LONG, name, value);
    }

    public static NbtBase<Float> of(String name, float value) {
        return ofWrapper(NbtType.TAG_FLOAT, name, value);
    }

    public static NbtBase<Double> of(String name, double value) {
        return ofWrapper(NbtType.TAG_DOUBLE, name, value);
    }

    public static NbtBase<byte[]> of(String name, byte[] value) {
        return ofWrapper(NbtType.TAG_BYTE_ARRAY, name, value);
    }

    public static NbtBase<int[]> of(String name, int[] value) {
        return ofWrapper(NbtType.TAG_INT_ARRAY, name, value);
    }

    public static NbtBase<long[]> of(String name, long[] value) {
        return ofWrapper(NbtType.TAG_LONG_ARRAY, name, value);
    }

    public static NbtCompound ofCompound(String name, Collection<? extends NbtBase<?>> list) {
        return WrappedCompound.fromList(name, list);
    }

    public static NbtCompound ofCompound(String name) {
        return WrappedCompound.fromName(name);
    }

    @SafeVarargs
    public static <T> NbtList<T> ofList(String name, T... elements) {
        return WrappedList.fromArray(name, elements);
    }

    public static <T> NbtList<T> ofList(String name, Collection<? extends T> elements) {
        return WrappedList.fromList(name, elements);
    }

    public static <T> NbtWrapper<T> ofWrapper(NbtType type, String name) {
        if (type == null || type == NbtType.TAG_END) {
            throw new IllegalArgumentException("Cannot create " + type);
        }
        switch (type) {
            case TAG_COMPOUND:
                return (NbtWrapper<T>) WrappedCompound.fromName(name);
            case TAG_LIST:
                return (NbtWrapper<T>) WrappedList.fromName(name);
            default:
                return new WrappedElement<T>(type, name, (T) defaultValue(type));
        }
    }

    public static <T> NbtWrapper<T> ofWrapper(NbtType type, String name, T value) {
        if (type == NbtType.TAG_COMPOUND) {
            if (value instanceof NbtCompound) {
                NbtCompound clone = (NbtCompound) ((NbtCompound) value).deepClone();
                clone.setName(name);
                return (NbtWrapper<T>) clone;
            }
            WrappedCompound compound = WrappedCompound.fromName(name);
            if (value instanceof Map<?, ?>) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                    compound.putObject(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            return (NbtWrapper<T>) compound;
        }
        if (type == NbtType.TAG_LIST) {
            if (value instanceof NbtList<?>) {
                NbtList<?> clone = (NbtList<?>) (Object) ((NbtList<?>) value).deepClone();
                clone.setName(name);
                return (NbtWrapper<T>) clone;
            }
            WrappedList<Object> list = new WrappedList<>(java.util.Collections.emptyList());
            list.setName(name);
            if (value instanceof Collection<?>) {
                for (Object entry : (Collection<?>) value) {
                    if (entry instanceof NbtBase<?>) {
                        list.add((NbtBase<Object>) entry);
                    } else if (entry != null) {
                        list.addClosest(entry, NbtType.getTypeFromClass(entry.getClass()));
                    }
                }
            }
            return (NbtWrapper<T>) list;
        }
        T actual = value == null ? (T) defaultValue(type) : value;
        return new WrappedElement<T>(type, name, actual);
    }

    public static <T> NbtWrapper<T> ofWrapper(Class<?> type, String name, T value) {
        return ofWrapper(NbtType.getTypeFromClass(type), name, value);
    }

    static Object defaultValue(NbtType type) {
        return switch (type) {
            case TAG_BYTE -> (byte) 0;
            case TAG_SHORT -> (short) 0;
            case TAG_INT -> 0;
            case TAG_LONG -> 0L;
            case TAG_FLOAT -> 0F;
            case TAG_DOUBLE -> 0D;
            case TAG_BYTE_ARRAY -> new byte[0];
            case TAG_INT_ARRAY -> new int[0];
            case TAG_LONG_ARRAY -> new long[0];
            case TAG_STRING -> "";
            case TAG_COMPOUND -> ofCompound("");
            case TAG_LIST -> ofList("");
            case TAG_END -> null;
        };
    }

    private static NbtBase<?> fromLogicalCopy(NbtBase<?> base) {
        if (base instanceof NbtCompound) {
            return ((NbtCompound) base).deepClone();
        }
        if (base instanceof NbtList<?>) {
            return ((NbtList<?>) base).deepClone();
        }
        return new WrappedElement<>(base.getType(), base.getName(), base.getValue());
    }

    private static void checkItemStack(ItemStack stack) {
        if (stack == null) {
            throw new IllegalArgumentException("stack cannot be NULL");
        }
        if (stack.getType() == Material.AIR) {
            throw new IllegalArgumentException("ItemStacks representing air cannot store NMS information.");
        }
    }

    private static final class NbtBinarySerializerBridge {
        private static void serialize(NbtBase<?> value, java.io.DataOutput output) {
            new com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer().serialize(value, output);
        }

        private static NbtCompound deserializeCompound(java.io.DataInput input) {
            return new com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer().deserializeCompound(input);
        }
    }
}
