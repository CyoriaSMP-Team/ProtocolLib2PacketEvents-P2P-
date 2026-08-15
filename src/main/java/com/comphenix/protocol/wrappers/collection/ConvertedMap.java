package com.comphenix.protocol.wrappers.collection;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** A live map view that converts values while retaining the original keys. */
public abstract class ConvertedMap<Key, VInner, VOuter> extends AbstractConverted<VInner, VOuter> implements Map<Key, VOuter> {
    private final Map<Key, VInner> inner;

    public ConvertedMap(Map<Key, VInner> inner) {
        if (inner == null) throw new IllegalArgumentException("Inner map cannot be NULL");
        this.inner = inner;
    }

    protected VOuter toOuter(Key key, VInner value) { return toOuter(value); }
    protected VInner toInner(Key key, VOuter value) { return toInner(value); }

    @Override public void clear() { inner.clear(); }
    @Override public boolean containsKey(Object key) { return inner.containsKey(key); }
    @Override @SuppressWarnings("unchecked") public boolean containsValue(Object value) { return inner.containsValue(toInner((VOuter) value)); }
    @Override public Set<Entry<Key, VOuter>> entrySet() { return convertedEntrySet(inner.entrySet(), this::toInner, this::toOuter); }
    @Override @SuppressWarnings("unchecked") public VOuter get(Object key) { return toOuter((Key) key, inner.get(key)); }
    @Override public boolean isEmpty() { return inner.isEmpty(); }
    @Override public Set<Key> keySet() { return inner.keySet(); }
    @Override public VOuter put(Key key, VOuter value) { return toOuter(key, inner.put(key, toInner(key, value))); }
    @Override public void putAll(Map<? extends Key, ? extends VOuter> values) { for (Entry<? extends Key, ? extends VOuter> entry : values.entrySet()) put(entry.getKey(), entry.getValue()); }
    @Override @SuppressWarnings("unchecked") public VOuter remove(Object key) { return toOuter((Key) key, inner.remove(key)); }
    @Override public int size() { return inner.size(); }
    @Override public Collection<VOuter> values() { return new ConvertedCollection<VInner, VOuter>(inner.values()) {
        @Override protected VInner toInner(VOuter outer) { return ConvertedMap.this.toInner(outer); }
        @Override protected VOuter toOuter(VInner value) { return ConvertedMap.this.toOuter(value); }
    }; }
    @Override public String toString() { return entrySet().toString(); }

    static <Key, VInner, VOuter> Set<Entry<Key, VOuter>> convertedEntrySet(
            Collection<Entry<Key, VInner>> entries,
            BiFunction<Key, VOuter, VInner> innerFunction,
            BiFunction<Key, VInner, VOuter> outerFunction) {
        return new ConvertedSet<Entry<Key, VInner>, Entry<Key, VOuter>>(entries) {
            @Override protected Entry<Key, VInner> toInner(Entry<Key, VOuter> outer) {
                return new SimpleEntry<>(outer.getKey(), innerFunction.apply(outer.getKey(), outer.getValue()));
            }
            @Override protected Entry<Key, VOuter> toOuter(Entry<Key, VInner> inner) {
                return new SimpleEntry<>(inner.getKey(), outerFunction.apply(inner.getKey(), inner.getValue()));
            }
        };
    }

    private static final class SimpleEntry<K, V> extends AbstractMap.SimpleEntry<K, V> {
        SimpleEntry(K key, V value) { super(key, value); }
    }
}
