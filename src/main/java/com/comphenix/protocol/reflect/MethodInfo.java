package com.comphenix.protocol.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Common metadata view for methods and constructors. */
public abstract class MethodInfo implements GenericDeclaration, Member {
    @Override public abstract String toString();
    public static MethodInfo fromMethod(Method method) {
        if (method == null) throw new IllegalArgumentException("method cannot be NULL");
        return new MethodBacked(method);
    }
    public static Collection<MethodInfo> fromMethods(Method[] methods) { return fromMethods(Arrays.asList(methods)); }
    public static List<MethodInfo> fromMethods(Collection<Method> methods) { List<MethodInfo> result = new ArrayList<>(); for (Method method : methods) result.add(fromMethod(method)); return result; }
    public static MethodInfo fromConstructor(Constructor<?> constructor) { if (constructor == null) throw new IllegalArgumentException("constructor cannot be NULL"); return new ConstructorBacked(constructor); }
    public static Collection<MethodInfo> fromConstructors(Constructor<?>[] constructors) { return fromConstructors(Arrays.asList(constructors)); }
    public static List<MethodInfo> fromConstructors(Collection<Constructor<?>> constructors) { List<MethodInfo> result = new ArrayList<>(); for (Constructor<?> constructor : constructors) result.add(fromConstructor(constructor)); return result; }

    public abstract String toGenericString();
    public abstract Class<?>[] getExceptionTypes();
    public abstract Class<?> getReturnType();
    public abstract Class<?>[] getParameterTypes();
    public abstract boolean isConstructor();

    private static final class MethodBacked extends MethodInfo {
        private final Method method;
        MethodBacked(Method method) { this.method = method; }
        @Override public <T extends Annotation> T getAnnotation(Class<T> type) { return method.getAnnotation(type); }
        @Override public Annotation[] getAnnotations() { return method.getAnnotations(); }
        @Override public Annotation[] getDeclaredAnnotations() { return method.getDeclaredAnnotations(); }
        @Override public String getName() { return method.getName(); }
        @Override public Class<?>[] getParameterTypes() { return method.getParameterTypes(); }
        @Override public Class<?> getDeclaringClass() { return method.getDeclaringClass(); }
        @Override public Class<?> getReturnType() { return method.getReturnType(); }
        @Override public int getModifiers() { return method.getModifiers(); }
        @Override public Class<?>[] getExceptionTypes() { return method.getExceptionTypes(); }
        @Override public TypeVariable<?>[] getTypeParameters() { return method.getTypeParameters(); }
        @Override public String toGenericString() { return method.toGenericString(); }
        @Override public boolean isSynthetic() { return method.isSynthetic(); }
        @Override public boolean isConstructor() { return false; }
        @Override public String toString() { return method.toString(); }
        @Override public int hashCode() { return method.hashCode(); }
    }

    private static final class ConstructorBacked extends MethodInfo {
        private final Constructor<?> constructor;
        ConstructorBacked(Constructor<?> constructor) { this.constructor = constructor; }
        @Override public <T extends Annotation> T getAnnotation(Class<T> type) { return constructor.getAnnotation(type); }
        @Override public Annotation[] getAnnotations() { return constructor.getAnnotations(); }
        @Override public Annotation[] getDeclaredAnnotations() { return constructor.getDeclaredAnnotations(); }
        @Override public String getName() { return constructor.getName(); }
        @Override public Class<?>[] getParameterTypes() { return constructor.getParameterTypes(); }
        @Override public Class<?> getDeclaringClass() { return constructor.getDeclaringClass(); }
        @Override public int getModifiers() { return constructor.getModifiers(); }
        @Override public Class<?>[] getExceptionTypes() { return constructor.getExceptionTypes(); }
        @Override public TypeVariable<?>[] getTypeParameters() { return constructor.getTypeParameters(); }
        @Override public String toGenericString() { return constructor.toGenericString(); }
        @Override public boolean isSynthetic() { return constructor.isSynthetic(); }
        @Override public boolean isConstructor() { return true; }
        @Override public Class<?> getReturnType() { return void.class; }
        @Override public String toString() { return constructor.toString(); }
        @Override public int hashCode() { return constructor.hashCode(); }
    }
}
