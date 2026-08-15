package com.comphenix.protocol.injector.netty.manager;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.ListenerManager;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.channel.InjectionFactory;
import com.github.retrooper.packetevents.PacketEvents;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Installs the P2P raw interceptor on PacketEvents-owned connection channels.
 *
 * <p>PacketEvents owns channel creation. P2P observes the injector's already
 * injected connection-channel set and adds its own named handlers to each
 * child pipeline, so no server connection list is replaced and PE remains the
 * owner of its decoder/encoder lifecycle.</p>
 */
public class NetworkManagerInjector implements AutoCloseable {
    private final Plugin plugin;
    private final InjectionFactory factory;
    private final Set<Channel> observed = ConcurrentHashMap.newKeySet();
    private volatile boolean closed;
    private volatile boolean injected;
    private BukkitTask scanTask;

    public NetworkManagerInjector(Plugin plugin, ListenerManager listenerManager, ErrorReporter reporter) {
        this.plugin = plugin;
        this.factory = new InjectionFactory(plugin, reporter, listenerManager);
    }

    public Injector getInjector(Player player) { return factory.fromPlayer(player); }

    public void invalidate(Player player) {
        if (player == null) return;
        Injector injector = factory.invalidate(player);
        if (injector != null && !injector.isClosed()) injector.close();
    }

    public boolean isUsingPaperChannelInitializer() { return false; }

    /** Begins a lightweight scan of PE-owned connection channels. */
    public synchronized void inject() {
        if (closed || injected) return;
        injected = true;
        scanChannels();
        if (plugin != null && plugin.getServer() != null) {
            scanTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::scanChannels, 1L, 1L);
        }
    }

    private void scanChannels() {
        if (closed) return;
        Set<Channel> current = packetEventsChannels();
        for (Channel channel : current) {
            if (channel == null || !channel.isActive()) continue;
            try {
                Injector injector = factory.fromChannel(channel);
                injector.inject();
                observed.add(channel);
            } catch (Throwable error) {
                // A channel may be in the middle of PE's initialization. Keep it in the next scan.
                if (plugin != null) plugin.getLogger().fine("P2P channel injection deferred: " + error.getMessage());
            }
        }
        for (Channel channel : new ArrayList<>(observed)) {
            if (!current.contains(channel) || !channel.isActive()) {
                observed.remove(channel);
                factory.invalidate(channel);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Channel> packetEventsChannels() {
        if (PacketEvents.getAPI() == null) return Collections.emptySet();
        Object peInjector;
        try { peInjector = PacketEvents.getAPI().getInjector(); }
        catch (Throwable error) { return Collections.emptySet(); }
        if (peInjector == null) return Collections.emptySet();
        for (Class<?> type = peInjector.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                String name = field.getName().toLowerCase(java.util.Locale.ROOT);
                if (!Set.class.isAssignableFrom(field.getType())
                        || (!name.contains("connection") && !name.contains("channel"))) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(peInjector);
                    if (value instanceof Set<?> values) {
                        Set<Channel> channels = ConcurrentHashMap.newKeySet();
                        for (Object candidate : values) if (candidate instanceof Channel channel) channels.add(channel);
                        return channels;
                    }
                } catch (ReflectiveOperationException | RuntimeException ignored) { }
            }
        }
        return Collections.emptySet();
    }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        injected = false;
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
        factory.close();
        observed.clear();
    }
}
