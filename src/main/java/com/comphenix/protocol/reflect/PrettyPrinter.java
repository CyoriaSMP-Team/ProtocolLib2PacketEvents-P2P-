package com.comphenix.protocol.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/** Lightweight object printer retained for diagnostics and error reports. */
public class PrettyPrinter {
    public static final int RECURSE_DEPTH = 3;

    public static String printObject(Object object) throws IllegalAccessException {
        if (object == null) throw new IllegalArgumentException("object cannot be NULL");
        return printObject(object, object.getClass(), Object.class);
    }
    public static String printObject(Object object, Class<?> start, Class<?> stop) throws IllegalAccessException { return printObject(object, start, stop, RECURSE_DEPTH); }
    public static String printObject(Object object, Class<?> start, Class<?> stop, int hierarchyDepth) throws IllegalAccessException { return printObject(object, start, stop, hierarchyDepth, ObjectPrinter.DEFAULT); }
    public static String printObject(Object object, Class<?> start, Class<?> stop, int hierarchyDepth, ObjectPrinter printer) throws IllegalAccessException {
        if (object == null) throw new IllegalArgumentException("object cannot be NULL");
        StringBuilder result = new StringBuilder("{ ");
        append(result, object, start, stop, hierarchyDepth, printer);
        return result.append(" }").toString();
    }
    private static void append(StringBuilder result, Object value, Class<?> type, Class<?> stop, int depth, ObjectPrinter printer) throws IllegalAccessException {
        if (printer.print(result, value)) return;
        if (value == null || depth <= 0 || type.isPrimitive() || value instanceof Number || value instanceof Boolean || value instanceof Character || value instanceof String || value.getClass().isEnum()) { result.append(String.valueOf(value)); return; }
        if (value.getClass().isArray()) { result.append('['); for (int i=0;i<Array.getLength(value);i++) { if(i>0)result.append(", "); append(result, Array.get(value,i), value.getClass().getComponentType(), stop, depth-1, printer); } result.append(']'); return; }
        if (value instanceof Iterable<?>) { result.append('['); boolean first=true; for(Object entry:(Iterable<?>)value){if(!first)result.append(", "); first=false; append(result,entry,entry==null?Object.class:entry.getClass(),stop,depth-1,printer);} result.append(']'); return; }
        if (value instanceof Map<?,?>) { result.append(value); return; }
        result.append("{ "); boolean first=true; for(Class<?> current=type; current!=null && current!=stop; current=current.getSuperclass()) for(Field field:current.getDeclaredFields()){ if(Modifier.isStatic(field.getModifiers()))continue; field.setAccessible(true); if(!first)result.append(", "); first=false; result.append(field.getName()).append('=').append(String.valueOf(field.get(value))); } result.append(" }");
    }
    public interface ObjectPrinter { ObjectPrinter DEFAULT = (output, value) -> false; boolean print(StringBuilder output, Object value); }
}
