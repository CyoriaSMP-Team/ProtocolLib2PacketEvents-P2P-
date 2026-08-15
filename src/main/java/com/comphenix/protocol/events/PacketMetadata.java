package com.comphenix.protocol.events;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/** Side-channel metadata store for packet objects. */
class PacketMetadata {
    private static final Map<Object, Map<String, Object>> VALUES = new WeakHashMap<>();
    public static <T> Optional<T> get(Object packet, String key) { synchronized (VALUES) { Map<String,Object> values=VALUES.get(packet); return values==null?Optional.empty():Optional.ofNullable((T) values.get(key)); } }
    public static <T> void set(Object packet, String key, T value) { synchronized (VALUES) { VALUES.computeIfAbsent(packet, ignored -> new ConcurrentHashMap<>()).put(key, value); } }
    public static <T> Optional<T> remove(Object packet, String key) { synchronized (VALUES) { Map<String,Object> values=VALUES.get(packet); return values==null?Optional.empty():Optional.ofNullable((T) values.remove(key)); } }
    static final class MetaObject<T> {
        private final String key;
        private final T value;
        MetaObject(String key, T value) { this.key = key; this.value = value; }
        @Override public int hashCode() { return java.util.Objects.hash(key, value); }
        @Override public boolean equals(Object other) {
            if (!(other instanceof MetaObject<?> meta)) return false;
            return java.util.Objects.equals(key, meta.key) && java.util.Objects.equals(value, meta.value);
        }
        @Override public String toString() { return key + "=" + value; }
    }
}
