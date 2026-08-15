package com.comphenix.protocol.injector;

import com.comphenix.protocol.internal.VersionAdapterRegistry;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

final class EntityUtilities {
    private static final EntityUtilities INSTANCE = new EntityUtilities();
    public static EntityUtilities getInstance() { return INSTANCE; }
    public void updateEntity(Entity entity, List<Player> observers) { }
    public Entity getEntity(World world, int id) {
        return world == null ? null : world.getEntities().stream().filter(entity -> entity.getEntityId() == id).findFirst().orElse(null);
    }
    public List<Player> getEntityTrackers(Entity entity) { return entity == null ? Collections.emptyList() : VersionAdapterRegistry.current().trackers(entity); }
}
