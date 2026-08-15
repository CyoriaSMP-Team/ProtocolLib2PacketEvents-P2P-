package com.comphenix.protocol.wrappers.collection;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public abstract class ConvertedList<VInner, VOuter> extends ConvertedCollection<VInner, VOuter> implements List<VOuter> {
    private final List<VInner> inner;

    public ConvertedList(List<VInner> inner) {
        super(inner);
        this.inner = inner;
    }

    @Override public void add(int index, VOuter element) { inner.add(index, toInner(element)); }
    @Override public boolean addAll(int index, Collection<? extends VOuter> values) { int current = index; boolean changed = false; for (VOuter value : values) { add(current++, value); changed = true; } return changed; }
    @Override public VOuter get(int index) { return toOuter(inner.get(index)); }
    @Override @SuppressWarnings("unchecked") public int indexOf(Object value) { return inner.indexOf(toInner((VOuter) value)); }
    @Override @SuppressWarnings("unchecked") public int lastIndexOf(Object value) { return inner.lastIndexOf(toInner((VOuter) value)); }
    @Override public ListIterator<VOuter> listIterator() { return listIterator(0); }
    @Override public ListIterator<VOuter> listIterator(int index) { final ListIterator<VInner> delegate = inner.listIterator(index); return new ListIterator<>() {
        @Override public void add(VOuter e) { delegate.add(toInner(e)); }
        @Override public boolean hasNext() { return delegate.hasNext(); }
        @Override public boolean hasPrevious() { return delegate.hasPrevious(); }
        @Override public VOuter next() { return toOuter(delegate.next()); }
        @Override public int nextIndex() { return delegate.nextIndex(); }
        @Override public VOuter previous() { return toOuter(delegate.previous()); }
        @Override public int previousIndex() { return delegate.previousIndex(); }
        @Override public void remove() { delegate.remove(); }
        @Override public void set(VOuter e) { delegate.set(toInner(e)); }
    }; }
    @Override public VOuter remove(int index) { return toOuter(inner.remove(index)); }
    @Override public VOuter set(int index, VOuter element) { return toOuter(inner.set(index, toInner(element))); }
    @Override public List<VOuter> subList(int fromIndex, int toIndex) { List<VInner> sub = inner.subList(fromIndex, toIndex); return new ConvertedList<>(sub) {
        @Override protected VInner toInner(VOuter outer) { return ConvertedList.this.toInner(outer); }
        @Override protected VOuter toOuter(VInner innerValue) { return ConvertedList.this.toOuter(innerValue); }
    }; }
}
