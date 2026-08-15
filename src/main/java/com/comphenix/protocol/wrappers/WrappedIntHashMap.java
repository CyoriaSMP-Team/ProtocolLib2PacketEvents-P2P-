package com.comphenix.protocol.wrappers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WrappedIntHashMap extends AbstractWrapper {
    private final Map<Integer,Object> values=new ConcurrentHashMap<>();
    private WrappedIntHashMap(){super(WrappedIntHashMap.class);handle=this;}
    public static WrappedIntHashMap newMap(){return new WrappedIntHashMap();} public static WrappedIntHashMap fromHandle(Object handle){return handle instanceof WrappedIntHashMap v?v:newMap();}
    public void put(int key,Object value){if(value==null)values.remove(key);else values.put(key,value);} public Object get(int key){return values.get(key);} public Object remove(int key){return values.remove(key);}
}
