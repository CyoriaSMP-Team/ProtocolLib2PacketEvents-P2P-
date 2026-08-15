package com.comphenix.protocol.wrappers;

import com.google.common.collect.Multimap;
import com.google.common.collect.ArrayListMultimap;

public class MutablePropertyMap {
    private final Multimap<String,Object> properties=ArrayListMultimap.create();
    public MutablePropertyMap() { }
    public MutablePropertyMap(Multimap<String,?> values){if(values!=null)for(var e:values.entries())properties.put(e.getKey(),e.getValue());}
    public Multimap<String,Object> asMultimap(){return properties;}
}
