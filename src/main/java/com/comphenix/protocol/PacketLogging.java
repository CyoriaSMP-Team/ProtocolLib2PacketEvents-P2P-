package com.comphenix.protocol;

import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/** Administrative packet logger with explicit add/remove lifecycle. */
public class PacketLogging implements CommandExecutor, PacketListener {
    public static final String NAME = "packetlog";
    private final Plugin plugin;
    private final ProtocolManager manager;
    private volatile ListeningWhitelist sending = ListeningWhitelist.EMPTY_WHITELIST;
    private volatile ListeningWhitelist receiving = ListeningWhitelist.EMPTY_WHITELIST;
    private volatile boolean registered;

    public PacketLogging(Plugin plugin, ProtocolManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args == null || args.length == 0) {
            sender.sendMessage("Usage: /packetlog <client|server> <packet names or ids> [console]");
            return true;
        }
        if ("off".equalsIgnoreCase(args[0]) || "remove".equalsIgnoreCase(args[0])) {
            if (registered && manager != null) manager.removePacketListener(this);
            registered = false;
            sending = receiving = ListeningWhitelist.EMPTY_WHITELIST;
            sender.sendMessage("Packet logging disabled.");
            return true;
        }
        try {
            String[] parseArgs = args;
            if (args.length > 1 && ("console".equalsIgnoreCase(args[args.length - 1])
                    || "file".equalsIgnoreCase(args[args.length - 1]))) {
                parseArgs = Arrays.copyOf(args, args.length - 1);
            }
            ArrayDeque<String> input = new ArrayDeque<>(Arrays.asList(parseArgs));
            PacketTypeParser parser = new PacketTypeParser();
            Set<PacketType> types = parser.parseTypes(input, PacketTypeParser.DEFAULT_MAX_RANGE);
            if (types.isEmpty()) throw new IllegalArgumentException("No packet type matched");
            ConnectionSide side = parser.getLastSide().toSide();
            ListeningWhitelist whitelist = ListeningWhitelist.newBuilder()
                    .priority(com.comphenix.protocol.events.ListenerPriority.MONITOR)
                    .types(types).build();
            sending = side.isForServer() ? whitelist : ListeningWhitelist.EMPTY_WHITELIST;
            receiving = side.isForClient() ? whitelist : ListeningWhitelist.EMPTY_WHITELIST;
            if (registered) manager.removePacketListener(this);
            manager.addPacketListener(this);
            registered = true;
            sender.sendMessage("Packet logging enabled for " + types.size() + " packet type(s).");
        } catch (RuntimeException error) {
            sender.sendMessage("Cannot configure packet logging: " + error.getMessage());
        }
        return true;
    }

    @Override public void onPacketSending(PacketEvent event) { log(event); }
    @Override public void onPacketReceiving(PacketEvent event) { log(event); }
    @Override public ListeningWhitelist getSendingWhitelist() { return sending; }
    @Override public ListeningWhitelist getReceivingWhitelist() { return receiving; }
    @Override public Plugin getPlugin() { return plugin; }

    private void log(PacketEvent event) {
        String payload = "";
        Object raw = event.getPacket().getRawBuffer();
        if (raw != null) {
            try { payload = " bytes=" + ByteBufHelper.copyBytes(raw).length; }
            catch (RuntimeException ignored) { payload = " bytes=?"; }
        }
        String message = "[P2P packetlog] " + (event.isServerPacket() ? "SERVER" : "CLIENT")
                + " " + event.getPacketType() + payload;
        if (plugin != null) plugin.getLogger().info(message);
    }

    private enum LogLocation { FILE, CONSOLE }
    private static class LogFormatter extends Formatter {
        @Override public String format(LogRecord record) { return record.getMessage() + System.lineSeparator(); }
    }
}
