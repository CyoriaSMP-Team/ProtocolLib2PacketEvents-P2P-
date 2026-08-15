package com.comphenix.protocol.wrappers;

import org.bukkit.entity.Entity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InMemoryDataWatcher implements IDataWatcher {
    private final List<WrappedWatchableObject> values=new ArrayList<>(); private Entity entity;
    public InMemoryDataWatcher() { }
    public InMemoryDataWatcher(Object handle){if(handle instanceof InMemoryDataWatcher v)values.addAll(v.values);else if(handle instanceof WrappedDataWatcher v)values.addAll(v.getWatchableObjects());}
    public InMemoryDataWatcher(Entity entity){this.entity=entity;populateFromEntity(entity);}
    public InMemoryDataWatcher(List<WrappedWatchableObject> values){if(values!=null)this.values.addAll(values);}
    public static WrappedDataWatcher getEntityWatcher(Entity entity){return new WrappedDataWatcher();}
    public void populateFromEntity(Entity entity){this.entity=entity;}
    public void applyToEntity(Entity entity){this.entity=entity;}
    public Object getHandle(){return values;} public Object getHandle(Object ignored){return getHandle();}
    public IDataWatcher deepClone(){return new InMemoryDataWatcher(values);} public Entity getEntity(){return entity;} public void setEntity(Entity value){entity=value;}
    public Map<Integer,WrappedWatchableObject> asMap(){Map<Integer,WrappedWatchableObject> map=new LinkedHashMap<>();for(var v:values)map.put(v.getIndex(),v);return map;} public Set<Integer> getIndexes(){return asMap().keySet();} public List<WrappedWatchableObject> getWatchableObjects(){return new ArrayList<>(values);} public int size(){return values.size();} public WrappedWatchableObject getWatchableObject(int index){return asMap().get(index);} public WrappedWatchableObject remove(int index){WrappedWatchableObject value=getWatchableObject(index);values.removeIf(v->v.getIndex()==index);return value;} public boolean hasIndex(int index){return getWatchableObject(index)!=null;} public void clear(){values.clear();} public Object getObject(int index){WrappedWatchableObject v=getWatchableObject(index);return v==null?null:v.getValue();} public Object getObject(WrappedDataWatcher.WrappedDataWatcherObject object){return object==null?null:getObject(object.getIndex());}
    public void setObject(WrappedDataWatcher.WrappedDataWatcherObject object,WrappedWatchableObject value,boolean update){if(value==null)return;remove(object.getIndex());values.add(value);} public void setObject(WrappedDataWatcher.WrappedDataWatcherObject object,Object value,boolean update){if(object==null)throw new IllegalArgumentException("object cannot be null");remove(object.getIndex());WrappedDataWatcher watcher=new WrappedDataWatcher();watcher.setObject(object,value);values.addAll(watcher.getWatchableObjects());}
    public java.util.Iterator<WrappedWatchableObject> iterator(){return getWatchableObjects().iterator();}
}
