package com.comphenix.protocol.wrappers;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

final class GuavaWrappers {
    public static <TKey,TValue> Multimap<TKey,TValue> getBukkitMultimap(Multimap<TKey,TValue> value){return value;}
    public static <TValue> Multiset<TValue> getBukkitMultiset(Multiset<TValue> value){return value;}
}
