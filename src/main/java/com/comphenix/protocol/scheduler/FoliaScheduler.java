package com.comphenix.protocol.scheduler;

import org.bukkit.plugin.Plugin;

/** Folia-compatible facade using reflective region/global schedulers when present. */
public class FoliaScheduler implements ProtocolScheduler {
    private final Plugin plugin;
    public FoliaScheduler(Plugin plugin) { this.plugin=plugin; }
    public Task scheduleSyncRepeatingTask(Runnable task,long delay,long period){return schedule("getGlobalRegionScheduler",task,delay,period);}
    public Task runTask(Runnable task){return schedule("getGlobalRegionScheduler",task,0,0);}
    public Task scheduleSyncDelayedTask(Runnable task,long delay){return schedule("getGlobalRegionScheduler",task,delay,0);}
    public Task runTaskAsync(Runnable task){return schedule("getAsyncScheduler",task,0,0);}
    private Task schedule(String accessor,Runnable task,long delay,long period){
        if(task==null)throw new IllegalArgumentException("task cannot be null");
        try {
            Object scheduler=plugin.getServer().getClass().getMethod(accessor).invoke(plugin.getServer());
            for(var m:scheduler.getClass().getMethods()) if(m.getName().equals("runAtFixedRate")&&period>0) return new FoliaTask(null,m.invoke(scheduler,plugin,task,delay,period));
            for(var m:scheduler.getClass().getMethods()) if(m.getName().equals("runDelayed")&&period==0) return new FoliaTask(null,m.invoke(scheduler,plugin,task,delay));
        } catch(ReflectiveOperationException ignored) { }
        return new DefaultScheduler(plugin).scheduleSyncDelayedTask(task,delay);
    }
}
