package com.comphenix.protocol.injector;

import com.comphenix.protocol.events.ListenerPriority;

public class PrioritizedListener<TListener> implements Comparable<PrioritizedListener<TListener>> {
    private final TListener listener;
    private final ListenerPriority priority;
    public PrioritizedListener(TListener listener, ListenerPriority priority) { this.listener = listener; this.priority = priority; }
    @Override public int compareTo(PrioritizedListener<TListener> other) { return Integer.compare(priority.ordinal(), other.priority.ordinal()); }
    @Override public boolean equals(Object other) { return other instanceof PrioritizedListener<?> value && java.util.Objects.equals(listener, value.listener) && priority == value.priority; }
    @Override public int hashCode() { return 31 * java.util.Objects.hashCode(listener) + priority.hashCode(); }
    public TListener getListener() { return listener; }
    public ListenerPriority getPriority() { return priority; }
}
