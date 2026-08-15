package com.comphenix.protocol;

import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/** Simple administrative packet listener command. */
class CommandPacket extends CommandBase {
    public static final ReportType REPORT_CANNOT_SEND_MESSAGE = new ReportType("Cannot send chat message.");
    public static final String NAME = "packet";
    public static final int PAGE_LINE_COUNT = 9;

    private enum SubCommand {
        ADD, REMOVE, NAMES, PAGE
    }

    private final Plugin plugin;
    private final ProtocolManager manager;
    private final PacketTypeSet packetTypes = new PacketTypeSet();
    private volatile PacketListener currentListener;

    public CommandPacket(ErrorReporter reporter, Plugin plugin, Logger logger,
                         CommandFilter filter, ProtocolManager manager) {
        super(reporter, PERMISSION_ADMIN, NAME);
        this.plugin = plugin;
        this.manager = manager;
    }

    public void sendMessageSilently(CommandSender receiver, String message) {
        if (receiver != null && message != null) receiver.sendMessage(message);
    }

    public void broadcastMessageSilently(String message, String permission) {
        if (plugin == null || plugin.getServer() == null) return;
        for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
            if (permission == null || player.hasPermission(permission)) player.sendMessage(message);
        }
    }

    public PacketListener createPacketListener(Set<PacketType> types) {
        return new PacketAdapter(plugin, ListenerPriority.NORMAL, types) {
            @Override public void onPacketSending(PacketEvent event) { }
            @Override public void onPacketReceiving(PacketEvent event) { }
        };
    }

    public PacketListener createCompareListener(Set<PacketType> types) {
        return createPacketListener(types);
    }

    public PacketListener updatePacketListener() {
        if (manager != null && currentListener != null) manager.removePacketListener(currentListener);
        currentListener = packetTypes.size() == 0 ? null : createPacketListener(packetTypes.values());
        if (manager != null && currentListener != null) manager.addPacketListener(currentListener);
        return currentListener;
    }

    @Override protected boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /packet add|remove|names client|server [protocol] <packet|range>");
            return true;
        }
        String action = args[0].toLowerCase(java.util.Locale.ROOT);
        if (action.equals("names")) {
            if (manager == null) return true;
            sender.sendMessage("Listening packet types: " + manager.getListeningTypes());
            return true;
        }
        if (!action.equals("add") && !action.equals("remove")) {
            sender.sendMessage("Unknown packet subcommand: " + args[0]);
            return true;
        }
        try {
            Set<PacketType> parsed = new PacketTypeParser().parseTypes(
                    new ArrayDeque<>(Arrays.asList(Arrays.copyOfRange(args, 1, args.length))),
                    PacketTypeParser.DEFAULT_MAX_RANGE);
            if (action.equals("add")) packetTypes.addAll(parsed);
            else packetTypes.removeAll(parsed);
            updatePacketListener();
            sender.sendMessage((action.equals("add") ? "Added " : "Removed ") + parsed.size() + " packet type(s).");
        } catch (RuntimeException error) {
            sender.sendMessage("Cannot parse packet types: " + error.getMessage());
        }
        return true;
    }

    public PacketTypeSet getPacketTypes() { return packetTypes; }
}
