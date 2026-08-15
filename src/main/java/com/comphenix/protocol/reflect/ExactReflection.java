package com.comphenix.protocol.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/** Exact hierarchy lookup facade used by ProtocolLib consumers. */
public class ExactReflection {
    private final Class<?> source;
    private final boolean forceAccess;

    private ExactReflection(Class<?> source, boolean forceAccess) {
        if (source == null) throw new IllegalArgumentException("source class cannot be NULL");
        this.source = source;
        this.forceAccess = forceAccess;
    }

    public static ExactReflection fromClass(Class<?> source, boolean forceAccess) { return new ExactReflection(source, forceAccess); }
    public static ExactReflection fromObject(Object reference, boolean forceAccess) { return new ExactReflection(reference.getClass(), forceAccess); }

    public Method getMethod(String name, Class<?>... parameters) {
        Method result = findMethod(name, parameters);
        if (result == null) throw new IllegalArgumentException("Unable to find method " + name + " in " + source.getName());
        return result;
    }
    public Method findMethod(String name, Class<?>... parameters) {
        for (Class<?> current = source; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if ((name == null || method.getName().equals(name)) && Arrays.equals(method.getParameterTypes(), parameters)
                        && (forceAccess || Modifier.isPublic(method.getModifiers()))) {
                    if (forceAccess) method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }
    public Field getField(String name) { Field result = findField(name); if (result == null) throw new IllegalArgumentException("Unable to find field " + name); return result; }
    public Field findField(String name) {
        for (Class<?> current = source; current != null; current = current.getSuperclass()) {
            try { Field field = current.getDeclaredField(name); if (forceAccess) field.setAccessible(true); if (forceAccess || Modifier.isPublic(field.getModifiers())) return field; }
            catch (NoSuchFieldException ignored) { }
        }
        return null;
    }
    public Constructor<?> getConstructor(Class<?>... parameters) { Constructor<?> result = findConstructor(parameters); if (result == null) throw new IllegalArgumentException("Unable to find constructor"); return result; }
    public Constructor<?> findConstructor(Class<?>... parameters) { try { Constructor<?> result = source.getDeclaredConstructor(parameters); if (forceAccess) result.setAccessible(true); return forceAccess || Modifier.isPublic(result.getModifiers()) ? result : null; } catch (NoSuchMethodException ignored) { return null; } }
    public ExactReflection forceAccess() { return new ExactReflection(source, true); }
    public boolean isForceAccess() { return forceAccess; }
    public Class<?> getSource() { return source; }
}
