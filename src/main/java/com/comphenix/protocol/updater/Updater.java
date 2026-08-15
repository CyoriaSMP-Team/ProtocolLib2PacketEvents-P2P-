package com.comphenix.protocol.updater;

import com.comphenix.protocol.error.ReportType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Safe updater facade. It can check Spigot metadata but never replaces a live
 * plugin jar automatically; deployment remains an explicit operator action.
 */
public abstract class Updater {
    public static final ReportType REPORT_CANNOT_UPDATE_PLUGIN = new ReportType("Cannot update plugin");

    public enum UpdateType { DEFAULT, NO_VERSION_CHECK, NO_DOWNLOAD }

    public enum UpdateResult {
        SUCCESS("The updater found an update, and has readied it to be loaded the next time the server restarts/reloads."),
        NO_UPDATE("The updater did not find an update, and nothing was downloaded."),
        DISABLED("The server administrator has disabled the updating system"),
        FAIL_DOWNLOAD("The updater found an update, but was unable to download it."),
        FAIL_DBO("For some reason, the updater was unable to contact dev.bukkit.org to download the file."),
        FAIL_NOVERSION("The updater did not receive a usable remote version."),
        FAIL_BADID("The resource id provided to the updater is invalid."),
        FAIL_APIKEY("The server administrator has improperly configured their API key in the configuration"),
        UPDATE_AVAILABLE("The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it was not downloaded."),
        SPIGOT_UPDATE_AVAILABLE("The updater found an update: %s (Running %s). Download at %s");

        private final String description;
        UpdateResult(String description) { this.description = description; }
        @Override public String toString() { return description; }
    }

    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private volatile String result = UpdateResult.NO_UPDATE.toString();
    private volatile String latestType;
    private volatile String latestGameVersion;
    private volatile String latestName;
    private volatile String latestFileLink;
    private volatile boolean checking;

    public static Updater create(Plugin plugin, int id, File file, UpdateType type, boolean announce) {
        return new SpigotUpdater(plugin, id, file, type, announce);
    }

    public boolean versionCheck(String version) {
        String remote = getRemoteVersion();
        return version != null && remote != null && !version.trim().equalsIgnoreCase(remote.trim());
    }

    public void addListener(Runnable listener) { if (listener != null) listeners.add(listener); }
    public boolean removeListener(Runnable listener) { return listeners.remove(listener); }
    public String getResult() { return result; }
    public String getLatestType() { return latestType; }
    public String getLatestGameVersion() { return latestGameVersion; }
    public String getLatestName() { return latestName; }
    public String getLatestFileLink() { return latestFileLink; }
    public boolean isChecking() { return checking; }

    protected final void setChecking(boolean checking) { this.checking = checking; }
    protected final void setResult(String result) { this.result = result; }
    protected final void setLatest(String type, String gameVersion, String name, String fileLink) {
        this.latestType = type;
        this.latestGameVersion = gameVersion;
        this.latestName = name;
        this.latestFileLink = fileLink;
    }

    public void updateFinished() { for (Runnable listener : listeners) listener.run(); }
    public boolean shouldNotify() { return true; }
    public abstract void start(UpdateType type);
    public abstract String getRemoteVersion();
}
