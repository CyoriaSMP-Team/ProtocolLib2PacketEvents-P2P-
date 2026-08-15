/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 *
 * Copyright (C) 2026 CyoriaSMP Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reflection-based accessor over a target object's declared instance fields, filtered by type
 * and ordered by declaration order. This is the same technique the real ProtocolLib uses
 * against raw NMS packet classes; here it is applied to PacketEvents' {@code PacketWrapper}
 * instances instead, so no NMS access is needed.
 * <p>
 * A modifier may additionally carry an {@link EquivalentConverter}, in which case the field
 * type selected is the converter's {@linkplain EquivalentConverter#getGenericType() generic
 * type} while {@link #read(int)}/{@link #write(int, Object)} speak the converter's
 * ProtocolLib-facing type.
 */
public class StructureModifier<T> {

    private final Object target;
    private final Class<?> fieldType;
    private final EquivalentConverter<T> converter;
    private final List<Field> fields;

    public StructureModifier(Object target, Class<?> fieldType) {
        this(target, fieldType, null);
    }

    public StructureModifier(Object target, Class<?> fieldType, EquivalentConverter<T> converter) {
        this.target = target;
        this.fieldType = fieldType;
        this.converter = converter;
        List<Field> collected = new ArrayList<>();
        collectFields(target == null ? null : target.getClass(), fieldType, collected);
        this.fields = Collections.unmodifiableList(collected);
    }

    private static void collectFields(Class<?> clazz, Class<?> fieldType, List<Field> out) {
        if (clazz == null || clazz == Object.class) {
            return;
        }
        // Walk the hierarchy top-down so field order matches declaration order, the way
        // ProtocolLib's own StructureModifier orders packet fields.
        collectFields(clazz.getSuperclass(), fieldType, out);
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (matches(field.getType(), fieldType)) {
                field.setAccessible(true);
                out.add(field);
            }
        }
    }

    private static boolean matches(Class<?> declared, Class<?> requested) {
        if (requested == Object.class) {
            return true;
        }
        return box(declared).equals(box(requested));
    }

    private static Class<?> box(Class<?> c) {
        if (!c.isPrimitive()) return c;
        if (c == int.class) return Integer.class;
        if (c == long.class) return Long.class;
        if (c == short.class) return Short.class;
        if (c == byte.class) return Byte.class;
        if (c == boolean.class) return Boolean.class;
        if (c == float.class) return Float.class;
        if (c == double.class) return Double.class;
        if (c == char.class) return Character.class;
        return c;
    }

    /** Number of fields of the selected type on the target packet. */
    public int size() {
        return fields.size();
    }

    /** The fields this modifier exposes, in declaration order. */
    public List<Field> getFields() {
        return fields;
    }

    @SuppressWarnings("unchecked")
    public T read(int index) {
        checkBounds(index);
        Object raw;
        try {
            raw = fields.get(index).get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot read field " + fields.get(index), e);
        }
        return converter == null ? (T) raw : converter.getSpecific(raw);
    }

    /** Reads a value, returning {@code fallback} when the field holds {@code null}. */
    public T readSafely(int index, T fallback) {
        if (index < 0 || index >= fields.size()) {
            return fallback;
        }
        T value = read(index);
        return value == null ? fallback : value;
    }

    /** Reads a value, returning {@code null} when the index is absent or the value is null. */
    public T readSafely(int index) {
        return readSafely(index, null);
    }

    public StructureModifier<T> write(int index, T value) {
        checkBounds(index);
        Object raw = converter == null ? value : converter.getGeneric(value);
        try {
            fields.get(index).set(target, raw);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot write field " + fields.get(index), e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot write " + (raw == null ? "null" : raw.getClass().getName())
                    + " into field " + fields.get(index) + " of type " + fields.get(index).getType().getName(), e);
        }
        return this;
    }

    /** Writes only if the index exists, so optional fields do not need a size() guard. */
    public StructureModifier<T> writeSafely(int index, T value) {
        if (index >= 0 && index < fields.size()) {
            write(index, value);
        }
        return this;
    }

    /** Resets every selected field to its Java default value. */
    @SuppressWarnings("unchecked")
    public StructureModifier<T> writeDefaults() {
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            Object value = field.getType().isPrimitive() ? primitiveDefault(field.getType()) : null;
            write(i, (T) value);
        }
        return this;
    }

    private static Object primitiveDefault(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        return null;
    }

    /** A modifier over the fields of another raw type on the same packet. */
    public <U> StructureModifier<U> withType(Class<U> type) {
        return new StructureModifier<>(target, type);
    }

    /** A modifier over the fields the given converter applies to, speaking its API type. */
    public <U> StructureModifier<U> withType(Class<U> type, EquivalentConverter<U> converter) {
        return new StructureModifier<>(target, converter.getGenericType(), converter);
    }

    /** The object whose fields this modifier reads and writes. */
    public Object getTarget() {
        return target;
    }

    private void checkBounds(int index) {
        if (index < 0 || index >= fields.size()) {
            throw new IndexOutOfBoundsException(
                    "No field of type " + fieldType.getSimpleName() + " at index " + index
                            + " (found " + fields.size() + " matching field(s) on "
                            + (target == null ? "null" : target.getClass().getName()) + ")");
        }
    }
}
