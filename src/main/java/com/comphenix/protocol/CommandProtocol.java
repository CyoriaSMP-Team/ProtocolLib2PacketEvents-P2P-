package com.comphenix.protocol;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.timing.TimingTrackerManager;
import com.comphenix.protocol.updater.Updater;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/** Basic administrative command surface retained for ProtocolLib integrations. */
class CommandProtocol extends CommandBase {
    public static final String NAME = "protocol";
    private final Plugin plugin;
    private final Updater updater;
    private final ProtocolConfig config;

    public CommandProtocol(ErrorReporter reporter, Plugin plugin, Updater updater, ProtocolConfig config) {
        super(reporter, PERMISSION_ADMIN, NAME);
        this.plugin = plugin;
        this.updater = updater;
        this.config = config;
    }

    @Override protected boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /protocol version|check|update|reload|timings");
            return true;
        }
        switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
            case "version" -> {
                sender.sendMessage("ProtocolLib API " + ProtocolLibrary.getVersion()
                        + " / P2P " + (plugin == null ? "unknown" : plugin.getDescription().getVersion()));
                return true;
            }
            case "check" -> { checkVersion(sender, true); return true; }
            case "update" -> { updateVersion(sender, true); return true; }
            case "reload", "config" -> { reloadConfiguration(sender); return true; }
            case "timings" -> {
                if (TimingTrackerManager.isTracking()) {
                    TimingTrackerManager.stopTracking();
                    sender.sendMessage("Protocol timing collection stopped.");
                } else {
                    TimingTrackerManager.startTracking();
                    sender.sendMessage("Protocol timing collection started.");
                }
                return true;
            }
            default -> {
                sender.sendMessage("Unknown protocol subcommand: " + args[0]);
                return true;
            }
        }
    }

    public void checkVersion(CommandSender sender, boolean command) {
        if (updater == null) { sender.sendMessage("Updater is unavailable."); return; }
        sender.sendMessage("Checking for a ProtocolLib update...");
        updater.start(Updater.UpdateType.NO_DOWNLOAD);
    }

    public void updateVersion(CommandSender sender, boolean command) {
        if (updater == null) { sender.sendMessage("Updater is unavailable."); return; }
        sender.sendMessage("Checking for an update. P2P will not replace a running jar automatically.");
        updater.start(Updater.UpdateType.DEFAULT);
    }

    public void updateFinished() { }

    public void reloadConfiguration(CommandSender sender) {
        if (config == null) { sender.sendMessage("Configuration is unavailable."); return; }
        config.reloadConfig();
        sender.sendMessage("Protocol configuration reloaded.");
    }
}
