/* ProtocolLib2PacketEvents - clean-room statistic wrapper. */
package com.comphenix.protocol.wrappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WrappedStatistic {
    private static final List<WrappedStatistic> VALUES = new ArrayList<>();
    private final String name;
    private WrappedStatistic(String name) { this.name = name; }
    public static WrappedStatistic fromHandle(Object handle) { return handle instanceof WrappedStatistic value ? value : null; }
    public static WrappedStatistic fromName(String name) {
        if (name == null) return null;
        synchronized (VALUES) { return VALUES.stream().filter(value -> name.equals(value.name)).findFirst().orElseGet(() -> { WrappedStatistic value = new WrappedStatistic(name); VALUES.add(value); return value; }); }
    }
    public static Iterable<WrappedStatistic> values() { synchronized (VALUES) { return Collections.unmodifiableList(new ArrayList<>(VALUES)); } }
    public String getName() { return name; }
    public Object getHandle() { return this; }
    @Override public String toString() { return name; }
}
