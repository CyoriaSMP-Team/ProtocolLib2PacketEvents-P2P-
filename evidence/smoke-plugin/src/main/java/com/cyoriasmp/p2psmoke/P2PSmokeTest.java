/*
 * Evidence-only smoke plugin for ProtocolLib2PacketEvents.
 *
 * It intentionally reports observations and never promotes an environment to
 * FULLY_TESTED on its own. Lifecycle, reconnect, restart, and client-driver
 * evidence are recorded by the external runner.
 */
package com.cyoriasmp.p2psmoke;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public final class P2PSmokeTest extends JavaPlugin implements CommandExecutor {
    private static final String P2P_ARTIFACT_VERSION = "1.0.2";
    private static final long MODIFIED_KEEP_ALIVE_ID = 0x5052502L;

    private final AtomicLong serverObserved = new AtomicLong();
    private final AtomicLong clientObserved = new AtomicLong();
    private final AtomicLong asyncObserved = new AtomicLong();
    private final AtomicLong asyncOffMain = new AtomicLong();
    private final AtomicLong sendRequested = new AtomicLong();
    private final AtomicLong modifyRequested = new AtomicLong();
    private final AtomicLong modifiedObserved = new AtomicLong();
    private final AtomicLong cancelRequested = new AtomicLong();
    private final AtomicLong cancelledObserved = new AtomicLong();

    private final AtomicBoolean modifyNext = new AtomicBoolean();
    private final AtomicBoolean cancelNext = new AtomicBoolean();

    private ProtocolManager protocolManager;
    private AsynchronousManager asynchronousManager;
    private String runId;
    private String startedAt;
    private volatile boolean enableClean;
    private volatile boolean disableClean;
    private volatile boolean reloadClean;
    private volatile boolean coreApi;
    private volatile String lastError;

    @Override
    public void onEnable() {
        runId = firstNonBlank(System.getenv("P2P_EVIDENCE_RUN_ID"),
                "p2psmoke-" + System.currentTimeMillis());
        startedAt = Instant.now().toString();
        enableClean = true;
        if (getCommand("p2psmoke") != null) {
            getCommand("p2psmoke").setExecutor(this);
        }

        try {
            protocolManager = ProtocolLibrary.getProtocolManager();
            asynchronousManager = ProtocolLibrary.getAsynchronousManager();
            registerListeners();
            coreApi = runCoreApiCheck();
            if (!coreApi) {
                lastError = "core API smoke check failed";
            }
        } catch (Throwable failure) {
            coreApi = false;
            lastError = failure.getClass().getName() + ": " + safeMessage(failure);
            getLogger().log(Level.SEVERE, "P2P smoke enable check failed", failure);
        }

        emit("ENABLE", coreApi ? "PASS" : "FAIL");
        writeReport();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            emit("CORE_READY", coreApi ? "PASS" : "FAIL");
            writeReport();
        }, 1L);
    }

    @Override
    public void onDisable() {
        disableClean = true;
        emit("DISABLE", lastError == null ? "PASS" : "FAIL");
        writeReport();
    }

    private void registerListeners() {
        protocolManager.addPacketListener(new PacketAdapter(
                this, ListenerPriority.NORMAL, ConnectionSide.SERVER_SIDE,
                PacketType.Play.Server.KEEP_ALIVE) {
            @Override
            public void onPacketSending(PacketEvent event) {
                serverObserved.incrementAndGet();
                if (modifyNext.compareAndSet(true, false)) {
                    try {
                        event.getPacket().getLongs().write(0, MODIFIED_KEEP_ALIVE_ID);
                        modifiedObserved.incrementAndGet();
                    } catch (Throwable failure) {
                        lastError = "modify: " + failure.getClass().getName() + ": " + safeMessage(failure);
                    }
                }
                if (cancelNext.compareAndSet(true, false)) {
                    event.setCancelled(true);
                    cancelledObserved.incrementAndGet();
                }
            }
        });

        protocolManager.addPacketListener(new PacketAdapter(
                this, ListenerPriority.NORMAL, ConnectionSide.CLIENT_SIDE,
                PacketType.Play.Client.KEEP_ALIVE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                clientObserved.incrementAndGet();
            }
        });

        protocolManager.addPacketListener(new PacketAdapter(
                PacketAdapter.params(this, PacketType.Play.Server.KEEP_ALIVE)
                        .connectionSide(ConnectionSide.SERVER_SIDE)
                        .listenerPriority(ListenerPriority.NORMAL)
                        .optionAsync()) {
            @Override
            public void onPacketSending(PacketEvent event) {
                asyncObserved.incrementAndGet();
                if (!Bukkit.isPrimaryThread()) {
                    asyncOffMain.incrementAndGet();
                }
            }
        });
    }

    private boolean runCoreApiCheck() {
        if (protocolManager == null || asynchronousManager == null) {
            return false;
        }
        if (PacketType.Play.Server.KEEP_ALIVE == null
                || PacketType.Play.Client.KEEP_ALIVE == null
                || PacketType.Login.Client.START == null
                || PacketType.Login.Server.LOGIN_SUCCESS == null) {
            return false;
        }
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.KEEP_ALIVE);
        packet.getLongs().write(0, 1L);
        return packet.getType() == PacketType.Play.Server.KEEP_ALIVE;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String action = args.length == 0 ? "status" : args[0].toLowerCase();
        switch (action) {
            case "status", "report" -> {
                writeReport();
                sender.sendMessage("P2P smoke: " + observedLevel() + " (see plugins/P2PSmokeTest/evidence.json)");
            }
            case "send", "modify", "cancel" -> runPacketCommand(sender, action, args);
            case "await" -> {
                long delay = args.length > 1 ? parseDelay(args[1]) : 10L;
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    writeReport();
                    sender.sendMessage("P2P smoke after " + delay + " ticks: " + observedLevel());
                }, delay);
            }
            case "reset" -> {
                resetCounters();
                writeReport();
                sender.sendMessage("P2P smoke counters reset.");
            }
            case "mark-reload" -> {
                reloadClean = true;
                emit("RELOAD_MARK", "PASS");
                writeReport();
                sender.sendMessage("P2P smoke reload marker recorded.");
            }
            default -> sender.sendMessage("Usage: /p2psmoke <status|send|modify|cancel|await|reset|mark-reload|report>");
        }
        return true;
    }

    private void runPacketCommand(CommandSender sender, String action, String[] args) {
        Player target = findTarget(args.length > 1 ? args[1] : null);
        if (target == null) {
            sender.sendMessage("No online target player was found.");
            return;
        }
        try {
            if ("modify".equals(action)) {
                modifyRequested.incrementAndGet();
                modifyNext.set(true);
            } else if ("cancel".equals(action)) {
                cancelRequested.incrementAndGet();
                cancelNext.set(true);
            }
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.KEEP_ALIVE);
            packet.getLongs().write(0, System.nanoTime());
            sendRequested.incrementAndGet();
            protocolManager.sendServerPacket(target, packet, true);
            sender.sendMessage("P2P smoke " + action + " requested for " + target.getName() + ".");
            emit("PACKET_" + action.toUpperCase(), "PASS");
        } catch (Throwable failure) {
            lastError = action + ": " + failure.getClass().getName() + ": " + safeMessage(failure);
            getLogger().log(Level.WARNING, "P2P smoke " + action + " failed", failure);
            sender.sendMessage("P2P smoke " + action + " failed: " + safeMessage(failure));
            emit("PACKET_" + action.toUpperCase(), "FAIL");
        }
        writeReport();
    }

    private Player findTarget(String requestedName) {
        if (requestedName != null && !requestedName.isBlank()) {
            return Bukkit.getPlayerExact(requestedName);
        }
        return Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
    }

    private void resetCounters() {
        serverObserved.set(0L);
        clientObserved.set(0L);
        asyncObserved.set(0L);
        asyncOffMain.set(0L);
        sendRequested.set(0L);
        modifyRequested.set(0L);
        modifiedObserved.set(0L);
        cancelRequested.set(0L);
        cancelledObserved.set(0L);
        modifyNext.set(false);
        cancelNext.set(false);
        lastError = null;
    }

    private String observedLevel() {
        if (coreApi && sendRequested.get() > 0 && serverObserved.get() > 0
                && clientObserved.get() > 0 && modifiedObserved.get() > 0
                && cancelledObserved.get() > 0 && asyncObserved.get() > 0
                && asyncOffMain.get() > 0) {
            return "PACKET_BEHAVIOR";
        }
        if (coreApi) return "CORE";
        if (enableClean) return "BOOT_RUNTIME";
        return "NOT_CERTIFIED";
    }

    private void emit(String event, String result) {
        getLogger().info("P2P_EVIDENCE_JSON {\"schema_version\":1,\"run_id\":\""
                + escape(runId) + "\",\"event\":\"" + escape(event)
                + "\",\"result\":\"" + escape(result) + "\",\"observed_level\":\""
                + escape(observedLevel()) + "\"}");
    }

    private void writeReport() {
        if (runId == null) return;
        try {
            Files.createDirectories(getDataFolder().toPath());
            Files.writeString(reportPath(), reportJson(), StandardCharsets.UTF_8);
        } catch (IOException failure) {
            getLogger().log(Level.WARNING, "Unable to write P2P evidence report", failure);
        }
    }

    private Path reportPath() {
        return getDataFolder().toPath().resolve("evidence.json");
    }

    private String reportJson() {
        String result = coreApi && lastError == null ? "PASS" : "FAIL";
        Plugin p2p = Bukkit.getPluginManager().getPlugin("ProtocolLib2PacketEvents");
        Plugin pe = Bukkit.getPluginManager().getPlugin("packetevents");
        String serverVersion = Bukkit.getVersion();
        return "{"
                + "\"schema_version\":1,"
                + "\"run_id\":\"" + escape(runId) + "\","
                + "\"started_at\":\"" + escape(startedAt) + "\","
                + "\"stopped_at\":" + (disableClean ? "\"" + escape(Instant.now().toString()) + "\"" : "null") + ","
                + "\"environment\":{"
                + "\"id\":\"" + escape(firstNonBlank(System.getenv("P2P_EVIDENCE_ENVIRONMENT"), "unmanaged-live-server")) + "\","
                + "\"server\":\"" + escape(serverVersion) + "\","
                + "\"server_version\":\"" + escape(Bukkit.getBukkitVersion()) + "\","
                + "\"java\":\"" + escape(System.getProperty("java.version")) + "\","
                + "\"packetevents\":\"" + escape(pluginVersion(pe)) + "\","
                + "\"p2p\":\"" + escape(P2P_ARTIFACT_VERSION) + "\"},"
                + "\"plugin\":{\"name\":\"P2PSmokeTest\",\"version\":\"1.0.0\"},"
                + "\"result\":\"" + result + "\","
                + "\"status_level\":\"NOT_CERTIFIED\","
                + "\"observed_level\":\"" + escape(observedLevel()) + "\","
                + "\"checks\":{"
                + "\"enable_clean\":" + enableClean + ","
                + "\"disable_clean\":" + disableClean + ","
                + "\"reload_clean\":" + reloadClean + ","
                + "\"no_linkage_errors\":" + (lastError == null) + ","
                + "\"core_api\":" + coreApi + ","
                + "\"send_requested\":" + (sendRequested.get() > 0) + ","
                + "\"send_observed\":" + (serverObserved.get() > 0) + ","
                + "\"receive_observed\":" + (clientObserved.get() > 0) + ","
                + "\"modify_requested\":" + (modifyRequested.get() > 0) + ","
                + "\"modify_observed\":" + (modifiedObserved.get() > 0) + ","
                + "\"cancel_requested\":" + (cancelRequested.get() > 0) + ","
                + "\"cancel_observed\":" + (cancelledObserved.get() > 0) + ","
                + "\"async_observed\":" + (asyncObserved.get() > 0) + ","
                + "\"async_off_main\":" + (asyncOffMain.get() > 0) + ","
                + "\"ordering\":false,"
                + "\"reconnect\":false,"
                + "\"restart\":false,"
                + "\"no_known_regression\":false},"
                + "\"counts\":{\"send_requested\":" + sendRequested.get()
                + ",\"send_observed\":" + serverObserved.get()
                + ",\"receive_observed\":" + clientObserved.get()
                + ",\"async_observed\":" + asyncObserved.get()
                + ",\"async_off_main\":" + asyncOffMain.get()
                + ",\"modified_observed\":" + modifiedObserved.get()
                + ",\"cancelled_observed\":" + cancelledObserved.get() + "},"
                + "\"artifact_sha256\":null,\"log_sha256\":null,"
                + "\"known_limitations\":[\"This plugin does not prove reconnect or restart.\","
                + "\"Login/configuration transitions require the external protocol matrix driver.\","
                + "\"FULLY_TESTED is never emitted by this plugin.\"]"
                + (p2p == null ? ",\"p2p_plugin_missing\":true" : "")
                + "}";
    }

    private static String pluginVersion(Plugin plugin) {
        return plugin == null ? "missing" : plugin.getDescription().getVersion();
    }

    private static long parseDelay(String value) {
        try {
            return Math.max(1L, Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            return 10L;
        }
    }

    private static String firstNonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String safeMessage(Throwable failure) {
        return failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
