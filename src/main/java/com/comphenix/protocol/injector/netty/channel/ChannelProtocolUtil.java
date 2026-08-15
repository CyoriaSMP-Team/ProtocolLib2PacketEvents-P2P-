package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.PacketType;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.function.BiFunction;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;

final class ChannelProtocolUtil {
    public static final BiFunction<Channel, PacketType.Sender, PacketType.Protocol> PROTOCOL_RESOLVER = ChannelProtocolUtil::resolve;
    static final class Pre1_20_2DirectResolver implements BiFunction<Channel, PacketType.Sender, Object> {
        private final AttributeKey<Object> key;
        public Pre1_20_2DirectResolver(AttributeKey<Object> key) { this.key = key; }
        public Object apply(Channel channel, PacketType.Sender sender) { return channel == null ? null : channel.attr(key).get(); }
    }
    static final class Post1_20_2WrappedResolver implements BiFunction<Channel, PacketType.Sender, Object> {
        private final AttributeKey<Object> first; private final AttributeKey<Object> second;
        public Post1_20_2WrappedResolver(AttributeKey<Object> first, AttributeKey<Object> second) { this.first=first;this.second=second; }
        public Object apply(Channel channel, PacketType.Sender sender) { Object value = channel == null ? null : channel.attr(sender == PacketType.Sender.CLIENT ? first : second).get(); return value; }
    }
    static final class Post1_20_5WrappedResolver implements BiFunction<Channel, PacketType.Sender, Object> {
        public Object apply(Channel channel, PacketType.Sender sender) {
            if (channel == null) return null;
            for (String name : channel.pipeline().names()) {
                io.netty.channel.ChannelHandler handler = channel.pipeline().get(name);
                Object protocol = findProtocolValue(handler);
                if (protocol != null) return protocol;
            }
            return null;
        }
    }

    private static PacketType.Protocol resolve(Channel channel, PacketType.Sender sender) {
        if (channel == null) return PacketType.Protocol.UNKNOWN;
        Object value = null;
        try {
            value = channel.attr(io.netty.util.AttributeKey.valueOf("protocol")).get();
        } catch (RuntimeException ignored) {
            // The attribute is absent on newer server versions.
        }
        PacketType.Protocol protocol = fromValue(value);
        if (protocol != PacketType.Protocol.UNKNOWN) return protocol;
        Object wrapped = new Post1_20_5WrappedResolver().apply(channel, sender);
        protocol = fromValue(wrapped);
        return protocol == PacketType.Protocol.UNKNOWN ? PacketType.Protocol.PLAY : protocol;
    }

    private static Object findProtocolValue(Object value) {
        if (value == null) return null;
        for (Class<?> type = value.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) continue;
                if (!field.getType().isEnum() && !field.getName().toLowerCase(Locale.ROOT).contains("protocol")) continue;
                try {
                    field.setAccessible(true);
                    Object result = field.get(value);
                    if (fromValue(result) != PacketType.Protocol.UNKNOWN) return result;
                } catch (ReflectiveOperationException | RuntimeException ignored) { }
            }
        }
        for (Method method : value.getClass().getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0) continue;
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (!name.contains("protocol")) continue;
            try {
                method.setAccessible(true);
                Object result = method.invoke(value);
                if (fromValue(result) != PacketType.Protocol.UNKNOWN) return result;
            } catch (ReflectiveOperationException | RuntimeException ignored) { }
        }
        return null;
    }

    private static PacketType.Protocol fromValue(Object value) {
        if (value instanceof PacketType.Protocol protocol) return protocol;
        if (value instanceof Enum<?> enumeration) {
            return PacketType.Protocol.fromVanilla(enumeration);
        }
        return PacketType.Protocol.UNKNOWN;
    }
}
