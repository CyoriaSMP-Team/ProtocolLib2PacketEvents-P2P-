package com.comphenix.protocol.reflect.cloning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionCloner implements Cloner {
    private final Cloner defaultCloner; public CollectionCloner(Cloner defaultCloner){this.defaultCloner=defaultCloner;}
    @Override public boolean canClone(Object source){return source instanceof Collection<?>||source instanceof Map<?,?>;}
    @Override public Object clone(Object source){if(source instanceof Map<?,?>){Map<Object,Object> out=new HashMap<>();for(Map.Entry<?,?> e:((Map<?,?>)source).entrySet())out.put(defaultCloner.canClone(e.getKey())?defaultCloner.clone(e.getKey()):e.getKey(),defaultCloner.canClone(e.getValue())?defaultCloner.clone(e.getValue()):e.getValue());return out;}Collection<?> input=(Collection<?>)source;Collection<Object> out=source instanceof Set?new LinkedHashSet<>():new ArrayList<>(input.size());for(Object value:input)out.add(defaultCloner.canClone(value)?defaultCloner.clone(value):value);return out;}
    public Cloner getDefaultCloner(){return defaultCloner;}
}
