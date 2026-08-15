package com.comphenix.protocol.reflect.cloning;

import com.comphenix.protocol.reflect.ObjectAllocator;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldCloner implements Cloner {
    private final Cloner defaultCloner; private final Object instanceProvider;
    public FieldCloner(Cloner defaultCloner, com.comphenix.protocol.reflect.instances.InstanceProvider instanceProvider){this.defaultCloner=defaultCloner;this.instanceProvider=instanceProvider;}
    @Override public boolean canClone(Object source){return source!=null&&!source.getClass().isEnum()&&!source.getClass().isArray()&&!source.getClass().isPrimitive();}
    @Override public Object clone(Object source){Object result;try{result=ObjectAllocator.allocate(source.getClass());}catch(RuntimeException ex){return source;}for(Class<?> current=source.getClass();current!=null&&current!=Object.class;current=current.getSuperclass())for(Field field:current.getDeclaredFields()){if(Modifier.isStatic(field.getModifiers()))continue;try{field.setAccessible(true);Object value=field.get(source);field.set(result,defaultCloner.canClone(value)?defaultCloner.clone(value):value);}catch(ReflectiveOperationException|RuntimeException ignored){}}return result;}
    protected void defaultTransform(Object source,Object destination,Field field){ }
    public Cloner getDefaultCloner(){return defaultCloner;} public com.comphenix.protocol.reflect.instances.InstanceProvider getInstanceProvider(){return (com.comphenix.protocol.reflect.instances.InstanceProvider)instanceProvider;}
}
