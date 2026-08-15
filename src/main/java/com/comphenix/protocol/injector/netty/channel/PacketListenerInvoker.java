/*
 * ProtocolLib2PacketEvents - clean-room connection listener adapter.
 */
package com.comphenix.protocol.injector.netty.channel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.netty.channel.Channel;

/**
 * Invokes the packet-listener operations exposed by a server connection.
 *
 * <p>The NMS connection and packet-listener classes are not part of P2P's
 * compile-time or runtime dependency set. This adapter therefore resolves
 * the small, stable operation contract lazily: send one packet and disconnect
 * with one reason. Named methods are preferred, but obfuscated server methods
 * are also accepted when their parameter type is compatible with the value
 * being sent. When a packet listener is not installed yet, operations fall
 * back to the network manager itself.</p>
 */
public class PacketListenerInvoker {
    public static void ensureStaticInitializedWithoutError() {
        // Kept as an explicit initialization hook for legacy callers. All
        // server-class discovery is intentionally lazy and instance-scoped.
    }

    private final Object networkManager;
    private final AtomicReference<Object> packetListener = new AtomicReference<>();

    /**
     * Creates an invoker for a native network-manager instance.
     *
     * <p>The constructor is package-private in ProtocolLib and is retained
     * with the same visibility for binary compatibility.</p>
     */
    PacketListenerInvoker(Object networkManager) {
        this.networkManager = Objects.requireNonNull(networkManager, "networkManager");
    }

    public void send(Object packet) {
        Objects.requireNonNull(packet, "packet");
        Object listener = getPacketListener();
        if (listener != null && invokePacketOperation(listener, packet, "send")) {
            return;
        }
        if (invokePacketOperation(networkManager, packet, "send")) {
            return;
        }
        if (networkManager instanceof Channel channel) {
            channel.writeAndFlush(packet);
            return;
        }
        throw unsupported("send", packet.getClass());
    }

    public void disconnect(String message) {
        String reason = message == null ? "Disconnected" : message;
        Object listener = getPacketListener();
        if (listener != null && invokeDisconnect(listener, reason, "disconnect")) {
            return;
        }
        if (invokeDisconnect(networkManager, reason, "disconnect")) {
            return;
        }
        if (networkManager instanceof Channel channel) {
            channel.close();
            return;
        }
        throw unsupported("disconnect", String.class);
    }

    private Object getPacketListener() {
        Object current = findPacketListener(networkManager);
        if (current == null) {
            packetListener.set(null);
            return null;
        }
        if (packetListener.get() != current) {
            packetListener.set(current);
        }
        return current;
    }

