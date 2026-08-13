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

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allocates instances of a class without invoking any of its declared constructors, mirroring
 * how the real ProtocolLib allocates raw NMS packet objects. This is required because
 * PacketEvents' {@code PacketWrapper} subclasses only expose decode-from-event or
 * fully-parameterized constructors, never a bare no-arg one.
 * <p>
 * The primary mechanism is {@code jdk.internal.reflect.ReflectionFactory}, the same one Java
 * serialization uses to revive objects. It is reached reflectively so this class still compiles
 * and loads on JDKs where the package is not exported. {@code sun.misc.Unsafe.allocateInstance}
 * is kept only as a fallback: it is deprecated for removal, so it must not be the default path
 * on a plugin expected to keep working on future JVMs.
 */
public final class ObjectAllocator {

    private static final Object REFLECTION_FACTORY;
    private static final java.lang.reflect.Method NEW_CONSTRUCTOR_FOR_SERIALIZATION;
    private static final Constructor<Object> OBJECT_CONSTRUCTOR;
    private static final Object UNSAFE;
    private static final java.lang.reflect.Method ALLOCATE_INSTANCE;

    /** Serialization constructors are relatively costly to derive; reuse them per class. */
    private static final Map<Class<?>, Constructor<?>> CACHE = new ConcurrentHashMap<>();

    static {
        Object factory = null;
        java.lang.reflect.Method newCtor = null;
        Constructor<Object> objectCtor = null;
        // sun.reflect.ReflectionFactory is exported by the jdk.unsupported module and is
        // reachable from an unnamed classloader; the jdk.internal.reflect one is not exported
        // and throws IllegalAccessException on modern JDKs, so it is only a secondary attempt.
        for (String className : new String[]{
                "sun.reflect.ReflectionFactory",
                "jdk.internal.reflect.ReflectionFactory"}) {
            try {
                Class<?> factoryClass = Class.forName(className);
                Object candidateFactory = factoryClass.getMethod("getReflectionFactory").invoke(null);
                java.lang.reflect.Method candidateMethod =
                        factoryClass.getMethod("newConstructorForSerialization", Class.class, Constructor.class);
                Constructor<Object> candidateObjectCtor = Object.class.getDeclaredConstructor();
                // Prove the whole path works before committing to it, so a JDK that exposes the
                // class but refuses the call does not leave us with a broken primary strategy.
                Constructor<?> probe = (Constructor<?>) candidateMethod
                        .invoke(candidateFactory, java.util.ArrayList.class, candidateObjectCtor);
                probe.setAccessible(true);
                probe.newInstance();

                factory = candidateFactory;
                newCtor = candidateMethod;
                objectCtor = candidateObjectCtor;
                break;
            } catch (Throwable t) {
                factory = null;
                newCtor = null;
                objectCtor = null;
            }
        }
        REFLECTION_FACTORY = factory;
        NEW_CONSTRUCTOR_FOR_SERIALIZATION = newCtor;
        OBJECT_CONSTRUCTOR = objectCtor;

        Object unsafe = null;
        java.lang.reflect.Method allocate = null;
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = f.get(null);
            allocate = unsafeClass.getMethod("allocateInstance", Class.class);
        } catch (Throwable t) {
            unsafe = null;
            allocate = null;
        }
        UNSAFE = unsafe;
        ALLOCATE_INSTANCE = allocate;
    }

    private ObjectAllocator() {
    }

    public static boolean isAvailable() {
        return REFLECTION_FACTORY != null || UNSAFE != null;
    }

    /** Describes which mechanism is in use, for start-up diagnostics. */
    public static String getStrategy() {
        if (REFLECTION_FACTORY != null) {
            return "ReflectionFactory";
        }
        if (UNSAFE != null) {
            return "sun.misc.Unsafe (deprecated fallback)";
        }
        return "unavailable";
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocate(Class<T> clazz) {
        if (REFLECTION_FACTORY != null) {
            try {
                Constructor<?> ctor = CACHE.computeIfAbsent(clazz, ObjectAllocator::deriveConstructor);
                return (T) ctor.newInstance();
            } catch (Throwable t) {
                // Fall through to Unsafe rather than failing outright.
            }
        }
        if (UNSAFE != null) {
            try {
                return (T) ALLOCATE_INSTANCE.invoke(UNSAFE, clazz);
            } catch (Throwable t) {
                throw new IllegalStateException("Failed to allocate an instance of " + clazz.getName(), t);
            }
        }
        throw new IllegalStateException("No constructor-bypassing allocation strategy is available on this JVM; "
                + "cannot create " + clazz.getName() + " without invoking a constructor");
    }

    private static Constructor<?> deriveConstructor(Class<?> clazz) {
        try {
            Constructor<?> ctor = (Constructor<?>) NEW_CONSTRUCTOR_FOR_SERIALIZATION
                    .invoke(REFLECTION_FACTORY, clazz, OBJECT_CONSTRUCTOR);
            ctor.setAccessible(true);
            return ctor;
        } catch (Throwable t) {
            throw new IllegalStateException("Cannot derive a serialization constructor for " + clazz.getName(), t);
        }
    }
}
