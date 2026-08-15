package com.comphenix.protocol.injector.packet.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProtocolContexts {
    public ProtocolContexts() { }
    public static Object createGameProtocolContext() {
        Class<?> context = find("net.minecraft.network.protocol.game.GameProtocols$Context",
                "net.minecraft.network.protocol.game.GameProtocols$a");
        if (context == null || !context.isInterface()) return null;
        InvocationHandler handler = (proxy, method, args) -> defaultValue(method);
        return Proxy.newProxyInstance(ProtocolContexts.class.getClassLoader(), new Class<?>[]{context}, handler);
    }

    private static Class<?> find(String... names) {
        for (String name : names) {
            try { return Class.forName(name, false, ProtocolContexts.class.getClassLoader()); }
            catch (ClassNotFoundException ignored) { }
        }
        return null;
    }

    private static Object defaultValue(Method method) {
        if (method.getReturnType() == boolean.class) return true;
        if (method.getReturnType() == byte.class) return (byte) 0;
        if (method.getReturnType() == short.class) return (short) 0;
        if (method.getReturnType() == int.class) return 0;
        if (method.getReturnType() == long.class) return 0L;
        if (method.getReturnType() == float.class) return 0F;
        if (method.getReturnType() == double.class) return 0D;
        if (method.getReturnType() == char.class) return '\0';
        return null;
    }
}
