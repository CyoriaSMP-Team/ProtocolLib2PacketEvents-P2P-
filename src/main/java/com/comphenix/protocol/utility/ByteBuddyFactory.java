package com.comphenix.protocol.utility;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;

/** Shared Byte Buddy entry point used for temporary players and generated accessors. */
public final class ByteBuddyFactory {
    private static final ByteBuddyFactory INSTANCE = new ByteBuddyFactory();
    private volatile ClassLoader classLoader = ByteBuddyFactory.class.getClassLoader();

    public ByteBuddyFactory() { }
    public static ByteBuddyFactory getInstance() { return INSTANCE; }
    public ClassLoader getClassLoader() { return classLoader; }
    public void setClassLoader(ClassLoader loader) { classLoader = loader == null ? ByteBuddyFactory.class.getClassLoader() : loader; }

    public <T> DynamicType.Builder.MethodDefinition.ImplementationDefinition.Optional<T> createSubclass(Class<T> type) {
        return new ByteBuddy()
                .subclass(type)
                .implement(ByteBuddyGenerated.class);
    }

    public <T> DynamicType.Builder.MethodDefinition.ImplementationDefinition.Optional<T> createSubclass(
            Class<T> type, ConstructorStrategy.Default constructorStrategy) {
        return new ByteBuddy()
                .subclass(type, constructorStrategy)
                .implement(ByteBuddyGenerated.class);
    }
}
