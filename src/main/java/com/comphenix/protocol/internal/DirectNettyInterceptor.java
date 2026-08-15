package com.comphenix.protocol.internal;

import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Optional direct-pipeline hook used only for capabilities PacketEvents cannot
 * model (raw buffers and hold/re-inject).  It records installed channels so a
 * packet is never dispatched through both the direct hook and the PE listener.
 */
public final class DirectNettyInterceptor implements AutoCloseable {
    public static final String HANDLER_PREFIX = "p2p-direct-";
    private final Set<Object> installed = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Object, String> handlerNames = new IdentityHashMap<>();
    private final String name;

    public DirectNettyInterceptor() {
        this("default");
    }

    public DirectNettyInterceptor(String name) {
        this.name = HANDLER_PREFIX + (name == null ? "default" : name);
    }

    public synchronized boolean install(Object channel, Object handler) {
        if (channel == null || handler == null || installed.contains(channel)) return false;
        Object pipeline = ChannelHelper.getPipeline(channel);
        if (pipeline == null) return false;
        String anchor = findAnchor(channel);
        boolean added = invokePipeline(pipeline, anchor, handler);
        if (added) {
            installed.add(channel);
            handlerNames.put(channel, name);
        }
        return added;
    }

    public synchronized boolean uninstall(Object channel) {
        if (!installed.remove(channel)) return false;
        try {
            Object pipeline = ChannelHelper.getPipeline(channel);
            Method remove = findPipelineMethod(pipeline, "remove", 1, String.class);
            if (remove == null) {
                handlerNames.remove(channel);
                return false;
            }
            remove.invoke(pipeline, handlerNames.remove(channel));
            return true;
        } catch (ReflectiveOperationException ex) {
            handlerNames.remove(channel);
            return false;
        }
    }

    public synchronized boolean isInstalled(Object channel) { return installed.contains(channel); }

    public synchronized Set<Object> getInstalledChannels() {
        Set<Object> copy = Collections.newSetFromMap(new IdentityHashMap<>());
        copy.addAll(installed);
        return Collections.unmodifiableSet(copy);
    }

    public HeldPacket hold(Object channel, Object packet, boolean inbound) {
        if (channel == null || packet == null) throw new IllegalArgumentException("channel and packet are required");
        return new HeldPacket(channel, packet, inbound);
    }

    public void release(HeldPacket held) {
        if (held == null || held.released) return;
        held.released = true;
        if (held.inbound) ChannelHelper.fireChannelRead(held.channel, held.packet);
        else ChannelHelper.writeAndFlush(held.channel, held.packet);
    }

    public void cancel(HeldPacket held) { if (held != null) held.released = true; }

    private String findAnchor(Object channel) {
        for (String candidate : ChannelHelper.pipelineHandlerNames(channel)) {
            if (candidate.contains("decoder") || candidate.contains("encoder") || candidate.contains("packet")) return candidate;
        }
        return null;
    }

    private boolean invokePipeline(Object pipeline, String anchor, Object handler) {
        try {
            if (anchor != null) {
                Method addBefore = findPipelineMethod(pipeline, "addBefore", 3,
                        String.class, String.class, handler == null ? Object.class : handler.getClass());
                if (addBefore == null) return false;
                addBefore.invoke(pipeline, anchor, name, handler);
            } else {
                Method addLast = findPipelineMethod(pipeline, "addLast", 2,
                        String.class, handler == null ? Object.class : handler.getClass());
                if (addLast == null) return false;
                addLast.invoke(pipeline, name, handler);
            }
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    /**
     * Netty declares the handler argument as ChannelHandler, not Object. Looking up
     * Object.class therefore fails even though the supplied handler is perfectly valid.
     * Resolve by assignability so this works with both the Netty interface and a test
     * pipeline implementation.
     */
    private static Method findPipelineMethod(Object pipeline, String name, int arity,
                                             Class<?>... suppliedTypes) {
        if (pipeline == null) return null;
        for (Method method : pipeline.getClass().getMethods()) {
            Class<?>[] parameters = method.getParameterTypes();
            if (!method.getName().equals(name) || parameters.length != arity) continue;
            boolean compatible = true;
            for (int i = 0; i < parameters.length; i++) {
                Class<?> supplied = suppliedTypes[i];
                if (supplied == null || supplied == Object.class) continue;
                if (i < 2 && parameters[i] != String.class) {
                    compatible = false;
                    break;
                }
                if (i == parameters.length - 1 && !parameters[i].isAssignableFrom(supplied)) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) return method;
        }
        return null;
    }

    @Override public synchronized void close() { for (Object channel : installed.toArray()) uninstall(channel); }

    public static final class HeldPacket {
        private final Object channel;
        private final Object packet;
        private final boolean inbound;
        private volatile boolean released;
        private HeldPacket(Object channel, Object packet, boolean inbound) { this.channel=channel; this.packet=packet; this.inbound=inbound; }
        public Object getChannel(){return channel;} public Object getPacket(){return packet;} public boolean isInbound(){return inbound;} public boolean isReleased(){return released;}
    }
}
