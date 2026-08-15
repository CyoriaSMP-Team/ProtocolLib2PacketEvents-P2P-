package com.comphenix.protocol.wrappers.collection;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Converted view over a Guava Multimap. */
public abstract class ConvertedMultimap<Key, VInner, VOuter> extends AbstractConverted<VInner, VOuter> implements Multimap<Key, VOuter> {
    private final Multimap<Key, VInner> inner;

    public ConvertedMultimap(Multimap<Key, VInner> inner) {
        if (inner == null) throw new IllegalArgumentException("inner map cannot be NULL");
        this.inner = inner;
    }

    protected Collection<VOuter> toOuterCollection(Collection<VInner> values) {
        return new ConvertedCollection<VInner, VOuter>(values) {
            @Override protected VInner toInner(VOuter outer) { return ConvertedMultimap.this.toInner(outer); }
            @Override protected VOuter toOuter(VInner value) { return ConvertedMultimap.this.toOuter(value); }
        };
    }
    protected Collection<VInner> toInnerCollection(Collection<VOuter> values) {
        return new ConvertedCollection<VOuter, VInner>(values) {
            @Override protected VOuter toInner(VInner outer) { return ConvertedMultimap.this.toOuter(outer); }
            @Override protected VInner toOuter(VOuter value) { return ConvertedMultimap.this.toInner(value); }
        };
    }
    @SuppressWarnings("unchecked") protected Object toInnerObject(Object value) { return toInner((VOuter) value); }

    @Override public int size() { return inner.size(); }
    @Override public boolean isEmpty() { return inner.isEmpty(); }
    @Override public boolean containsKey(Object key) { return inner.containsKey(key); }
    @Override public boolean containsValue(Object value) { return inner.containsValue(toInnerObject(value)); }
    @Override public boolean containsEntry(Object key, Object value) { return inner.containsEntry(key, toInnerObject(value)); }
    @Override public boolean put(Key key, VOuter value) { return inner.put(key, toInner(value)); }
    @Override public boolean remove(Object key, Object value) { return inner.remove(key, toInnerObject(value)); }
    @Override public boolean putAll(Key key, Iterable<? extends VOuter> values) { boolean changed = false; for (VOuter value : values) changed |= put(key, value); return changed; }
    @Override @SuppressWarnings({"rawtypes", "unchecked"}) public boolean putAll(Multimap<? extends Key, ? extends VOuter> values) { boolean changed = false; for (Map.Entry entry : values.entries()) changed |= put((Key) entry.getKey(), (VOuter) entry.getValue()); return changed; }
    @Override public Collection<VOuter> replaceValues(Key key, Iterable<? extends VOuter> values) { java.util.ArrayList<VOuter> old = new java.util.ArrayList<>(get(key)); removeAll(key); putAll(key, values); return old; }
    @Override public Collection<VOuter> removeAll(Object key) { return toOuterCollection(inner.removeAll(key)); }
    @Override public void clear() { inner.clear(); }
    @Override public Collection<VOuter> get(Key key) { return toOuterCollection(inner.get(key)); }
    @Override public Set<Key> keySet() { return inner.keySet(); }
    @Override public Multiset<Key> keys() { return inner.keys(); }
    @Override public Collection<VOuter> values() { return toOuterCollection(inner.values()); }
    @Override public Collection<Map.Entry<Key, VOuter>> entries() { return ConvertedMap.convertedEntrySet(inner.entries(), (key, value) -> toInner(value), (key, value) -> toOuter(value)); }
    @Override public Map<Key, Collection<VOuter>> asMap() { return new ConvertedMap<Key, Collection<VInner>, Collection<VOuter>>(inner.asMap()) {
        @Override protected Collection<VInner> toInner(Collection<VOuter> outer) { return toInnerCollection(outer); }
        @Override protected Collection<VOuter> toOuter(Collection<VInner> value) { return toOuterCollection(value); }
    }; }
    @Override public String toString() { return entries().toString(); }
}
