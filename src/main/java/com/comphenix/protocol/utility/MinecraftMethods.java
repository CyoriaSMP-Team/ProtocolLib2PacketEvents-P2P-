package com.comphenix.protocol.utility;

import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.accessors.Accessors;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.Function;

/** Optional NMS method accessors. Unsupported server layouts return null instead of guessing. */
public final class MinecraftMethods {
    private MinecraftMethods() { }
    public static Function<ByteBuf, Object> getFriendlyBufBufConstructor() {
        for (String name : new String[]{
                "net.minecraft.network.FriendlyByteBuf",
                "net.minecraft.network.PacketDataSerializer"}) {
            try {
                Class<?> type = Class.forName(name, false, MinecraftMethods.class.getClassLoader());
                var constructor = Accessors.getConstructorAccessorOrNull(type, ByteBuf.class);
                if (constructor != null) return value -> constructor.invoke(value);
            } catch (ClassNotFoundException ignored) { }
        }
        return value -> {
            throw unsupported("FriendlyByteBuf(ByteBuf)");
        };
    }

    public static MethodAccessor getPlayerConnectionSendMethod() {
        return findOrUnsupported(MinecraftReflection.getPlayerConnectionClass(),
                "player-connection send", "send", "sendPacket", "a");
    }

    public static MethodAccessor getPlayerConnectionDisconnectMethod() {
        return findOrUnsupported(MinecraftReflection.getPlayerConnectionClass(),
                "player-connection disconnect", "disconnect", "close", "a");
    }

    public static MethodAccessor getNetworkManagerSendMethod() {
        return findOrUnsupported(MinecraftReflection.getNetworkManagerClass(),
                "network-manager send", "send", "writeAndFlush", "a");
    }

    public static MethodAccessor getNetworkManagerReadPacketMethod() {
        return findOrUnsupported(MinecraftReflection.getNetworkManagerClass(),
                "network-manager read", "receive", "readPacket", "channelRead", "a");
    }

    public static MethodAccessor getNetworkManagerDisconnectMethod() {
        return findOrUnsupported(MinecraftReflection.getNetworkManagerClass(),
                "network-manager disconnect", "disconnect", "close", "stop", "a");
    }

    public static MethodAccessor getPacketReadByteBufMethod() {
        return findPacketCodecMethod("read", "packet read(ByteBuf)");
    }

    public static MethodAccessor getPacketWriteByteBufMethod() {
        return findPacketCodecMethod("write", "packet write(ByteBuf)");
    }

    private static MethodAccessor findPacketCodecMethod(String name, String capability) {
        Class<?> packet = MinecraftReflection.getPacketClass();
        if (packet != null) {
            for (Method method : packet.getMethods()) {
                if (method.getName().equalsIgnoreCase(name) && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].getName().toLowerCase(Locale.ROOT).contains("buf")) {
                    return Accessors.getMethodAccessor(method);
                }
            }
        }
        return new UnsupportedMethodAccessor(capability);
    }

    private static MethodAccessor findOrUnsupported(Class<?> type, String capability, String... names) {
        if (type != null) {
            for (Method method : type.getMethods()) {
                for (String name : names) {
                    if (method.getName().equals(name)) {
                        return Accessors.getMethodAccessor(method);
                    }
                }
            }
        }
        return new UnsupportedMethodAccessor(capability);
    }

    private static UnsupportedOperationException unsupported(String capability) {
        return new UnsupportedOperationException("Native reflection capability unavailable: " + capability);
    }

    private static final class UnsupportedMethodAccessor implements MethodAccessor {
        private final String capability;
        private UnsupportedMethodAccessor(String capability) { this.capability = capability; }
        @Override public Object invoke(Object target, Object... args) { throw unsupported(capability); }
        @Override public Method getMethod() { return null; }
    }

    static class ReadMethodException extends RuntimeException {
        public ReadMethodException() { }
    }

    static class WriteMethodException extends RuntimeException {
        public WriteMethodException() { }
    }
}
