package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.google.common.base.Function;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TroveWrapper {
    public TroveWrapper() { }
    public static FieldAccessor wrapMapField(FieldAccessor accessor){return accessor;} public static FieldAccessor wrapMapField(FieldAccessor accessor,Function<Integer,Integer> ignored){return accessor;}
    public static <K,V> Map<K,V> getDecoratedMap(Object value){return value instanceof Map<?,?> map?(Map<K,V>)map:Collections.emptyMap();} public static <V> Set<V> getDecoratedSet(Object value){return value instanceof Set<?> set?(Set<V>)set:Collections.emptySet();} public static <V> List<V> getDecoratedList(Object value){return value instanceof List<?> list?(List<V>)list:Collections.emptyList();} public static boolean isTroveClass(Class<?> type){return type!=null&&type.getName().startsWith("gnu.trove");}
    static class CannotFindTroveNoEntryValue extends RuntimeException { }
}
