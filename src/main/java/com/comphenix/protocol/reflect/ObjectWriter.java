package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** Copies non-static fields between compatible object instances. */
public class ObjectWriter {
    public void copyTo(Object source, Object destination, Class<?> commonType) {
        if (source == null || destination == null || commonType == null) throw new IllegalArgumentException("source, destination and commonType are required");
        for (Class<?> current = commonType; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                try { transformField(field, source, destination); }
                catch (ReflectiveOperationException ex) { throw new IllegalStateException("Unable to copy " + field, ex); }
            }
        }
    }

    protected void transformField(StructureModifier<Object> modifierSource, StructureModifier<Object> modifierDest, int fieldIndex) {
        modifierDest.write(fieldIndex, modifierSource.read(fieldIndex));
    }

    private void transformField(Field field, Object source, Object destination) throws IllegalAccessException {
        field.set(destination, field.get(source));
    }
}
