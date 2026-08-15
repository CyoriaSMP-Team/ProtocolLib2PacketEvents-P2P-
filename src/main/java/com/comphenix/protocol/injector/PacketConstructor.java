/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.injector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.lang.reflect.Constructor;

/** Constructor facade. PacketEvents-backed packet construction is delegated to PacketContainer. */
public class PacketConstructor {
    public static final PacketConstructor DEFAULT = new PacketConstructor(null, Collections.emptyList());

    private final PacketType type;
    private final List<Unwrapper> unwrappers;
    private final Object[] parameterTemplate;

    protected PacketConstructor(PacketType type, List<Unwrapper> unwrappers) {
        this(type, unwrappers, null);
    }

    private PacketConstructor(PacketType type, List<Unwrapper> unwrappers, Object[] parameterTemplate) {
        this.type = type;
        this.unwrappers = unwrappers == null ? Collections.emptyList() : List.copyOf(unwrappers);
        this.parameterTemplate = parameterTemplate == null ? null : parameterTemplate.clone();
    }

    public static Class<?> getClass(Object object) {
        if (object instanceof Class<?>) return (Class<?>) object;
        if (object == null) throw new IllegalArgumentException("object cannot be null");
        return object.getClass();
    }

    public ImmutableList<Unwrapper> getUnwrappers() {
        return ImmutableList.copyOf(unwrappers);
    }

    @Deprecated
    public int getPacketID() {
        return type == null ? -1 : type.getCurrentId();
    }

    public PacketType getType() {
        return type;
    }

    public PacketConstructor withUnwrappers(List<Unwrapper> replacements) {
        return new PacketConstructor(type, replacements, parameterTemplate);
    }

    public PacketConstructor withPacket(PacketType packetType, Object[] values) {
        if (packetType == null) throw new IllegalArgumentException("packet type cannot be null");
        return new PacketConstructor(packetType, unwrappers, values);
    }

    public PacketContainer createPacket(Object... values) throws FieldAccessException {
        if (type == null) throw new IllegalStateException("withPacket must be called first");
        Object[] supplied = values == null || values.length == 0 ? parameterTemplate : values;
        if (supplied == null || supplied.length == 0) {
            return new PacketContainer(type);
        }
        Object[] converted = supplied.clone();
        for (int i = 0; i < converted.length; i++) {
            for (Unwrapper unwrapper : unwrappers) {
                Object result = unwrapper.unwrapItem(converted[i]);
                if (result != null) {
                    converted[i] = result;
                    break;
                }
            }
        }
        Class<?> wrapperClass = type.toPacketEvents() == null ? null : type.toPacketEvents().getWrapperClass();
        if (wrapperClass != null) {
            for (Constructor<?> constructor : wrapperClass.getConstructors()) {
                if (!isCompatible(constructor.getParameterTypes(), converted)) continue;
                try {
                    Object wrapper = constructor.newInstance(converted);
                    if (wrapper instanceof com.github.retrooper.packetevents.wrapper.PacketWrapper<?> packetWrapper) {
                        return new PacketContainer(type, packetWrapper);
                    }
                } catch (ReflectiveOperationException error) {
                    throw new FieldAccessException("Unable to construct " + type, error);
                }
            }
        }
        throw new FieldAccessException("PacketEvents exposes no compatible constructor for " + type);
    }

    private static boolean isCompatible(Class<?>[] parameterTypes, Object[] values) {
        if (parameterTypes.length != values.length) return false;
        for (int i = 0; i < parameterTypes.length; i++) {
            if (values[i] == null) continue;
            Class<?> actual = values[i] instanceof Class<?> clazz ? clazz : values[i].getClass();
            Class<?> expected = parameterTypes[i].isPrimitive() ? box(parameterTypes[i]) : parameterTypes[i];
            if (!expected.isAssignableFrom(actual)) return false;
        }
        return true;
    }

    private static Class<?> box(Class<?> type) {
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == boolean.class) return Boolean.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == short.class) return Short.class;
        if (type == byte.class) return Byte.class;
        if (type == char.class) return Character.class;
        return type;
    }

    public interface Unwrapper {
        Object unwrapItem(Object wrappedObject);
    }
}
