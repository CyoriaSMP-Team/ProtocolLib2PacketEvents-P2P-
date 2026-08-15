package com.comphenix.protocol;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PacketTypeEnum implements Iterable<PacketType> {
    protected final Set<PacketType> members=new HashSet<>();
    public PacketTypeEnum(){registerAll();}
    protected void registerAll(){for(Field field:getClass().getFields())try{if(Modifier.isStatic(field.getModifiers())&&PacketType.class.isAssignableFrom(field.getType())){PacketType type=(PacketType)field.get(null);if(type!=null)members.add(type);}}catch(ReflectiveOperationException ignored){}}
    public boolean registerMember(PacketType instance,String name){return instance!=null&&members.add(instance);}
    public boolean hasMember(PacketType member){return members.contains(member);}
    public Set<PacketType> values(){return new HashSet<>(members);}
    public Iterator<PacketType> iterator(){return members.iterator();}
}
