package com.comphenix.protocol.utility;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class CachedPackage {
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    public static String combine(String first, String second) { return ClassSource.append(first, second); }
    public void setPackageClass(String name, Class<?> type) { if (name != null && type != null) classes.put(name, type); }
    public void removePackageClass(String name) { classes.remove(name); }
    public Optional<Class<?>> getPackageClass(String packageName, String... names) {
        String key = packageName;
        if (names != null) for (String name : names) key = combine(key, name);
        return Optional.ofNullable(classes.get(key));
    }
}
