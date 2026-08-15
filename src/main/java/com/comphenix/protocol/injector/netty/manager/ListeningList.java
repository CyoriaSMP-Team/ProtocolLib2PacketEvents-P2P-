package com.comphenix.protocol.injector.netty.manager;

import io.netty.channel.ChannelHandler;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.UnaryOperator;
import java.util.function.Consumer;

final class ListeningList extends AbstractList<Object> {
    private final List<Object> original;
    private final ChannelHandler handler;
    public ListeningList(List<Object> original, ChannelHandler handler) { this.original=original;this.handler=handler; }
    public List<Object> getOriginal() { return original; }
    public void unProcessAll() { }
    @Override public Object get(int index) { return original.get(index); }
    @Override public int size() { return original.size(); }
    @Override public Object set(int index,Object value) { return original.set(index,value); }
    @Override public void add(int index,Object value) { original.add(index,value); modCount++; }
    @Override public boolean add(Object value) { boolean result=original.add(value); if(result)modCount++; return result; }
    @Override public Object remove(int index) { Object value=original.remove(index); modCount++; return value; }
    @Override public boolean remove(Object value) { boolean result=original.remove(value); if(result)modCount++; return result; }
    @Override public void clear() { original.clear(); modCount++; }
    @Override public boolean removeIf(java.util.function.Predicate<? super Object> filter) { boolean result=original.removeIf(filter); if(result)modCount++; return result; }
    @Override public void forEach(Consumer<? super Object> action) { original.forEach(action); }
    @Override public Object[] toArray() { return original.toArray(); }
    @Override public <T> T[] toArray(T[] array) { return original.toArray(array); }
    @Override public boolean addAll(Collection<? extends Object> values) { boolean result=original.addAll(values); if(result)modCount++; return result; }
    @Override public boolean addAll(int index, Collection<? extends Object> values) { boolean result=original.addAll(index,values); if(result)modCount++; return result; }
    @Override public boolean contains(Object value) { return original.contains(value); }
    @Override public boolean containsAll(Collection<?> values) { return original.containsAll(values); }
    @Override public boolean isEmpty() { return original.isEmpty(); }
    @Override public boolean removeAll(Collection<?> values) { boolean result=original.removeAll(values); if(result)modCount++; return result; }
    @Override public boolean retainAll(Collection<?> values) { boolean result=original.retainAll(values); if(result)modCount++; return result; }
    @Override public int indexOf(Object value) { return original.indexOf(value); }
    @Override public int lastIndexOf(Object value) { return original.lastIndexOf(value); }
    @Override public Iterator<Object> iterator() { return original.iterator(); }
    @Override public List<Object> subList(int from, int to) { return original.subList(from,to); }
    @Override public ListIterator<Object> listIterator() { return original.listIterator(); }
    @Override public ListIterator<Object> listIterator(int index) { return original.listIterator(index); }
    @Override public void replaceAll(UnaryOperator<Object> operator) { original.replaceAll(operator); }
    @Override public void sort(Comparator<? super Object> comparator) { original.sort(comparator); }
}
