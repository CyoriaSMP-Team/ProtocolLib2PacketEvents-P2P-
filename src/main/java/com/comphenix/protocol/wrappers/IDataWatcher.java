package com.comphenix.protocol.wrappers;

import org.bukkit.entity.Entity;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDataWatcher extends Iterable<WrappedWatchableObject> {
    Map<Integer,WrappedWatchableObject> asMap(); Set<Integer> getIndexes(); List<WrappedWatchableObject> getWatchableObjects(); int size(); WrappedWatchableObject getWatchableObject(int index); WrappedWatchableObject remove(int index); boolean hasIndex(int index); void clear(); Object getObject(int index); Object getObject(WrappedDataWatcher.WrappedDataWatcherObject object); void setObject(WrappedDataWatcher.WrappedDataWatcherObject object,WrappedWatchableObject value,boolean update); void setObject(WrappedDataWatcher.WrappedDataWatcherObject object,Object value,boolean update); IDataWatcher deepClone(); Object getHandle(); Entity getEntity(); void setEntity(Entity entity);
}
