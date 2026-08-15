package com.comphenix.protocol.updater;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Spigot metadata checker with an explicit no-auto-replacement policy. */
public final class SpigotUpdater extends Updater {
    private static final ExecutorService CHECKER = Executors.newCachedThreadPool(task -> {
        Thread thread = new Thread(task, "P2P-updater");
        thread.setDaemon(true);
        return thread;
    });

    private final Plugin plugin;
    private final int resourceId;
    @SuppressWarnings("unused") private final File targetFile;
    private final boolean announce;
    private volatile String remoteVersion;

    public SpigotUpdater(Plugin plugin, UpdateType type, boolean announce) {
        this(plugin, -1, null, type, announce);
    }

    public SpigotUpdater(Plugin plugin, int resourceId, File targetFile, UpdateType type, boolean announce) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.targetFile = targetFile;
        this.announce = announce;
        if (type != null && type == UpdateType.NO_VERSION_CHECK) setResult(UpdateResult.NO_UPDATE.toString());
    }

    @Override public void start(UpdateType type) {
        UpdateType requested = type == null ? UpdateType.DEFAULT : type;
        if (requested == UpdateType.NO_VERSION_CHECK) {
            setResult(UpdateResult.NO_UPDATE.toString());
            updateFinished();
            return;
        }
        if (isChecking()) return;
        if (resourceId <= 0) {
            setResult(UpdateResult.FAIL_BADID.toString());
            updateFinished();
            return;
        }
        setChecking(true);
        CHECKER.execute(() -> {
            try {
                String remote = getSpigotVersion();
                String current = plugin == null ? null : plugin.getDescription().getVersion();
                if (remote == null || remote.isBlank()) {
                    setResult(UpdateResult.FAIL_NOVERSION.toString());
                } else if (current != null && current.trim().equalsIgnoreCase(remote.trim())) {
                    setResult(UpdateResult.NO_UPDATE.toString());
                } else {
                    String link = getLatestFileLink();
                    setResult(String.format(UpdateResult.SPIGOT_UPDATE_AVAILABLE.toString(),
                            remote, current == null ? "unknown" : current, link == null ? "" : link));
                    if (announce && plugin != null && plugin.getLogger() != null) {
                        plugin.getLogger().info(getResult());
                    }
                    // Intentionally do not download or replace a jar from a server thread.
                }
            } catch (IOException | RuntimeException error) {
                setResult(UpdateResult.FAIL_DBO.toString());
                if (plugin != null) plugin.getLogger().warning("P2P updater check failed: " + error.getMessage());
            } finally {
                setChecking(false);
                updateFinished();
            }
        });
    }

    @Override public String getResult() { return super.getResult(); }

    public String getSpigotVersion() throws IOException {
        if (resourceId <= 0) return null;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "ProtocolLib2PacketEvents updater")
                .GET().build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Spigot metadata HTTP " + response.statusCode());
            }
            remoteVersion = response.body() == null ? null : response.body().trim();
            setLatest("spigot", null, plugin == null ? null : plugin.getName(), getLatestFileLink());
            return remoteVersion;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while checking Spigot metadata", error);
        }
    }

    @Override public String getRemoteVersion() { return remoteVersion; }

    @Override public String getLatestFileLink() {
        return resourceId > 0 ? "https://www.spigotmc.org/resources/" + resourceId + "/" : super.getLatestFileLink();
    }

    static class SpigotUpdateRunnable implements Runnable {
        private final Runnable delegate;
        SpigotUpdateRunnable() { this(null); }
        SpigotUpdateRunnable(Runnable delegate) { this.delegate = delegate; }
        @Override public void run() { if (delegate != null) delegate.run(); }
    }
}
