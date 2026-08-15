package com.comphenix.protocol.reflect.instances;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

/** Ordered default-value and constructor provider. */
public class DefaultInstances implements InstanceProvider {
    public static final DefaultInstances DEFAULT = fromArray(PrimitiveGenerator.INSTANCE, CollectionGenerator.INSTANCE, MinecraftGenerator.INSTANCE);
    private ImmutableList<InstanceProvider> registered;
    private boolean nonNull;
    private int maximumRecursion = 20;
    public DefaultInstances(ImmutableList<InstanceProvider> registered) { this.registered=registered; }
    public DefaultInstances(DefaultInstances other) { this.registered=other.registered; this.nonNull=other.nonNull; this.maximumRecursion=other.maximumRecursion; }
    public DefaultInstances(InstanceProvider... providers) { this(ImmutableList.copyOf(providers)); }
    public static DefaultInstances fromArray(InstanceProvider... providers) { return new DefaultInstances(providers); }
    public static DefaultInstances fromCollection(Collection<InstanceProvider> providers) { return new DefaultInstances(ImmutableList.copyOf(providers)); }
    public ImmutableList<InstanceProvider> getRegistered() { return registered; }
    public boolean isNonNull() { return nonNull; }
    public void setNonNull(boolean value) { nonNull=value; }
    public int getMaximumRecursion() { return maximumRecursion; }
    public void setMaximumRecursion(int value) { if(value<1)throw new IllegalArgumentException("Maximum recursion must be positive"); maximumRecursion=value; }
    @Override public Object create(@Nullable Class<?> type) { return getDefault((Class) type); }
    public <T> T getDefault(Class<T> type) { return getDefault(type, registered, 0); }
    public boolean hasDefault(Class<?> type) { return getDefault((Class)type)!=null; }
    public <T> Constructor<T> getMinimumConstructor(Class<T> type) { Constructor<T> best=null; for(Constructor<?> candidate:type.getConstructors()) if(best==null||candidate.getParameterCount()<best.getParameterCount()) best=(Constructor<T>)candidate; return best; }
    public <T> T getDefault(Class<T> type, List<InstanceProvider> providers) { return getDefault(type, providers, 0); }
    private <T> T getDefault(Class<T> type, List<InstanceProvider> providers, int depth) {
        if(type==null || depth>maximumRecursion) return null;
        for(InstanceProvider provider:providers) { Object value=provider.create(type); if(value!=null) return type.cast(value); }
        Constructor<T> constructor=getMinimumConstructor(type); if(constructor==null || !java.lang.reflect.Modifier.isPublic(constructor.getModifiers())) return null;
        try { Object[] args=new Object[constructor.getParameterCount()]; Class<?>[] types=constructor.getParameterTypes(); for(int i=0;i<types.length;i++){ args[i]=getDefault(types[i],providers,depth+1); if(nonNull&&args[i]==null)return null; } return constructor.newInstance(args); } catch(ReflectiveOperationException|RuntimeException ignored){ return null; }
    }
}
