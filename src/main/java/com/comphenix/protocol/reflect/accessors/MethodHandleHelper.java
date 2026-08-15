package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Compatibility facade; the implementation intentionally uses reflection for portability. */
final class MethodHandleHelper {
    private MethodHandleHelper() { }
    public static MethodAccessor getMethodAccessor(Method method) { return Accessors.getMethodAccessor(method); }
    public static ConstructorAccessor getConstructorAccessor(Constructor<?> constructor) { return Accessors.getConstructorAccessor(constructor); }
    public static FieldAccessor getFieldAccessor(Field field) { return Accessors.getFieldAccessor(field); }
}
