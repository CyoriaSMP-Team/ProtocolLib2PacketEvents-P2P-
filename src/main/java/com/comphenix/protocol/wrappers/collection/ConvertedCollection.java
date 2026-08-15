package com.comphenix.protocol.wrappers.collection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/** A live collection view that converts values in both directions. */
public abstract class ConvertedCollection<VInner, VOuter> extends AbstractConverted<VInner, VOuter> implements Collection<VOuter> {
    private final Collection<VInner> inner;

    public ConvertedCollection(Collection<VInner> inner) {
        if (inner == null) throw new IllegalArgumentException("Inner collection cannot be NULL");
        this.inner = inner;
    }

    @Override public boolean add(VOuter value) { return inner.add(toInner(value)); }
    @Override public boolean addAll(Collection<? extends VOuter> values) { boolean changed = false; for (VOuter value : values) changed |= add(value); return changed; }
    @Override public void clear() { inner.clear(); }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object value) { return inner.contains(toInner((VOuter) value)); }

    @Override public boolean containsAll(Collection<?> values) { for (Object value : values) if (!contains(value)) return false; return true; }
    @Override public boolean isEmpty() { return inner.isEmpty(); }
    @Override public Iterator<VOuter> iterator() { return new Iterator<>() {
        private final Iterator<VInner> delegate = inner.iterator();
        @Override public boolean hasNext() { return delegate.hasNext(); }
        @Override public VOuter next() { return toOuter(delegate.next()); }
        @Override public void remove() { delegate.remove(); }
    }; }

    @Override @SuppressWarnings("unchecked") public boolean remove(Object value) { return inner.remove(toInner((VOuter) value)); }
    @Override public boolean removeAll(Collection<?> values) { boolean changed = false; for (Object value : values) changed |= remove(value); return changed; }
    @Override @SuppressWarnings("unchecked") public boolean retainAll(Collection<?> values) {
        ArrayList<VInner> converted = new ArrayList<>();
        for (Object value : values) converted.add(toInner((VOuter) value));
        return inner.retainAll(converted);
    }
    @Override public int size() { return inner.size(); }
    @Override @SuppressWarnings("unchecked") public Object[] toArray() { Object[] result = new Object[inner.size()]; int index = 0; for (VInner value : inner) result[index++] = toOuter(value); return result; }
    @Override @SuppressWarnings("unchecked") public <T> T[] toArray(T[] array) {
        T[] result = array.length >= size() ? array : (T[]) Array.newInstance(array.getClass().getComponentType(), size());
        int index = 0; for (VInner value : inner) result[index++] = (T) toOuter(value);
        if (result.length > index) result[index] = null;
        return result;
    }
}
