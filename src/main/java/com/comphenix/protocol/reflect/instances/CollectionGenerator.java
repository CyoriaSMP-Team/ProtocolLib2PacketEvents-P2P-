package com.comphenix.protocol.reflect.instances;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nullable;

public class CollectionGenerator implements InstanceProvider {
    public static final CollectionGenerator INSTANCE = new CollectionGenerator();
    @Override public Object create(@Nullable Class<?> type) {
        if (type == null || !type.isInterface()) return null;
        if (type == Collection.class || type == List.class) return new ArrayList<>();
        if (type == Set.class) return new HashSet<>();
        if (type == Map.class) return new HashMap<>();
        if (type == SortedSet.class) return new TreeSet<>();
        if (type == SortedMap.class) return new TreeMap<>();
        if (type == Queue.class) return new LinkedList<>();
        return null;
    }
}
