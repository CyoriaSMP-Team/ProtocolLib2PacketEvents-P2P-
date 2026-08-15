package com.comphenix.protocol.concurrent;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Small stable sorted copy-on-write set used for listener priority ordering. */
public class SortedCopyOnWriteSet<E, C extends Comparable<C>> implements Iterable<E> {
    private volatile Entry<E,C>[] entries = empty();
    @SuppressWarnings("unchecked") private static <E,C extends Comparable<C>> Entry<E,C>[] empty(){return (Entry<E,C>[])new Entry[0];}
    public synchronized boolean add(E element, C comparable) {
        Objects.requireNonNull(element, "element cannot be null");
        for (Entry<E,C> e: entries) if (e.element.equals(element)) return false;
        int at=entries.length; Entry<E,C> next=new Entry<>(element, comparable);
        for(int i=0;i<entries.length;i++) if(next.compareTo(entries[i])<0){at=i;break;}
        @SuppressWarnings("unchecked") Entry<E,C>[] copy=(Entry<E,C>[])new Entry[entries.length+1];
        System.arraycopy(entries,0,copy,0,at); copy[at]=next; System.arraycopy(entries,at,copy,at+1,entries.length-at); entries=copy; return true;
    }
    public synchronized boolean remove(E element) {
        Objects.requireNonNull(element,"element cannot be null"); int at=-1;
        for(int i=0;i<entries.length;i++) if(entries[i].element.equals(element)){at=i;break;}
        if(at<0)return false; @SuppressWarnings("unchecked") Entry<E,C>[] copy=(Entry<E,C>[])new Entry[entries.length-1];
        System.arraycopy(entries,0,copy,0,at); System.arraycopy(entries,at+1,copy,at,entries.length-at-1); entries=copy; return true;
    }
    public boolean isEmpty(){return entries.length==0;}
    @Override public Iterator<E> iterator(){ return new ElementIterator(entries); }
    static final class Entry<E,C extends Comparable<C>> implements Comparable<Entry<E,C>> {
        final E element; final C comparable;
        public Entry(E e,C c){element=e;comparable=c;}
        public E getElement(){return element;}
        public boolean is(E value){return element.equals(value);}
        public int compareTo(Entry<E,C> other){return comparable.compareTo(other.comparable);}
    }
    final class ElementIterator<E,C extends Comparable<C>> implements Iterator<E> {
        private final Entry<E,C>[] snapshot; private int index;
        public ElementIterator(Entry<E,C>[] snapshot){this.snapshot=snapshot;}
        public boolean hasNext(){return index<snapshot.length;}
        public E next(){if(!hasNext())throw new NoSuchElementException();return snapshot[index++].getElement();}
    }
}
