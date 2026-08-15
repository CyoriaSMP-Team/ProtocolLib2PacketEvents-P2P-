package com.comphenix.protocol.events;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

/** Serializable offline-player snapshot used by legacy packet serializers. */
class SerializedOfflinePlayer implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private UUID uniqueId;
    private boolean operator;
    private boolean whitelisted;
    private transient OfflinePlayer delegate;
    private boolean banned;
    private long firstPlayed;
    private long lastPlayed;
    private long lastLogin;
    private long lastSeen;
    private Location bedSpawnLocation;
    private Location respawnLocation;
    private Location location;
    private Location lastDeathLocation;
    private final Map<Object, Integer> statistics = new HashMap<>();
    private final Map<String, Object> values = new HashMap<>();
    public SerializedOfflinePlayer() { }
    public SerializedOfflinePlayer(OfflinePlayer player) {
        delegate = player;
        if (player != null) {
            name=player.getName(); uniqueId=player.getUniqueId(); operator=player.isOp(); whitelisted=player.isWhitelisted();
            firstPlayed = player.getFirstPlayed(); lastPlayed = player.getLastPlayed();
            banned = player.isBanned();
        }
    }
    public boolean isOp() { return operator; }
    public void setOp(boolean value) { operator=value; }
    public Map<String,Object> serialize() { Map<String,Object> result=new HashMap<>(values); result.put("name",name); result.put("uuid",uniqueId==null?null:uniqueId.toString()); result.put("operator",operator); result.put("whitelisted",whitelisted); return result; }
    public UUID getUniqueId() { return uniqueId; }
    public String getName() { return name; }
    public Location getBedSpawnLocation() { return location("getBedSpawnLocation", bedSpawnLocation); }
    public long getLastLogin() { return longValue("getLastLogin", lastLogin); }
    public long getLastSeen() { return longValue("getLastSeen", lastSeen); }
    public Location getRespawnLocation() { return location("getRespawnLocation", respawnLocation); }
    public Location getLocation() { return location("getLocation", location); }
    public void incrementStatistic(Statistic statistic) { incrementStatistic(statistic, 1); }
    public void decrementStatistic(Statistic statistic) { decrementStatistic(statistic, 1); }
    public void incrementStatistic(Statistic statistic, int amount) { setStatistic(statistic, getStatistic(statistic) + amount); }
    public void decrementStatistic(Statistic statistic, int amount) { setStatistic(statistic, getStatistic(statistic) - amount); }
    public void setStatistic(Statistic statistic, int amount) { statistics.put(statistic, amount); invoke("setStatistic", statistic, amount); }
    public int getStatistic(Statistic statistic) { Object result = invoke("getStatistic", statistic); return result instanceof Number ? ((Number) result).intValue() : statistics.getOrDefault(statistic, 0); }
    public void incrementStatistic(Statistic statistic, Material material) { incrementStatistic(statistic, material, 1); }
    public void decrementStatistic(Statistic statistic, Material material) { decrementStatistic(statistic, material, 1); }
    public int getStatistic(Statistic statistic, Material material) { return statisticValue(statistic, material); }
    public void incrementStatistic(Statistic statistic, Material material, int amount) { setStatistic(statistic, material, getStatistic(statistic, material) + amount); }
    public void decrementStatistic(Statistic statistic, Material material, int amount) { setStatistic(statistic, material, getStatistic(statistic, material) - amount); }
    public void setStatistic(Statistic statistic, Material material, int amount) { statistics.put(key(statistic, material), amount); invoke("setStatistic", statistic, material, amount); }
    public void incrementStatistic(Statistic statistic, EntityType type) { incrementStatistic(statistic, type, 1); }
    public void decrementStatistic(Statistic statistic, EntityType type) { decrementStatistic(statistic, type, 1); }
    public int getStatistic(Statistic statistic, EntityType type) { return statisticValue(statistic, type); }
    public void incrementStatistic(Statistic statistic, EntityType type, int amount) { setStatistic(statistic, type, getStatistic(statistic, type) + amount); }
    public void decrementStatistic(Statistic statistic, EntityType type, int amount) { setStatistic(statistic, type, getStatistic(statistic, type) - amount); }
    public void setStatistic(Statistic statistic, EntityType type, int amount) { statistics.put(key(statistic, type), amount); invoke("setStatistic", statistic, type, amount); }
    public Location getLastDeathLocation() { return location("getLastDeathLocation", lastDeathLocation); }
    public long getFirstPlayed() { return longValue("getFirstPlayed", firstPlayed); }
    public long getLastPlayed() { return longValue("getLastPlayed", lastPlayed); }
    public PlayerProfile getPlayerProfile() { return (PlayerProfile) invoke("getPlayerProfile"); }
    public boolean hasPlayedBefore() { return longValue("getFirstPlayed", firstPlayed) > 0; }
    public boolean isBanned() { Object result = invoke("isBanned"); return result instanceof Boolean ? (Boolean) result : banned; }
    @SuppressWarnings("unchecked") public org.bukkit.BanEntry<PlayerProfile> ban(String reason, Date expires, String source) { return (org.bukkit.BanEntry<PlayerProfile>) invoke("ban", reason, expires, source); }
    @SuppressWarnings("unchecked") public org.bukkit.BanEntry<PlayerProfile> ban(String reason, Instant expires, String source) { return (org.bukkit.BanEntry<PlayerProfile>) invoke("ban", reason, expires, source); }
    @SuppressWarnings("unchecked") public org.bukkit.BanEntry<PlayerProfile> ban(String reason, Duration duration, String source) { return (org.bukkit.BanEntry<PlayerProfile>) invoke("ban", reason, duration, source); }
    public void setBanned(boolean value) { banned = value; invoke("setBanned", value); }
    public boolean isOnline() { Object result = invoke("isOnline"); return result instanceof Boolean && (Boolean) result; }
    public boolean isWhitelisted() { return whitelisted; }
    public void setWhitelisted(boolean value) { whitelisted=value; }
    public Player getPlayer() { return delegate == null ? null : delegate.getPlayer(); }
    public Player getProxyPlayer() { return getPlayer(); }
    static final class PlayerUnion { }

    private Object key(Object first, Object second) { return String.valueOf(first) + ":" + second; }
    private int statisticValue(Statistic statistic, Object second) {
        Object result = invoke("getStatistic", statistic, second);
        return result instanceof Number ? ((Number) result).intValue() : statistics.getOrDefault(key(statistic, second), 0);
    }
    private Location location(String method, Location fallback) { Object result = invoke(method); return result instanceof Location ? (Location) result : fallback; }
    private long longValue(String method, long fallback) { Object result = invoke(method); return result instanceof Number ? ((Number) result).longValue() : fallback; }
    private Object invoke(String method, Object... args) {
        if (delegate == null) return null;
        for (Method candidate : delegate.getClass().getMethods()) {
            if (!candidate.getName().equals(method) || candidate.getParameterCount() != args.length) continue;
            try { return candidate.invoke(delegate, args); }
            catch (IllegalAccessException | InvocationTargetException ignored) { return null; }
        }
        return null;
    }
}
