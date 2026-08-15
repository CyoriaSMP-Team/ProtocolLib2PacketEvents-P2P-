package com.comphenix.protocol.wrappers.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/** A best-effort enumeration cache around a set delegate. */
public class CachedCollection<T> implements Collection<T> {
    protected Set<T> delegate;
    protected Object[] cache;

    public CachedCollection(Set<T> delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate cannot be NULL");
        }
        this.delegate = delegate;
    }

    private void fillCache() {
        if (cache == null || cache.length != delegate.size()) {
            cache = delegate.toArray();
        }
    }

    @Override
    public int size() { return delegate.size(); }

    @Override
    public boolean isEmpty() { return delegate.isEmpty(); }

    @Override
    public boolean contains(Object o) { return delegate.contains(o); }

    @Override
    public Iterator<T> iterator() {
        fillCache();
        return new Iterator<>() {
            private int index;
            private final Iterator<T> source = delegate.iterator();

            @Override public boolean hasNext() { return index < cache.length; }

            @SuppressWarnings("unchecked")
            @Override public T next() {
                if (index >= cache.length) throw new java.util.NoSuchElementException();
                return (T) cache[index++];
            }

            @Override public void remove() {
                source.remove();
                cache = null;
            }
        };
    }

    @Override public Object[] toArray() { fillCache(); return cache.clone(); }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E[] toArray(E[] array) {
        fillCache();
        return (E[]) Arrays.copyOf(cache, size(), array.getClass());
    }

    @Override public boolean add(T e) { boolean changed = delegate.add(e); cache = null; return changed; }
    @Override public boolean addAll(Collection<? extends T> c) { boolean changed = delegate.addAll(c); cache = null; return changed; }
    @Override public boolean containsAll(Collection<?> c) { return delegate.containsAll(c); }
    @Override public boolean remove(Object o) { boolean changed = delegate.remove(o); cache = null; return changed; }
    @Override public boolean removeAll(Collection<?> c) { boolean changed = delegate.removeAll(c); cache = null; return changed; }
    @Override public boolean retainAll(Collection<?> c) { boolean changed = delegate.retainAll(c); cache = null; return changed; }
    @Override public void clear() { delegate.clear(); cache = null; }

    @Override public int hashCode() { int result = 1; for (T value : this) result = 31 * result + (value == null ? 0 : value.hashCode()); return result; }
    @Override public String toString() { return Arrays.toString(toArray()); }
}