    private static Object findPacketListener(Object owner) {
        Class<?> type = owner.getClass();
        List<Method> methods = allMethods(type);
        methods.sort(Comparator.comparingInt(PacketListenerInvoker::listenerMethodScore).reversed());
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0
                    || method.getReturnType() == void.class || method.getReturnType().isPrimitive()) {
                continue;
            }
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (!(name.contains("packetlistener") || name.contains("packet_listener")
                    || name.equals("getlistener") || name.equals("listener"))) {
                continue;
            }
            Object result = invoke(method, owner);
            if (result != null && result != owner) return result;
        }

        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) continue;
                String name = field.getName().toLowerCase(Locale.ROOT);
                if (!(name.contains("packetlistener") || name.contains("packet_listener")
                        || name.equals("listener"))) continue;
                try {
                    field.setAccessible(true);
                    Object result = field.get(owner);
                    if (result != null && result != owner) return result;
                } catch (ReflectiveOperationException | RuntimeException error) {
                    ProtocolLogger.debug("Unable to inspect packet listener field " + field, error);
                }
            }
        }
        return null;
    }

    private static int listenerMethodScore(Method method) {
        String name = method.getName().toLowerCase(Locale.ROOT);
        if (name.contains("packetlistener")) return 4;
        if (name.contains("packet_listener")) return 3;
        if (name.equals("getlistener") || name.equals("listener")) return 2;
        return 0;
    }

    private static boolean invokePacketOperation(Object owner, Object packet, String operation) {
        Method method = selectPacketMethod(owner.getClass(), packet.getClass(), operation);
        if (method == null) return false;
        invoke(method, owner, packet);
        return true;
    }

    private static Method selectPacketMethod(Class<?> owner, Class<?> packetType, String operation) {
        List<Method> candidates = new ArrayList<>();
        for (Method method : allMethods(owner)) {
            if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 1
                    || method.getReturnType() != void.class) continue;
            Class<?> parameter = method.getParameterTypes()[0];
            if (!parameter.isAssignableFrom(packetType)) continue;
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (name.equals(operation) || name.contains(operation) || name.equals("a")
                    || parameter == packetType || parameter == Object.class) {
                candidates.add(method);
            }
        }
        return candidates.stream()
                .sorted(Comparator.comparingInt((Method method) -> packetMethodScore(method, packetType, operation)).reversed()
                        .thenComparing(Method::getName))
                .findFirst().orElse(null);
    }

    private static int packetMethodScore(Method method, Class<?> packetType, String operation) {
        String name = method.getName().toLowerCase(Locale.ROOT);
        int score = name.equals(operation) ? 100 : name.contains(operation) ? 50 : 0;
        if (name.equals("a")) score -= 1;
        Class<?> parameter = method.getParameterTypes()[0];
        if (parameter == packetType) score += 20;
        if (parameter == Object.class) score -= 10;
        return score;
    }

    private static boolean invokeDisconnect(Object owner, String reason, String operation) {
        List<Method> methods = new ArrayList<>(allMethods(owner.getClass()).stream()
                .filter(method -> !Modifier.isStatic(method.getModifiers())
                        && method.getParameterCount() == 1
                        && method.getReturnType() == void.class)
                .toList());
        methods.sort(Comparator.comparingInt((Method method) -> disconnectMethodScore(method, operation)).reversed());
        for (Method method : methods) {
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (!(name.equals(operation) || name.contains(operation) || name.startsWith("a"))) continue;
            Object argument = adaptReason(method.getParameterTypes()[0], reason);
            if (argument == UNAVAILABLE) continue;
            invoke(method, owner, argument);
            return true;
        }
        return false;
    }

    private static int disconnectMethodScore(Method method, String operation) {
        String name = method.getName().toLowerCase(Locale.ROOT);
        int score = name.equals(operation) ? 100 : name.contains(operation) ? 50 : 0;
        if (name.equals("a")) score -= 1;
        if (method.getParameterTypes()[0] == String.class) score += 20;
        return score;
    }

    private static final Object UNAVAILABLE = new Object();

    private static Object adaptReason(Class<?> parameter, String reason) {
        if (parameter == String.class || parameter == Object.class || parameter.isAssignableFrom(String.class)) {
            return reason;
        }
        try {
            Object adventure = WrappedChatComponent.fromText(reason).getComponent();
            if (parameter.isInstance(adventure)) return adventure;
        } catch (RuntimeException ignored) {
            // Adventure is optional for native-only server paths.
        }

        for (Method method : allMethods(parameter)) {
            if (!Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 1
                    || method.getParameterTypes()[0] != String.class
                    || !parameter.isAssignableFrom(method.getReturnType())) continue;
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (!(name.equals("literal") || name.equals("text") || name.equals("fromstring")
                    || name.equals("a") || name.equals("b"))) continue;
            try {
                return invoke(method, null, reason);
            } catch (RuntimeException ignored) {
                // Try the next server-version factory.
            }
        }
        try {
            Constructor<?> constructor = parameter.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            return constructor.newInstance(reason);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return UNAVAILABLE;
        }
    }

    private static List<Method> allMethods(Class<?> type) {
        List<Method> result = new ArrayList<>();
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!result.stream().anyMatch(existing -> sameSignature(existing, method))) {
                    result.add(makeAccessible(method));
                }
            }
        }
        for (Method method : type.getMethods()) {
            if (!result.stream().anyMatch(existing -> sameSignature(existing, method))) {
                result.add(makeAccessible(method));
            }
        }
        return result;
    }

    private static boolean sameSignature(Method left, Method right) {
        return left.getName().equals(right.getName())
                && java.util.Arrays.equals(left.getParameterTypes(), right.getParameterTypes());
    }

    private static Method makeAccessible(Method method) {
        try {
            method.setAccessible(true);
        } catch (RuntimeException ignored) {
            // Public methods can still be invoked when a module rejects this.
        }
        return method;
    }

    private static Object invoke(Method method, Object target, Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (IllegalAccessException error) {
            throw new IllegalStateException("Unable to invoke " + method, error);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error fatal) throw fatal;
            throw new IllegalStateException("Invocation failed for " + method, cause);
        }
    }

    private static UnsupportedOperationException unsupported(String operation, Class<?> type) {
        return new UnsupportedOperationException("Unable to " + operation
                + " on the current server connection for " + type.getName());
    }
}
