package com.comphenix.protocol.utility;

/** Environment probes without a hard dependency on a particular server implementation. */
public final class Util {
    public Util() {
    }

    public static boolean classExists(String name) {
        try {
            Class.forName(name, false, Util.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    public static boolean isUsingSpigot() {
        return classExists("org.spigotmc.SpigotConfig") || classExists("org.bukkit.craftbukkit.CraftServer");
    }

    public static boolean isUsingFolia() {
        return classExists("io.papermc.paper.threadedregions.RegionizedServer");
    }

    public static boolean isCurrentlyReloading() {
        return false;
    }
}
