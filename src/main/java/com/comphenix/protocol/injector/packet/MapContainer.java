package com.comphenix.protocol.injector.packet;

import java.lang.reflect.Field;
import java.util.Map;

/** Change detector for reflective packet registries and ordinary maps. */
public class MapContainer {
    private final Object source;
    private final Field modCount;
    private int lastModCount;
    private int lastSize;
    private boolean changed;
    public MapContainer(Object source){if(source==null)throw new NullPointerException("source cannot be null");this.source=source;this.modCount=find(source.getClass(),"modCount");this.lastModCount=modCount==null?0:readModCount();this.lastSize=size();}
    public synchronized boolean hasChanged(){checkChanged();return changed;}
    public synchronized void setChanged(boolean changed){this.changed=changed;}
    protected synchronized void checkChanged(){if(changed)return;int current=size();if((modCount!=null&&readModCount()!=lastModCount)||(modCount==null&&current!=lastSize)){lastModCount=modCount==null?lastModCount:readModCount();lastSize=current;changed=true;}}
    private int size(){return source instanceof Map<?,?>?((Map<?,?>)source).size():0;}
    private int readModCount(){try{return ((Number)modCount.get(source)).intValue();}catch(ReflectiveOperationException e){return lastModCount;}}
    private static Field find(Class<?> type,String name){for(Class<?> c=type;c!=null;c=c.getSuperclass())try{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}catch(NoSuchFieldException ignored){}return null;}
}
