/*
 * ProtocolLib2PacketEvents - clean-room configuration facade.
 */
package com.comphenix.protocol;

import com.google.common.collect.ImmutableList;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Configuration surface consumed by ProtocolLib-compatible plugins.  P2P does not run an
 * updater, but it preserves the settings and persistence behavior expected by integrations.
 */
public class ProtocolConfig {
    private static final long DEFAULT_UPDATER_DELAY = 43200L;
    private final Plugin plugin;
    private Configuration config;
    private ConfigurationSection global;
    private ConfigurationSection updater;
    private long lastUpdateTime;
    private boolean configChanged;
    private boolean valuesChanged;
    private int modificationCount;

    public ProtocolConfig(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        configChanged = false;
        valuesChanged = false;
        modificationCount++;
        config = plugin.getConfig();
        global = config == null ? null : config.getConfigurationSection("global");
        updater = global == null ? null : global.getConfigurationSection("auto updater");
        lastUpdateTime = loadLastUpdate();
    }

    public File getFile() {
        return new File(plugin.getDataFolder(), "config.yml");
    }

    public boolean isDetailedErrorReporting() {
        return getGlobal("detailed error", false);
    }

    public boolean isChatWarnings() {
        return getGlobal("chat warnings", true);
    }

    public boolean isAutoNotify() {
        return getUpdater("notify", true);
    }

    public boolean isAutoDownload() {
        return getUpdater("download", false);
    }

    public boolean isDebug() {
        return getGlobal("debug", false);
    }

    public void setDebug(boolean value) {
        setGlobal("debug", value);
    }

    public ImmutableList<String> getSuppressedReports() {
        Object raw = value(global, "suppressed reports", new ArrayList<String>());
        if (raw instanceof Iterable<?> iterable) {
            ArrayList<String> result = new ArrayList<>();
            for (Object value : iterable) {
                if (value != null) {
                    result.add(String.valueOf(value));
                }
            }
            return ImmutableList.copyOf(result);
        }
        return ImmutableList.of();
    }

    public long getAutoDelay() {
        return Math.max(getUpdater("delay", 0L), DEFAULT_UPDATER_DELAY);
    }

    public String getIgnoreVersionCheck() {
        return getGlobal("ignore version check", "");
    }

    public boolean isMetricsEnabled() {
        return getGlobal("metrics", true);
    }

    public long getAutoLastTime() {
        return lastUpdateTime;
    }

    public void setAutoLastTime(long lastTimeSeconds) {
        valuesChanged = true;
        lastUpdateTime = lastTimeSeconds;
    }

    public String getScriptEngineName() {
        return getGlobal("script engine", "JavaScript");
    }

    public void setScriptEngineName(String name) {
        setGlobal("script engine", name);
    }

    public int getModificationCount() {
        return modificationCount;
    }

    public void saveAll() {
        if (valuesChanged) {
            saveLastUpdate(lastUpdateTime);
        }
        if (configChanged) {
            plugin.saveConfig();
        }
        valuesChanged = false;
        configChanged = false;
    }

    private <T> T getGlobal(String path, T fallback) {
        return value(global, path, fallback);
    }

    private <T> T getUpdater(String path, T fallback) {
        return value(updater, path, fallback);
    }

    @SuppressWarnings("unchecked")
    private static <T> T value(ConfigurationSection section, String path, T fallback) {
        if (section == null) {
            return fallback;
        }
        try {
            Object result = section.get(path, fallback);
            return result == null ? fallback : (T) result;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private void setGlobal(String path, Object value) {
        if (global == null && config != null) {
            global = config.createSection("global");
        }
        if (global != null) {
            global.set(path, value);
            configChanged = true;
            modificationCount++;
        }
    }

    private long loadLastUpdate() {
        File file = new File(plugin.getDataFolder(), "lastupdate");
        if (!file.isFile()) {
            return 0L;
        }
        try {
            return Long.parseLong(Files.readString(file.toPath(), StandardCharsets.UTF_8).trim());
        } catch (IOException | NumberFormatException ignored) {
            return 0L;
        }
    }

    private void saveLastUpdate(long value) {
        File file = new File(plugin.getDataFolder(), "lastupdate");
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IllegalStateException("Cannot create " + parent);
        }
        try {
            Files.writeString(file.toPath(), Long.toString(value), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot write " + file, ex);
        }
    }
}
