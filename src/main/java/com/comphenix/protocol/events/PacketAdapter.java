/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.GamePhase;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/** ProtocolLib listener adapter with the upstream constructor/builder surface. */
public abstract class PacketAdapter implements PacketListener {
    protected Plugin plugin;
    protected ConnectionSide connectionSide;
    protected ListeningWhitelist receivingWhitelist = ListeningWhitelist.EMPTY_WHITELIST;
    protected ListeningWhitelist sendingWhitelist = ListeningWhitelist.EMPTY_WHITELIST;

    public PacketAdapter(AdapterParameteters params) {
        this(require(params).plugin, require(params).connectionSide, require(params).listenerPriority,
                require(params).gamePhase, require(params).options, require(params).packets);
    }

    public PacketAdapter(Plugin plugin, PacketType... types) {
        this(params(plugin, types));
    }

    public PacketAdapter(Plugin plugin, Iterable<? extends PacketType> types) {
        this(params(plugin, toArray(types)));
    }

    public PacketAdapter(Plugin plugin, ListenerPriority priority, Iterable<? extends PacketType> types) {
        this(params(plugin, toArray(types)).listenerPriority(priority));
    }

    public PacketAdapter(Plugin plugin, ListenerPriority priority, Iterable<? extends PacketType> types,
                         ListenerOptions... options) {
        this(params(plugin, toArray(types)).listenerPriority(priority).options(options));
    }

    public PacketAdapter(Plugin plugin, ListenerPriority priority, PacketType... types) {
        this(params(plugin, types).listenerPriority(priority));
    }

    /** Early P2P constructor retained for source compatibility. */
    public PacketAdapter(Plugin plugin, ConnectionSide side, PacketType... types) {
        this(params(plugin, types).connectionSide(side));
    }

    /** Early P2P constructor retained for source compatibility. */
    public PacketAdapter(Plugin plugin, ListenerPriority priority, ConnectionSide side, PacketType... types) {
        this(params(plugin, types).listenerPriority(priority).connectionSide(side));
    }

    private PacketAdapter(Plugin plugin, ConnectionSide side, ListenerPriority priority,
                          GamePhase phase, ListenerOptions[] options, PacketType... packets) {
        if (plugin == null || side == null || priority == null || phase == null || options == null || packets == null) {
            throw new IllegalArgumentException("PacketAdapter parameters cannot be null");
        }
        this.plugin = plugin;
        this.connectionSide = side;
        if (side.isForServer()) {
            sendingWhitelist = ListeningWhitelist.newBuilder()
                    .priority(priority).gamePhase(phase).options(options).types(packets).build();
        }
        if (side.isForClient()) {
            receivingWhitelist = ListeningWhitelist.newBuilder()
                    .priority(priority).gamePhase(phase).options(options).types(packets).build();
        }
    }

    private static AdapterParameteters require(AdapterParameteters params) {
        if (params == null) throw new IllegalArgumentException("params cannot be null");
        if (params.plugin == null) throw new IllegalStateException("Plugin was never set in parameters");
        if (params.connectionSide == null) throw new IllegalStateException("Connection side was never set");
        if (params.packets == null) throw new IllegalStateException("Packet types were never set");
        return params;
    }

    private static PacketType[] toArray(Iterable<? extends PacketType> types) {
        if (types == null) throw new IllegalArgumentException("types cannot be null");
        java.util.ArrayList<PacketType> list = new java.util.ArrayList<>();
        for (PacketType type : types) list.add(type);
        return list.toArray(new PacketType[0]);
    }

    public static String getPluginName(PacketListener listener) {
        return listener == null ? "UNKNOWN" : getPluginName(listener.getPlugin());
    }

    public static String getPluginName(Plugin plugin) {
        if (plugin == null) return "UNKNOWN";
        try { return plugin.getName(); } catch (Throwable ignored) { return plugin.toString(); }
    }

    public static AdapterParameteters params() { return new AdapterParameteters(); }

    public static AdapterParameteters params(Plugin plugin, PacketType... packets) {
        return new AdapterParameteters().plugin(plugin).types(packets);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        throw new IllegalStateException("Override onPacketReceiving to receive packet events");
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        throw new IllegalStateException("Override onPacketSending to receive packet events");
    }

    @Override public ListeningWhitelist getReceivingWhitelist() { return receivingWhitelist; }
    @Override public ListeningWhitelist getSendingWhitelist() { return sendingWhitelist; }
    @Override public Plugin getPlugin() { return plugin; }

    @Override
    public String toString() {
        return "PacketAdapter[plugin=" + getPluginName(this) + ", sending=" + sendingWhitelist
                + ", receiving=" + receivingWhitelist + "]";
    }

    public static class AdapterParameteters {
        private Plugin plugin;
        private ConnectionSide connectionSide = ConnectionSide.BOTH;
        private PacketType[] packets = new PacketType[0];
        private GamePhase gamePhase = GamePhase.PLAYING;
        private ListenerOptions[] options = new ListenerOptions[0];
        private ListenerPriority listenerPriority = ListenerPriority.NORMAL;

        public AdapterParameteters plugin(Plugin plugin) { this.plugin = plugin; return this; }
        public AdapterParameteters types(PacketType... packets) { this.packets = packets == null ? null : packets.clone(); return this; }
        public AdapterParameteters connectionSide(ConnectionSide side) { this.connectionSide = side; return this; }
        public AdapterParameteters clientSide() { this.connectionSide = ConnectionSide.add(connectionSide, ConnectionSide.CLIENT_SIDE); return this; }
        public AdapterParameteters serverSide() { this.connectionSide = ConnectionSide.add(connectionSide, ConnectionSide.SERVER_SIDE); return this; }
        public AdapterParameteters listenerPriority(ListenerPriority priority) { this.listenerPriority = priority; return this; }
        public AdapterParameteters gamePhase(GamePhase phase) { this.gamePhase = phase; return this; }
        public AdapterParameteters loginPhase() { this.gamePhase = GamePhase.LOGIN; return this; }
        public AdapterParameteters options(ListenerOptions... options) { this.options = options == null ? null : options.clone(); return this; }
        public AdapterParameteters options(java.util.Set<? extends ListenerOptions> options) {
            this.options = options == null ? null : options.toArray(new ListenerOptions[0]); return this;
        }
        public AdapterParameteters options(java.util.Collection<ListenerOptions> options) {
            this.options = options == null ? null : options.toArray(new ListenerOptions[0]); return this;
        }
        public AdapterParameteters optionAsync() { return mergeOptions(ListenerOptions.ASYNC); }
        public AdapterParameteters optionSync() { return mergeOptions(ListenerOptions.SYNC); }
        public AdapterParameteters optionManualGamePhase() { return mergeOptions(ListenerOptions.DISABLE_GAMEPHASE_DETECTION); }
        public AdapterParameteters mergeOptions(ListenerOptions... additions) {
            java.util.ArrayList<ListenerOptions> merged = new java.util.ArrayList<>();
            if (options != null) merged.addAll(Arrays.asList(options));
            if (additions != null) merged.addAll(Arrays.asList(additions));
            options = merged.toArray(new ListenerOptions[0]);
            return this;
        }
        public AdapterParameteters types(java.util.Set<PacketType> packets) {
            this.packets = packets == null ? null : packets.toArray(new PacketType[0]); return this;
        }
    }
}
