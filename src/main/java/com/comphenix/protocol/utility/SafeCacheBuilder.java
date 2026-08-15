package com.comphenix.protocol.utility;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/** Guava cache builder that remains safe when optional cache features are unavailable. */
public class SafeCacheBuilder<K, V> {
    private final CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
    private SafeCacheBuilder() { }
    public static <K, V> SafeCacheBuilder<K, V> newBuilder() { return new SafeCacheBuilder<>(); }
    public SafeCacheBuilder<K, V> concurrencyLevel(int value) { builder.concurrencyLevel(value); return this; }
    public SafeCacheBuilder<K, V> expireAfterAccess(long value, TimeUnit unit) { builder.expireAfterAccess(value, unit); return this; }
    public SafeCacheBuilder<K, V> expireAfterWrite(long value, TimeUnit unit) { builder.expireAfterWrite(value, unit); return this; }
    public SafeCacheBuilder<K, V> initialCapacity(int value) { builder.initialCapacity(value); return this; }
    public SafeCacheBuilder<K, V> maximumSize(int value) { builder.maximumSize(value); return this; }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <K1 extends K, V1 extends V> SafeCacheBuilder<K1, V1> removalListener(RemovalListener<? super K1, ? super V1> listener) { builder.removalListener((RemovalListener) listener); return (SafeCacheBuilder) this; }
    public SafeCacheBuilder<K, V> ticker(Ticker ticker) { builder.ticker(ticker); return this; }
    public SafeCacheBuilder<K, V> softValues() { builder.softValues(); return this; }
    public SafeCacheBuilder<K, V> weakKeys() { builder.weakKeys(); return this; }
    public SafeCacheBuilder<K, V> weakValues() { builder.weakValues(); return this; }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <K1 extends K, V1 extends V> ConcurrentMap<K1, V1> build(CacheLoader<? super K1, V1> loader) {
        return (ConcurrentMap) builder.build((CacheLoader) loader).asMap();
    }
}
