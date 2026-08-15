package com.comphenix.protocol.async;

import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.AsynchronousManagerImpl;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.scheduler.ProtocolScheduler;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Compatibility facade over P2P's real worker-pool async manager. */
public class AsyncFilterManager extends AsynchronousManagerImpl implements com.comphenix.protocol.AsynchronousManager {
    private final ErrorReporter errorReporter;
    private final ProtocolScheduler scheduler; private final Set<PacketListener> timeoutHandlers=ConcurrentHashMap.newKeySet(); private final AtomicLong sendingIndex=new AtomicLong(); private volatile ProtocolManager manager;
    private final PlayerSendingHandler playerSendingHandler = new PlayerSendingHandler(null, null);
    private final PacketProcessingQueue processingQueue = new PacketProcessingQueue(playerSendingHandler);
    public AsyncFilterManager(ErrorReporter reporter,ProtocolScheduler scheduler){super(reporter);this.errorReporter=reporter;this.scheduler=scheduler;}
    public ProtocolManager getManager(){return manager;} public void setManager(ProtocolManager manager){this.manager=manager;} public ProtocolScheduler getScheduler(){return scheduler;}
    @Override public AsyncListenerHandler registerAsyncHandler(PacketListener listener){return super.registerAsyncHandler(listener);}
    public AsyncListenerHandler registerAsyncHandler(PacketListener listener, boolean autoStart){AsyncListenerHandler handler=super.registerAsyncHandler(listener);if(!autoStart)handler.cancel();return handler;}
    @Override public void registerTimeoutHandler(PacketListener listener){if(listener!=null)timeoutHandlers.add(listener);} @Override public void unregisterTimeoutHandler(PacketListener listener){timeoutHandlers.remove(listener);} @Override public Set<PacketListener> getTimeoutHandlers(){return Collections.unmodifiableSet(timeoutHandlers);}
    @Override public Set<PacketListener> getAsyncHandlers(){return super.getAsyncHandlers();}
    @Override public Set<PacketType> getReceivingTypes(){return super.getReceivingTypes();} @Override public Set<PacketType> getSendingTypes(){return super.getSendingTypes();} @Override public boolean hasAsynchronousListeners(PacketEvent event){return super.hasAsynchronousListeners(event);}
    public AsyncMarker createAsyncMarker(Injector injector){return createAsyncMarker(injector,AsyncMarker.DEFAULT_TIMEOUT_DELTA);} public AsyncMarker createAsyncMarker(Injector injector,long timeoutDelta){return new AsyncMarker(injector,sendingIndex.incrementAndGet(),System.currentTimeMillis(),timeoutDelta);}
    @Override public PacketStream getPacketStream(){return manager;} @Override public ErrorReporter getErrorReporter(){return errorReporter;} @Override public void cleanupAll(){super.cleanupAll();timeoutHandlers.clear();processingQueue.cleanupAll();playerSendingHandler.cleanupAll();}
    public void unregisterAsyncHandler(PacketListener listener){super.unregisterAsyncHandler(listener);} public void unregisterAsyncHandler(AsyncListenerHandler handler){super.unregisterAsyncHandler(handler);} public void unregisterAsyncHandlers(org.bukkit.plugin.Plugin plugin){super.unregisterAsyncHandlers(plugin);}
    public synchronized void enqueueSyncPacket(PacketEvent event,AsyncMarker marker){if(event==null||marker==null)throw new IllegalArgumentException("event and marker cannot be null");enqueue(PacketEvent.fromSynchronous(event,marker));}
    public void signalPacketTransmission(PacketEvent event){
        super.signalPacketTransmission(event);
        if (event != null) {
            PacketSendingQueue queue = playerSendingHandler.getSendingQueue(event, false);
            if (queue != null) queue.signalPacketUpdate(event, org.bukkit.Bukkit.isPrimaryThread());
        }
    } public PacketSendingQueue getSendingQueue(PacketEvent event){return getSendingQueue(event,true);} public PacketSendingQueue getSendingQueue(PacketEvent event,boolean create){return playerSendingHandler.getSendingQueue(event,create);} public PacketProcessingQueue getProcessingQueue(PacketEvent event){return processingQueue;} public void signalFreeProcessingSlot(PacketEvent event,boolean onMainThread){processingQueue.signalProcessingDone();} public void sendProcessedPackets(int tickCounter,boolean onMainThread){playerSendingHandler.sendAllPackets();}
    public void removePlayer(org.bukkit.entity.Player player){if(player!=null){releasePlayer(player.getUniqueId());playerSendingHandler.removePlayer(player);}}
}
