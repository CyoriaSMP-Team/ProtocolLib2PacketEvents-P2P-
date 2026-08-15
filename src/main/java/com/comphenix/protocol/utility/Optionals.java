package com.comphenix.protocol.utility;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Optional helpers retained for plugins compiled against ProtocolLib. */
public final class Optionals {
    public Optionals() {
    }

    public static <T> Optional<T> or(Optional<T> value, Supplier<Optional<T>> fallback) {
        return value != null && value.isPresent() ? value : fallback.get();
    }

    public static <T> boolean TestIfPresent(Optional<T> value, Predicate<T> predicate) {
        return value != null && value.isPresent() && predicate.test(value.get());
    }

    public static <T> boolean Equals(Optional<T> value, Class<?> type) {
        return value != null && value.isPresent() && type != null && type.isInstance(value.get());
    }
}
