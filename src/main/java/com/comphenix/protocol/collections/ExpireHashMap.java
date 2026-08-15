package com.comphenix.protocol.collections;

import com.google.common.base.Objects;
import com.google.common.base.Ticker;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Thread-safe-enough expiring map matching the semantics of ProtocolLib's cache helper. */
public class ExpireHashMap<K, V> {
    private final Map<K, ExpireEntry> entries = new HashMap<>();
    private final PriorityQueue<ExpireEntry> expirations = new PriorityQueue<>();
    private final Ticker ticker;

    final class ExpireEntry implements Comparable<ExpireEntry> {
        public final long expireTime;
        public final K expireKey;
        public final V expireValue;
        public ExpireEntry(long expireTime, K expireKey, V expireValue) { this.expireTime = expireTime; this.expireKey = expireKey; this.expireValue = expireValue; }
        @Override public int compareTo(ExpireEntry other) { return Long.compare(expireTime, other.expireTime); }
        @Override public String toString() { return "ExpireEntry[" + expireKey + "=" + expireValue + ", " + expireTime + "]"; }
    }

    public ExpireHashMap() { this(Ticker.systemTicker()); }
    public ExpireHashMap(Ticker ticker) { this.ticker = ticker == null ? Ticker.systemTicker() : ticker; }

    public synchronized V get(K key) { evictExpired(); ExpireEntry e = entries.get(key); return e == null ? null : e.expireValue; }
    public synchronized V put(K key, V value, long delay, TimeUnit unit) {
        if (unit == null) throw new NullPointerException("expireUnit cannot be null");
        if (delay <= 0) throw new IllegalArgumentException("expireDelay must be greater than zero");
        evictExpired();
        ExpireEntry previous = entries.put(key, new ExpireEntry(ticker.read() + unit.toNanos(delay), key, value));
        expirations.add(entries.get(key));
        return previous == null ? null : previous.expireValue;
    }
    public synchronized boolean containsKey(K key) { evictExpired(); return entries.containsKey(key); }
    public synchronized boolean containsValue(V value) {
        evictExpired();
        for (ExpireEntry e : entries.values()) if (Objects.equal(e.expireValue, value)) return true;
        return false;
    }
    public synchronized V removeKey(K key) { evictExpired(); ExpireEntry e = entries.remove(key); return e == null ? null : e.expireValue; }
    public synchronized int size() { evictExpired(); return entries.size(); }
    public synchronized Set<K> keySet() { evictExpired(); return entries.keySet(); }
    public synchronized Collection<V> values() { evictExpired(); return new ValueView(); }
    public synchronized Set<Map.Entry<K, V>> entrySet() { evictExpired(); return new EntryView(); }
    public synchronized Map<K, V> asMap() { evictExpired(); return new AbstractMap<>() {
        @Override public Set<Map.Entry<K, V>> entrySet() { return ExpireHashMap.this.entrySet(); }
    }; }
    public synchronized void collect() { evictExpired(); expirations.clear(); expirations.addAll(entries.values()); }
    public synchronized void clear() { entries.clear(); expirations.clear(); }
    protected synchronized void evictExpired() {
        long now = ticker.read();
        while (!expirations.isEmpty() && expirations.peek().expireTime <= now) {
            ExpireEntry expired = expirations.poll();
            if (entries.get(expired.expireKey) == expired) entries.remove(expired.expireKey);
        }
    }

    private final class ValueView extends java.util.AbstractCollection<V> {
        @Override public java.util.Iterator<V> iterator() {
            java.util.Iterator<ExpireEntry> it = entries.values().iterator();
            return new java.util.Iterator<>() { public boolean hasNext(){return it.hasNext();} public V next(){return it.next().expireValue;} };
        }
        @Override public int size() { return entries.size(); }
    }
    private final class EntryView extends java.util.AbstractSet<Map.Entry<K,V>> {
        @Override public java.util.Iterator<Map.Entry<K,V>> iterator() {
            java.util.Iterator<ExpireEntry> it = entries.values().iterator();
            return new java.util.Iterator<>() {
                public boolean hasNext(){return it.hasNext();}
                public Map.Entry<K,V> next(){ ExpireEntry e=it.next(); return new java.util.AbstractMap.SimpleImmutableEntry<>(e.expireKey,e.expireValue); }
            };
        }
        @Override public int size(){return entries.size();}
    }
    @Override public synchronized String toString() { evictExpired(); return asMap().toString(); }
}
