package com.comphenix.protocol.collections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** A compact non-negative integer keyed map used by the legacy packet lookup API. */
public class IntegerMap<T> {
    private Object[] values;
    private int size;

    public IntegerMap() { this(8); }

    public IntegerMap(int initialCapacity) {
        if (initialCapacity < 0) throw new IllegalArgumentException("initialCapacity cannot be negative");
        values = new Object[Math.max(1, initialCapacity)];
    }

    public T put(int key, T value) {
        if (key < 0) throw new IllegalArgumentException("Negative key values are not permitted.");
        if (value == null) throw new NullPointerException("value cannot be null");
        ensureCapacity(key);
        @SuppressWarnings("unchecked") T old = (T) values[key];
        values[key] = value;
        if (old == null) size++;
        return old;
    }

    public T remove(int key) {
        if (key < 0 || key >= values.length) return null;
        @SuppressWarnings("unchecked") T old = (T) values[key];
        values[key] = null;
        if (old != null) size--;
        return old;
    }

    protected void ensureCapacity(int key) {
        if (key < values.length) return;
        int next = values.length;
        while (next <= key && next < (1 << 30)) next <<= 1;
        if (next <= key) next = key + 1;
        values = Arrays.copyOf(values, next);
    }

    public int size() { return size; }

    public T get(int key) {
        if (key < 0 || key >= values.length) return null;
        @SuppressWarnings("unchecked") T result = (T) values[key];
        return result;
    }

    public boolean containsKey(int key) { return get(key) != null; }

    public Map<Integer, Object> toMap() {
        Map<Integer, Object> result = new HashMap<>();
        for (int i = 0; i < values.length; i++) if (values[i] != null) result.put(i, values[i]);
        return result;
    }
}
