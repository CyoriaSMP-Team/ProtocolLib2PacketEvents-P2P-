package com.comphenix.protocol.utility;

import java.util.Map;
import java.util.Optional;

/** Class lookup abstraction used by version adapters. */
public interface ClassSource {
    static ClassSource fromClassLoader() { return fromClassLoader(ClassSource.class.getClassLoader()); }
    static ClassSource fromPackage(String packageName) { return fromClassLoader().usingPackage(packageName); }
    static ClassSource fromClassLoader(ClassLoader loader) {
        return name -> {
            try { return Optional.of(Class.forName(name, false, loader)); }
            catch (ClassNotFoundException | LinkageError ignored) { return Optional.empty(); }
        };
    }
    static ClassSource fromMap(Map<String, Class<?>> classes) {
        return name -> Optional.ofNullable(classes.get(name));
    }
    static ClassSource empty() { return name -> Optional.empty(); }
    static String append(String prefix, String name) { return prefix == null || prefix.isEmpty() ? name : prefix + '.' + name; }
    default ClassSource retry(ClassSource other) { return name -> loadClass(name).or(() -> other.loadClass(name)); }
    default ClassSource usingPackage(String packageName) { return name -> loadClass(append(packageName, name)); }
    Optional<Class<?>> loadClass(String name);
}
