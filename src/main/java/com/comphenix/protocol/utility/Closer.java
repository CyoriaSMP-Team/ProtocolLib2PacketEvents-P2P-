package com.comphenix.protocol.utility;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Deque;

/** Closeable stack used by injection lifecycle code. */
public class Closer implements AutoCloseable {
    private final Deque<Closeable> closeables = new ArrayDeque<>();

    public static Closer create() {
        return new Closer();
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    public <C extends Closeable> C register(C closeable) {
        if (closeable != null) closeables.push(closeable);
        return closeable;
    }

    @Override
    public void close() {
        while (!closeables.isEmpty()) closeQuietly(closeables.pop());
    }
}
