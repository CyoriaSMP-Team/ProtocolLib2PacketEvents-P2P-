/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Minimal reflection search facade used by older ProtocolLib-based plugins. */
public class FuzzyReflection {
    private final Class<?> source;
    private final boolean forceAccess;

    public FuzzyReflection(Class<?> source, boolean forceAccess) {
        this.source = source;
        this.forceAccess = forceAccess;
    }

    public static FuzzyReflection fromClass(Class<?> source) {
        return fromClass(source, false);
    }

    public static FuzzyReflection fromClass(Class<?> source, boolean forceAccess) {
        return new FuzzyReflection(source, forceAccess);
    }

    public static FuzzyReflection fromObject(Object reference) {
        return fromClass(reference.getClass(), false);
    }

    public static FuzzyReflection fromObject(Object reference, boolean forceAccess) {
        return fromClass(reference.getClass(), forceAccess);
    }

    public static <T> T getFieldValue(Object instance, Class<T> fieldClass, boolean forceAccess) {
        if (instance == null) throw new IllegalArgumentException("instance cannot be null");
        for (Class<?> current = instance.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (fieldClass.isAssignableFrom(field.getType()) || field.getType().isAssignableFrom(fieldClass)) {
                    try {
                        if (forceAccess) field.setAccessible(true);
                        return fieldClass.cast(field.get(instance));
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Unable to read " + field, e);
                    }
                }
            }
        }
        throw new IllegalArgumentException("Unable to find a field of type " + fieldClass.getName()
                + " on " + instance.getClass().getName());
    }

    public Class<?> getSource() {
        return source;
    }

    public Method getMethodByName(String nameRegex) {
        Pattern pattern = Pattern.compile(nameRegex);
        for (Method method : getMethods()) {
            if (pattern.matcher(method.getName()).matches()) return accessible(method);
        }
        throw new IllegalArgumentException("Unable to find method matching " + nameRegex);
    }

    public Method getMethodByParameters(String name, Class<?>... parameters) {
        for (Method method : getMethods()) {
            if (method.getName().equals(name) && java.util.Arrays.equals(method.getParameterTypes(), parameters)) {
                return accessible(method);
            }
        }
        throw new IllegalArgumentException("Unable to find method " + name + " on " + source.getName());
    }

    public List<Method> getMethodListByParameters(Class<?> returnType, Class<?>... parameters) {
        List<Method> out = new ArrayList<>();
        for (Method method : getMethods()) {
            if (method.getReturnType().equals(returnType)
                    && java.util.Arrays.equals(method.getParameterTypes(), parameters)) {
                out.add(accessible(method));
            }
        }
        return out;
    }

    public Field getFieldByName(String name) {
        for (Field field : getFields()) if (field.getName().equals(name)) return accessible(field);
        throw new IllegalArgumentException("Unable to find field " + name + " on " + source.getName());
    }

    public List<Field> getFieldListByType(Class<?> type) {
        List<Field> out = new ArrayList<>();
        for (Field field : getFields()) if (type.isAssignableFrom(field.getType())) out.add(accessible(field));
        return out;
    }

    private List<Method> getMethods() {
        List<Method> out = new ArrayList<>();
        for (Class<?> current = source; current != null; current = current.getSuperclass()) {
            for (Method method : forceAccess ? current.getDeclaredMethods() : current.getMethods()) {
                if (!out.contains(method)) out.add(method);
            }
            if (!forceAccess) break;
        }
        return out;
    }

    private List<Field> getFields() {
        List<Field> out = new ArrayList<>();
        for (Class<?> current = source; current != null; current = current.getSuperclass()) {
            for (Field field : forceAccess ? current.getDeclaredFields() : current.getFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !out.contains(field)) out.add(field);
            }
            if (!forceAccess) break;
        }
        return out;
    }

    private <T extends java.lang.reflect.AccessibleObject> T accessible(T object) {
        if (forceAccess) object.setAccessible(true);
        return object;
    }
}
