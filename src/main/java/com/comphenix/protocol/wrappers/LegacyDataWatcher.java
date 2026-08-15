package com.comphenix.protocol.wrappers;

import org.bukkit.entity.Entity;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LegacyDataWatcher extends AbstractWrapper implements IDataWatcher {
    private final InMemoryDataWatcher delegate;
    public LegacyDataWatcher(Object handle){super(Object.class);delegate=new InMemoryDataWatcher(handle);this.handle=delegate.getHandle();}
    public LegacyDataWatcher(){this((Object)null);} public LegacyDataWatcher(Entity entity){this((Object)entity);} public LegacyDataWatcher(List<WrappedWatchableObject> values){super(Object.class);delegate=new InMemoryDataWatcher(values);handle=delegate.getHandle();}
    public Map<Integer,WrappedWatchableObject> asMap(){return delegate.asMap();} public Set<Integer> getIndexes(){return delegate.getIndexes();} public List<WrappedWatchableObject> getWatchableObjects(){return delegate.getWatchableObjects();} public java.util.Iterator<WrappedWatchableObject> iterator(){return delegate.iterator();} public int size(){return delegate.size();} public WrappedWatchableObject getWatchableObject(int i){return delegate.getWatchableObject(i);} public WrappedWatchableObject removeObject(int i){return delegate.remove(i);} public WrappedWatchableObject remove(int i){return delegate.remove(i);} public boolean hasIndex(int i){return delegate.hasIndex(i);} public Set<Integer> indexSet(){return delegate.getIndexes();} public void clear(){delegate.clear();} public Object getObject(int i){return delegate.getObject(i);} public Object getObject(WrappedDataWatcher.WrappedDataWatcherObject o){return delegate.getObject(o);} public void setObject(WrappedDataWatcher.WrappedDataWatcherObject o,WrappedWatchableObject v,boolean b){delegate.setObject(o,v,b);} public void setObject(WrappedDataWatcher.WrappedDataWatcherObject o,Object v,boolean b){delegate.setObject(o,v,b);} public IDataWatcher deepClone(){return new LegacyDataWatcher(delegate.getWatchableObjects());} public Entity getEntity(){return delegate.getEntity();} public void setEntity(Entity e){delegate.setEntity(e);}
    @Override public boolean equals(Object other){return other instanceof LegacyDataWatcher value && delegate.asMap().equals(value.delegate.asMap());}
    @Override public int hashCode(){return delegate.asMap().hashCode();}
    @Override public String toString(){return delegate.asMap().toString();}
}
