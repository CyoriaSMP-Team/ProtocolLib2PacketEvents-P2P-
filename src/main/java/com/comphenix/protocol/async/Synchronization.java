package com.comphenix.protocol.async;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

class Synchronization {
    private Synchronization(){} public static <E> Queue<E> queue(Queue<E> queue,Object mutex){return new SynchronizedQueue<>(queue,mutex==null?queue:mutex);}
    private static class SynchronizedObject implements Serializable{final Object mutex;SynchronizedObject(Object mutex){this.mutex=mutex;}public String toString(){synchronized(mutex){return super.toString();}}}
    private static class SynchronizedCollection<E> extends SynchronizedObject implements Collection<E>{final Collection<E> delegate;SynchronizedCollection(Collection<E> d,Object m){super(m);delegate=d;}public int size(){synchronized(mutex){return delegate.size();}}public boolean isEmpty(){synchronized(mutex){return delegate.isEmpty();}}public boolean contains(Object o){synchronized(mutex){return delegate.contains(o);}}public Iterator<E> iterator(){return delegate.iterator();}public Object[] toArray(){synchronized(mutex){return delegate.toArray();}}public <T>T[] toArray(T[] a){synchronized(mutex){return delegate.toArray(a);}}public boolean add(E e){synchronized(mutex){return delegate.add(e);}}public boolean remove(Object o){synchronized(mutex){return delegate.remove(o);}}public boolean containsAll(Collection<?> c){synchronized(mutex){return delegate.containsAll(c);}}public boolean addAll(Collection<? extends E> c){synchronized(mutex){return delegate.addAll(c);}}public boolean removeAll(Collection<?> c){synchronized(mutex){return delegate.removeAll(c);}}public boolean retainAll(Collection<?> c){synchronized(mutex){return delegate.retainAll(c);}}public void clear(){synchronized(mutex){delegate.clear();}}}
    private static class SynchronizedQueue<E> extends SynchronizedCollection<E> implements Queue<E>{final Queue<E> queue;SynchronizedQueue(Queue<E> q,Object m){super(q,m);queue=q;}public boolean offer(E e){synchronized(mutex){return queue.offer(e);}}public E remove(){synchronized(mutex){return queue.remove();}}public E poll(){synchronized(mutex){return queue.poll();}}public E element(){synchronized(mutex){return queue.element();}}public E peek(){synchronized(mutex){return queue.peek();}}}
}
