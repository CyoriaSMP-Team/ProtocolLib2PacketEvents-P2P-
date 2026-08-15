package com.comphenix.protocol.wrappers.nbt;

import java.util.Arrays;
import java.util.Map;

final class NbtValues {
    private NbtValues() {
    }

    static Object copyValue(Object value) {
        if (value instanceof byte[]) {
            return Arrays.copyOf((byte[]) value, ((byte[]) value).length);
        }
        if (value instanceof int[]) {
            return Arrays.copyOf((int[]) value, ((int[]) value).length);
        }
        if (value instanceof long[]) {
            return Arrays.copyOf((long[]) value, ((long[]) value).length);
        }
        if (value instanceof NbtBase<?>) {
            return ((NbtBase<?>) value).deepClone();
        }
        return value;
    }

    static NbtBase<?> fromObject(String name, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof NbtBase<?>) {
            NbtBase<?> copy = ((NbtBase<?>) value).deepClone();
            copy.setName(name);
            return copy;
        }
        if (value instanceof Map<?, ?>) {
            NbtCompound compound = NbtFactory.ofCompound(name);
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                compound.putObject(String.valueOf(entry.getKey()), entry.getValue());
            }
            return compound;
        }
        if (value instanceof Iterable<?>) {
            java.util.ArrayList<Object> values = new java.util.ArrayList<>();
            for (Object element : (Iterable<?>) value) {
                values.add(element);
            }
            return NbtFactory.ofList(name, values);
        }
        if (value instanceof Boolean) {
            return NbtFactory.of(name, (byte) ((Boolean) value ? 1 : 0));
        }
        if (value instanceof Character) {
            return NbtFactory.of(name, String.valueOf(value));
        }
        NbtType type = NbtType.getTypeFromClass(value.getClass());
        @SuppressWarnings({"unchecked", "rawtypes"})
        NbtBase<?> result = NbtFactory.ofWrapper(type, name, value);
        return result;
    }
}
