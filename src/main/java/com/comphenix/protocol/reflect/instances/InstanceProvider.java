package com.comphenix.protocol.reflect.instances;

import javax.annotation.Nullable;

@FunctionalInterface
public interface InstanceProvider {
    Object create(@Nullable Class<?> type);
}
