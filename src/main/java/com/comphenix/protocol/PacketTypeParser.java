package com.comphenix.protocol;

import com.comphenix.protocol.events.ConnectionSide;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

class PacketTypeParser {
    public static final Range<Integer> DEFAULT_MAX_RANGE = Range.closed(0, 255);

    private PacketType.Sender side;
    private PacketType.Protocol protocol;

    public Set<PacketType> parseTypes(Deque<String> arguments, Range<Integer> defaultRange) {
        final Set<PacketType> result = new HashSet<>();
        side = null;
        protocol = null;

        while (side == null) {
            String argument = arguments.poll();
            ConnectionSide connection = parseSide(argument);
            if (connection != null) {
                side = connection.getSender();
                continue;
            }
            if ((protocol = parseProtocol(argument)) != null) {
                continue;
            }
            throw new IllegalArgumentException("Specify connection side (CLIENT or SERVER).");
        }

        // Parse named packet types for the selected protocol/side first.
        for (Iterator<String> iterator = arguments.iterator(); iterator.hasNext();) {
            String name = iterator.next().toUpperCase(Locale.ROOT);
            Collection<PacketType> named = PacketType.fromName(name);
            boolean matched = false;
            for (PacketType type : named) {
                if (protocol == null || (type.getProtocol() == protocol && type.getSender() == side)) {
                    result.add(type);
                    matched = true;
                }
            }
            if (matched) iterator.remove();
        }

        List<Range<Integer>> ranges = RangeParser.getRanges(arguments,
                defaultRange == null ? DEFAULT_MAX_RANGE : defaultRange);
        if (ranges.isEmpty() && result.isEmpty()) {
            ranges = Collections.singletonList(defaultRange == null ? DEFAULT_MAX_RANGE : defaultRange);
        }
        for (Range<Integer> range : ranges) {
            for (Integer id : ContiguousSet.create(range, DiscreteDomain.integers())) {
                if (protocol == null) {
                    if (PacketType.hasLegacy(id)) result.add(PacketType.findLegacy(id, side));
                } else if (PacketType.hasCurrent(protocol, side, id)) {
                    result.add(PacketType.findCurrent(protocol, side, id));
                }
            }
        }
        return result;
    }

    public PacketType.Protocol getLastProtocol() { return protocol; }
    public PacketType.Sender getLastSide() { return side; }

    /** Parse a ProtocolLib connection side, preserving the upstream descriptor. */
    public ConnectionSide parseSide(String value) {
        if (value == null) return null;
        String candidate = value.toLowerCase(Locale.ROOT);
        if ("client".startsWith(candidate)) return ConnectionSide.CLIENT_SIDE;
        if ("server".startsWith(candidate)) return ConnectionSide.SERVER_SIDE;
        return null;
    }

    public PacketType.Protocol parseProtocol(String value) {
        if (value == null) return null;
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "handshake", "handshaking" -> PacketType.Protocol.HANDSHAKING;
            case "login" -> PacketType.Protocol.LOGIN;
            case "play", "game" -> PacketType.Protocol.PLAY;
            case "status" -> PacketType.Protocol.STATUS;
            case "configuration", "config" -> PacketType.Protocol.CONFIGURATION;
            default -> null;
        };
    }
}
