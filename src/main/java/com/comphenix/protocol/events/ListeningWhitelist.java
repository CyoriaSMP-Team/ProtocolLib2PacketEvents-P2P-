/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.GamePhase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Immutable packet filter description with ProtocolLib's builder API. */
public class ListeningWhitelist {
    public static final ListeningWhitelist EMPTY_WHITELIST = new ListeningWhitelist(ListenerPriority.LOWEST);
    /** Alias emitted by early P2P builds. */
    public static final ListeningWhitelist EMPTY = EMPTY_WHITELIST;

    private final ListenerPriority priority;
    private final GamePhase gamePhase;
    private final Set<ListenerOptions> options;
    private final Set<PacketType> types;

    private ListeningWhitelist(ListenerPriority priority) {
        this(priority, GamePhase.PLAYING, Collections.emptySet(), Collections.emptySet());
    }

    private ListeningWhitelist(ListenerPriority priority, GamePhase gamePhase,
                               Collection<ListenerOptions> options, Collection<PacketType> types) {
        this.priority = priority == null ? ListenerPriority.NORMAL : priority;
        this.gamePhase = gamePhase == null ? GamePhase.PLAYING : gamePhase;
        this.options = Collections.unmodifiableSet(options.isEmpty()
                ? EnumSet.noneOf(ListenerOptions.class)
                : EnumSet.copyOf(options));
        this.types = Collections.unmodifiableSet(new HashSet<>(types));
    }

    /** Legacy constructor retained for code written against the first P2P release. */
    public ListeningWhitelist(ListenerPriority priority, PacketType... types) {
        this(priority, GamePhase.PLAYING, Collections.emptySet(),
                types == null ? Collections.emptySet() : Arrays.asList(types));
    }

    /** Legacy constructor retained for code written against the first P2P release. */
    public ListeningWhitelist(ListenerPriority priority, Set<PacketType> types) {
        this(priority, GamePhase.PLAYING, Collections.emptySet(),
                types == null ? Collections.emptySet() : types);
    }

    public static boolean isEmpty(ListeningWhitelist whitelist) {
        return whitelist == null || whitelist.getTypes().isEmpty();
    }

    public static Builder newBuilder() {
        return new Builder(null);
    }

    public static Builder newBuilder(ListeningWhitelist template) {
        return new Builder(template);
    }

    public boolean isEnabled() { return !types.isEmpty(); }
    public boolean isEmpty() { return types.isEmpty(); }
    public ListenerPriority getPriority() { return priority; }
    public GamePhase getGamePhase() { return gamePhase; }
    public Set<ListenerOptions> getOptions() { return options; }
    public Set<PacketType> getTypes() { return types; }

    @Override
    public int hashCode() { return Objects.hash(priority, gamePhase, options, types); }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ListeningWhitelist other)) return false;
        return priority == other.priority && gamePhase == other.gamePhase
                && options.equals(other.options) && types.equals(other.types);
    }

    @Override
    public String toString() {
        return this == EMPTY_WHITELIST ? "EMPTY_WHITELIST"
                : "ListeningWhitelist[priority=" + priority + ", packets=" + types
                + ", gamephase=" + gamePhase + ", options=" + options + "]";
    }

    public static class Builder {
        private ListenerPriority priority = ListenerPriority.NORMAL;
        private GamePhase gamePhase = GamePhase.PLAYING;
        private final Set<ListenerOptions> options = EnumSet.noneOf(ListenerOptions.class);
        private final Set<PacketType> types = new HashSet<>();

        private Builder(ListeningWhitelist template) {
            if (template != null) {
                priority = template.priority;
                gamePhase = template.gamePhase;
                options.addAll(template.options);
                types.addAll(template.types);
            }
        }

        public Builder priority(ListenerPriority priority) { this.priority = priority; return this; }
        public Builder monitor() { return priority(ListenerPriority.MONITOR); }
        public Builder highest() { return priority(ListenerPriority.HIGHEST); }
        public Builder high() { return priority(ListenerPriority.HIGH); }
        public Builder normal() { return priority(ListenerPriority.NORMAL); }
        public Builder low() { return priority(ListenerPriority.LOW); }
        public Builder lowest() { return priority(ListenerPriority.LOWEST); }

        public Builder types(PacketType... types) {
            this.types.clear();
            if (types != null) this.types.addAll(Arrays.asList(types));
            return this;
        }

        public Builder types(Collection<PacketType> types) {
            this.types.clear();
            if (types != null) this.types.addAll(types);
            return this;
        }

        @Deprecated public Builder gamePhase(GamePhase gamePhase) { this.gamePhase = gamePhase; return this; }
        @Deprecated public Builder gamePhaseBoth() { return gamePhase(GamePhase.BOTH); }

        public Builder options(ListenerOptions[] options) {
            this.options.clear();
            if (options != null) this.options.addAll(Arrays.asList(options));
            return this;
        }

        public Builder options(Collection<ListenerOptions> options) {
            this.options.clear();
            if (options != null) this.options.addAll(options);
            return this;
        }

        public Builder options(Set<ListenerOptions> options) { return options((Collection<ListenerOptions>) options); }

        public Builder mergeOptions(ListenerOptions... options) {
            if (options != null) this.options.addAll(Arrays.asList(options));
            return this;
        }

        public Builder mergeOptions(Collection<ListenerOptions> options) {
            if (options != null) this.options.addAll(options);
            return this;
        }

        public ListeningWhitelist build() {
            return new ListeningWhitelist(priority, gamePhase, options, types);
        }
    }
}
