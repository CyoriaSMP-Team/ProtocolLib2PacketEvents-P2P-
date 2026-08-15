package com.comphenix.protocol.wrappers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

final class GuavaReflection {
    public static <TKey,TValue> Multimap<TKey,TValue> getBukkitMultimap(Object value){return value instanceof Multimap<?,?> map?(Multimap<TKey,TValue>)map:HashMultimap.create();}
    public static <TValue> Multiset<TValue> getBukkitMultiset(Object value){return value instanceof Multiset<?> set?(Multiset<TValue>)set:HashMultiset.create();}
}
