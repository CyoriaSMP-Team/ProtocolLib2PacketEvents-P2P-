package com.comphenix.protocol.reflect.instances;

import java.util.function.Supplier;

public final class InstanceCreator implements Supplier<Object> {
    private final Class<?> type;
    private InstanceCreator(Class<?> type) { this.type=type; }
    public static InstanceCreator forClass(Class<?> type) { if(type==null)throw new IllegalArgumentException("Type cannot be null"); return new InstanceCreator(type); }
    @Override public Object get() { return DefaultInstances.DEFAULT.getDefault(type); }
}
