package com.comphenix.protocol.injector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.ObjectAllocator;

public class StructureCache {
    public StructureCache() { }
    public static Object newPacket(Class<?> type) { return ObjectAllocator.allocate(type); }
    public static boolean canCreateInstance(Class<?> type) { return type != null && ObjectAllocator.isAvailable(); }
    public static Object newInstance(Class<?> type) { return newPacket(type); }
    public static Object newPacket(PacketType type) { return type == null ? null : new com.comphenix.protocol.events.PacketContainer(type).getHandle(); }
    public static StructureModifier<Object> getStructure(Class<?> type) { return new StructureModifier<>(type, Object.class); }
    public static StructureModifier<Object> getStructure(PacketType type) { return type == null ? null : new StructureModifier<>(newPacket(type), Object.class); }
    public static Object newNullDataSerializer() { return null; }
    public static boolean tryInitTrickDataSerializer() { return false; }
}
