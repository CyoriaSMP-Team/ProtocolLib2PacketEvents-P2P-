package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Array;
import java.util.Optional;
import javax.annotation.Nullable;

public class PrimitiveGenerator implements InstanceProvider {
    @Deprecated public static final String STRING_DEFAULT = "";
    public static final PrimitiveGenerator INSTANCE = new PrimitiveGenerator();
    private final String stringDefault;
    public PrimitiveGenerator() { this(""); }
    @Deprecated public PrimitiveGenerator(String stringDefault) { this.stringDefault = stringDefault; }
    @Deprecated public String getStringDefault() { return stringDefault; }
    @Override public Object create(@Nullable Class<?> type) {
        if (type == null) return null;
        if (type == String.class) return stringDefault;
        if (type == Optional.class) return Optional.empty();
        if (type.isArray()) return Array.newInstance(type.getComponentType(), 0);
        if (type.isEnum()) { Object[] values=type.getEnumConstants(); return values==null||values.length==0?null:values[0]; }
        if (type.isPrimitive() || type == Boolean.class || type == Byte.class || type == Short.class || type == Integer.class || type == Long.class || type == Float.class || type == Double.class || type == Character.class) return primitiveDefault(type);
        return null;
    }
    private static Object primitiveDefault(Class<?> type) { if(type==boolean.class||type==Boolean.class)return false; if(type==char.class||type==Character.class)return '\0'; if(type==byte.class||type==Byte.class)return (byte)0; if(type==short.class||type==Short.class)return (short)0; if(type==int.class||type==Integer.class)return 0; if(type==long.class||type==Long.class)return 0L; if(type==float.class||type==Float.class)return 0F; if(type==double.class||type==Double.class)return 0D; return null; }
}
