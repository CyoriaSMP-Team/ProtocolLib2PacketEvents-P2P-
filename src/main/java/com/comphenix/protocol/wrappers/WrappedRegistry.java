package com.comphenix.protocol.wrappers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WrappedRegistry {
    private final Map<MinecraftKey,Object> values=new ConcurrentHashMap<>();
    public Object get(MinecraftKey key){return values.get(key);} public Object get(String key){return get(new MinecraftKey(key));} public MinecraftKey getKey(Object value){for(var e:values.entrySet())if(java.util.Objects.equals(e.getValue(),value))return e.getKey();return null;} public int getId(MinecraftKey key){return key==null?-1:Math.abs(key.hashCode());} public int getId(String key){return getId(new MinecraftKey(key));} public int getId(Object value){MinecraftKey key=getKey(value);return getId(key);} public Object getHolder(Object value){return value;}
    public static WrappedRegistry getAttributeRegistry(){return new WrappedRegistry();} public static WrappedRegistry getDimensionRegistry(){return new WrappedRegistry();} public static WrappedRegistry getSoundRegistry(){return new WrappedRegistry();} public static WrappedRegistry getRegistry(Class<?> type){return new WrappedRegistry();}
}
