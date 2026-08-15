package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import java.util.Arrays;

public final class CustomPacketPayloadWrapper {
    private final byte[] payload; private final MinecraftKey id;
    public CustomPacketPayloadWrapper(byte[] payload,MinecraftKey id){this.payload=payload==null?new byte[0]:payload.clone();this.id=id;}
    public static Class<?> getCustomPacketPayloadClass(){return CustomPacketPayloadWrapper.class;}
    public static EquivalentConverter<CustomPacketPayloadWrapper> getConverter(){return new EquivalentConverter<>(){public CustomPacketPayloadWrapper getSpecific(Object g){return fromUnknownPayload(g);}public Object getGeneric(CustomPacketPayloadWrapper s){return s==null?null:s.newHandle();}public Class<CustomPacketPayloadWrapper> getSpecificType(){return CustomPacketPayloadWrapper.class;}public Class<?> getGenericType(){return CustomPacketPayloadWrapper.class;}};}
    public static CustomPacketPayloadWrapper fromUnknownPayload(Object value){return value instanceof CustomPacketPayloadWrapper v?v:null;}
    public byte[] getPayload(){return payload.clone();} public MinecraftKey getId(){return id;} public Object newHandle(){return this;}
    static final class CustomPacketPayloadInterceptionHandler { public static void intercept(byte[] payload,Object handle){ } }
    @Override public boolean equals(Object o){return o instanceof CustomPacketPayloadWrapper v&&java.util.Objects.equals(id,v.id)&&Arrays.equals(payload,v.payload);} @Override public int hashCode(){return 31*java.util.Objects.hashCode(id)+Arrays.hashCode(payload);}
}
