package com.comphenix.protocol.reflect.instances;

import com.comphenix.protocol.reflect.accessors.Accessors;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/** Supplies already-created values by their declared type and supertypes. */
public class ExistingGenerator implements InstanceProvider {
    private final Map<Class<?>, Object> values = new HashMap<>();
    private ExistingGenerator() { }
    public static ExistingGenerator fromObjectFields(Object object) { return fromObjectFields(object, object == null ? null : object.getClass()); }
    public static ExistingGenerator fromObjectFields(Object object, Class<?> type) {
        if (object == null || type == null || !type.isAssignableFrom(object.getClass())) throw new IllegalArgumentException("object/type mismatch");
        ExistingGenerator result = new ExistingGenerator();
        for (Class<?> current=type; current!=null; current=current.getSuperclass()) for(Field field:current.getDeclaredFields()) { try { field.setAccessible(true); Object value=field.get(object); if(value!=null)result.values.put(field.getType(),value); } catch(ReflectiveOperationException ignored){} }
        return result;
    }
    public static ExistingGenerator fromObjectArray(Object[] objects) { ExistingGenerator result=new ExistingGenerator(); if(objects!=null) for(Object value:objects) if(value!=null) result.values.put(value.getClass(),value); return result; }
    @Override public Object create(@Nullable Class<?> type) { if(type==null)return null; Object exact=values.get(type); if(exact!=null)return exact; for(Map.Entry<Class<?>,Object> e:values.entrySet())if(type.isAssignableFrom(e.getKey()))return e.getValue(); return null; }
    private static final class Node {
        private final Class<?> type;
        private Object value;
        private final int level;
        private final Map<Class<?>, Node> children = new HashMap<>();
        public Node(Class<?> type, Object value, int level) { this.type = type; this.value = value; this.level = level; }
        public Node addChild(Node child) { if (child != null) children.put(child.type, child); return child; }
        public int getLevel() { return level; }
        public java.util.Collection<Node> getChildren() { return java.util.Collections.unmodifiableCollection(children.values()); }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
        public Node getChild(Class<?> type) { return children.get(type); }
    }
}
