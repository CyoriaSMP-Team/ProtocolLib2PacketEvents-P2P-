package com.comphenix.protocol.injector.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.wrappers.WrappedStreamCodec;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Packet registry backed by PacketEvents' version-aware wrapper registry. */
public class PacketRegistry {
    private static volatile boolean initialized;
    protected static volatile Register REGISTER;
    private static final Object LOCK = new Object();
    public static class Register {
        private final Map<PacketType,Optional<Class<?>>> typeToClass=new HashMap<>();
        private final Map<Class<?>,PacketType> classToType=new HashMap<>();
        private final Map<Class<?>,WrappedStreamCodec> classToCodec=new HashMap<>();
        private final Set<PacketType> serverPackets=new HashSet<>();
        private final Set<PacketType> clientPackets=new HashSet<>();
        public void registerPacket(PacketType type,Class<?> packetClass,Sender sender,WrappedStreamCodec codec){typeToClass.put(type,Optional.ofNullable(packetClass));if(packetClass!=null)classToType.put(packetClass,type);if(codec!=null&&packetClass!=null)classToCodec.put(packetClass,codec);(sender==Sender.SERVER?serverPackets:clientPackets).add(type);}
        private final java.util.List<MapContainer> containers = new java.util.concurrent.CopyOnWriteArrayList<>();
        public void addContainer(MapContainer container) {
            if (container != null) containers.add(container);
        }
        public boolean isOutdated(){
            for (MapContainer container : containers) if (container.hasChanged()) return true;
            return false;
        }
    }
    public static synchronized void synchronize(){if(!initialized)initialize();}
    static void initialize(){synchronized(LOCK){if(initialized)return;Register register=new Register();for(PacketType type:PacketType.values()){if(type==null||!type.isSupported())continue;register.registerPacket(type,type.getPacketClass(),type.getSender(),null);}REGISTER=register;initialized=true;}}
    protected static synchronized Register createOldRegister(){synchronize();return REGISTER;}
    protected static void associatePackets(Register register,Map<Integer,Class<?>> lookup,PacketType.Protocol protocol,Sender sender){if(lookup==null)return;for(Map.Entry<Integer,Class<?>> entry:lookup.entrySet()){PacketType type=PacketType.fromKey(protocol,sender,String.valueOf(entry.getKey()));if(type!=null)register.registerPacket(type,entry.getValue(),sender,null);}}
    public static WrappedStreamCodec getStreamCodec(Class<?> packetClass){synchronize();return REGISTER.classToCodec.get(packetClass);}
    public static boolean isSupported(PacketType type){synchronize();return type!=null&&REGISTER.typeToClass.containsKey(type)&&REGISTER.typeToClass.get(type).isPresent();}
    public static Set<PacketType> getServerPacketTypes(){synchronize();return Collections.unmodifiableSet(new HashSet<>(REGISTER.serverPackets));}
    public static Set<PacketType> getClientPacketTypes(){synchronize();return Collections.unmodifiableSet(new HashSet<>(REGISTER.clientPackets));}
    @Deprecated public static Class<?> getPacketClassFromType(PacketType type,boolean forceVanilla){return tryGetPacketClass(type).orElse(null);}
    public static Optional<Class<?>> tryGetPacketClass(PacketType type){synchronize();return type==null?Optional.empty():REGISTER.typeToClass.getOrDefault(type,Optional.empty());}
    public static Class<?> getPacketClassFromType(PacketType type){return tryGetPacketClass(type).orElseThrow(()->new IllegalArgumentException("Unknown packet type: "+type));}
    @Deprecated public static PacketType getPacketType(Class<?> packet){synchronize();return REGISTER.classToType.get(packet);}
    public static PacketType getPacketType(PacketType.Protocol protocol,Class<?> packet){PacketType result=getPacketType(packet);return result!=null&&result.getProtocol()==protocol?result:null;}
    @Deprecated public static PacketType getPacketType(Class<?> packet,Sender sender){PacketType result=getPacketType(packet);return result!=null&&result.getSender()==sender?result:null;}
}
