package com.comphenix.protocol.wrappers.nbt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The wire-level NBT tag types. */
public enum NbtType {
    TAG_END(0, Void.class),
    TAG_BYTE(1, byte.class),
    TAG_SHORT(2, short.class),
    TAG_INT(3, int.class),
    TAG_LONG(4, long.class),
    TAG_FLOAT(5, float.class),
    TAG_DOUBLE(6, double.class),
    TAG_BYTE_ARRAY(7, byte[].class),
    TAG_INT_ARRAY(11, int[].class),
    TAG_STRING(8, String.class),
    TAG_LIST(9, List.class),
    TAG_COMPOUND(10, Map.class),
    TAG_LONG_ARRAY(12, long[].class);

    private static final NbtType[] BY_ID = new NbtType[13];
    private static final Map<Class<?>, NbtType> BY_CLASS = new HashMap<>();

    static {
        for (NbtType type : values()) {
            BY_ID[type.rawId] = type;
            BY_CLASS.put(type.valueType, type);
        }
        BY_CLASS.put(Byte.class, TAG_BYTE);
        BY_CLASS.put(Short.class, TAG_SHORT);
        BY_CLASS.put(Integer.class, TAG_INT);
        BY_CLASS.put(Long.class, TAG_LONG);
        BY_CLASS.put(Float.class, TAG_FLOAT);
        BY_CLASS.put(Double.class, TAG_DOUBLE);
        BY_CLASS.put(NbtList.class, TAG_LIST);
        BY_CLASS.put(NbtCompound.class, TAG_COMPOUND);
    }

    private final int rawId;
    private final Class<?> valueType;

    NbtType(int rawId, Class<?> valueType) {
        this.rawId = rawId;
        this.valueType = valueType;
    }

    public boolean isComposite() {
        return this == TAG_LIST || this == TAG_COMPOUND;
    }

    public int getRawID() {
        return rawId;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public static NbtType getTypeFromID(int rawID) {
        if (rawID < 0 || rawID >= BY_ID.length || BY_ID[rawID] == null) {
            throw new IllegalArgumentException("Unrecognized raw ID " + rawID);
        }
        return BY_ID[rawID];
    }

    public static NbtType getTypeFromClass(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be NULL.");
        }
        NbtType exact = BY_CLASS.get(clazz);
        if (exact != null) {
            return exact;
        }
        for (Map.Entry<Class<?>, NbtType> entry : BY_CLASS.entrySet()) {
            if (entry.getKey().isAssignableFrom(clazz)) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("No NBT tag can represent a " + clazz);
    }
}
