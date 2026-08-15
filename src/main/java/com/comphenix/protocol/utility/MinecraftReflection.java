/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.utility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.packet.internal.ProtocolInfoWrapper;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedLevelChunkData;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import com.comphenix.protocol.wrappers.WrappedNumberFormat;
import com.comphenix.protocol.wrappers.WrappedParticle;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import com.comphenix.protocol.wrappers.WrappedProfilePublicKey;
import com.comphenix.protocol.wrappers.WrappedRegistrable;
import com.comphenix.protocol.wrappers.WrappedRemoteChatSessionData;
import com.comphenix.protocol.wrappers.WrappedSaltedSignature;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.comphenix.protocol.wrappers.WrappedStreamCodec;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.ping.ServerPingRecord;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;

/**
 * Feature-detection facade for the reflection API exposed by ProtocolLib.
 *
 * <p>P2P does not link against server/NMS classes.  Every native lookup is
 * therefore late-bound and every fallback is a real PacketEvents/Bukkit
 * representation.  Methods for concepts which only exist in a particular
 * server generation return an empty {@link Optional}; native-only methods
 * return the best detected class and fail explicitly if there is no useful
 * representation.</p>
 */
public final class MinecraftReflection {
    private static final String MINECRAFT_REGEX = "(?:net\\.minecraft|org\\.bukkit\\.craftbukkit)(?:\\..*)?";
    private static final Pattern NMS_PATTERN = Pattern.compile("net\\.minecraft(?:\\..*)?");

    private MinecraftReflection() { }

    public static String getMinecraftObjectRegex() { return MINECRAFT_REGEX; }
    public static AbstractFuzzyMatcher<Class<?>> getMinecraftObjectMatcher() {
        return FuzzyMatchers.matchRegex(MINECRAFT_REGEX);
    }
    public static String getMinecraftPackage() { return "net.minecraft"; }
    public static String getPackageVersion() {
        String name = getCraftBukkitPackage();
        String prefix = "org.bukkit.craftbukkit.";
        return name.startsWith(prefix) ? name.substring(prefix.length()) : "";
    }
    public static String getCraftBukkitPackage() {
        try { return Bukkit.getServer().getClass().getPackageName(); }
        catch (Throwable ignored) { return "org.bukkit.craftbukkit"; }
    }

    public static Object getBukkitEntity(Object handle) {
        if (handle instanceof Entity) return handle;
        Object value = invokeNoArg(handle, "getBukkitEntity", "getEntity");
        return value instanceof Entity ? value : null;
    }
    public static Player getBukkitPlayerFromConnection(Object connection) {
        Object value = invokeNoArg(connection, "getPlayer", "getBukkitPlayer", "player");
        return value instanceof Player ? (Player) value : null;
    }

    public static boolean isMinecraftObject(Object value) { return value != null && isMinecraftClass(value.getClass()); }
    public static boolean isMinecraftClass(Class<?> value) {
        return value != null && (NMS_PATTERN.matcher(value.getName()).matches()
                || value.getName().startsWith("org.bukkit.craftbukkit."));
    }
    public static boolean isMinecraftObject(Object value, String name) {
        return value != null && name != null && (value.getClass().getName().equals(name)
                || value.getClass().getSimpleName().equals(name));
    }
    public static boolean is(Class<?> type, Object value) { return value != null && type != null && type.isInstance(value); }
    public static boolean is(Class<?> type, Class<?> value) { return value != null && type != null && type.isAssignableFrom(value); }
    public static boolean isBlockPosition(Object value) { return value instanceof BlockPosition || simpleName(value, "BlockPos", "BlockPosition"); }
    public static boolean isChunkCoordIntPair(Object value) { return value instanceof ChunkCoordIntPair || simpleName(value, "ChunkCoordIntPair"); }
    public static boolean isPacketClass(Object value) { return value instanceof Class<?> ? isPacketClass((Class<?>) value) : value != null && isPacketClass(value.getClass()); }
    public static boolean isPacketClass(Class<?> value) {
        return value != null && (com.github.retrooper.packetevents.wrapper.PacketWrapper.class.isAssignableFrom(value)
                || value.getName().startsWith("net.minecraft.network.protocol.")
                || value.getSimpleName().endsWith("Packet"));
    }
    public static boolean isServerHandler(Object value) { return value != null && value.getClass().getName().toLowerCase().contains("network"); }
    public static boolean isMinecraftEntity(Object value) { return value instanceof Entity || simpleName(value, "Entity", "ServerPlayer", "EntityPlayer"); }
    public static boolean isItemStack(Object value) { return value instanceof ItemStack || value instanceof com.github.retrooper.packetevents.protocol.item.ItemStack || simpleName(value, "ItemStack"); }
    public static boolean isCraftPlayer(Object value) { return value != null && value.getClass().getName().startsWith("org.bukkit.craftbukkit.") && value instanceof Player; }
    public static boolean isMinecraftPlayer(Object value) { return value != null && (value instanceof Player || simpleName(value, "ServerPlayer", "EntityPlayer")); }
    public static boolean isDataWatcher(Object value) { return value instanceof WrappedDataWatcher || value instanceof List<?>; }
    public static boolean isIntHashMap(Object value) { return value instanceof Map<?, ?> || simpleName(value, "Int2ObjectMap", "IntHashMap"); }
    public static boolean isCraftItemStack(Object value) { return value instanceof ItemStack && value.getClass().getName().startsWith("org.bukkit.craftbukkit."); }
    public static boolean isIChatBaseComponent(Class<?> value) { return value != null && (ComponentClassHolder.COMPONENT.isAssignableFrom(value) || WrappedChatComponent.class.isAssignableFrom(value) || value.getSimpleName().contains("Component")); }

    public static Class<?> getEntityPlayerClass() { return nativeOr(Player.class, "net.minecraft.server.level.ServerPlayer", "net.minecraft.server.level.EntityPlayer"); }
    public static Class<?> getEntityHumanClass() { return nativeOr(Entity.class, "net.minecraft.world.entity.player.Player", "net.minecraft.world.entity.player.EntityHuman"); }
    public static Class<?> getGameProfileClass() { return nativeOr(WrappedGameProfile.ProfileHandle.class, "com.mojang.authlib.GameProfile"); }
    public static Class<?> getGameProfilePropertyMapClass() { return nativeOr(Map.class, "com.mojang.authlib.properties.PropertyMap"); }
    public static Class<?> getEntityClass() { return nativeOr(Entity.class, "net.minecraft.world.entity.Entity"); }
    public static Class<?> getCraftChatMessage() { return WrappedChatComponent.class; }
    public static Class<?> getWorldServerClass() { return nativeOr(World.class, "net.minecraft.server.level.ServerLevel", "net.minecraft.world.level.World"); }
    public static Class<?> getNmsWorldClass() { return getWorldServerClass(); }
    public static Class<?> getPacketClass() { return nativeOr(com.github.retrooper.packetevents.wrapper.PacketWrapper.class, "net.minecraft.network.protocol.Packet"); }
    public static Class<?> getByteBufClass() { return ByteBuf.class; }
    public static Class<?> getEnumProtocolClass() { return PacketType.Protocol.class; }
    public static Class<?> getIChatBaseComponentClass() { return nativeOr(ComponentClassHolder.COMPONENT, "net.minecraft.network.chat.Component", "net.minecraft.network.chat.IChatBaseComponent"); }
    public static Optional<Class<?>> getPackedBundlePacketClass() { return optionalNative("net.minecraft.network.protocol.game.ClientboundBundlePacket", "net.minecraft.network.protocol.game.PacketPlayOutBundle"); }
    public static boolean isBundlePacket(Class<?> value) { return value != null && value.getSimpleName().toLowerCase().contains("bundle"); }
    public static boolean isBundleDelimiter(Class<?> value) { return value != null && value.getSimpleName().toLowerCase().contains("bundledelimiter"); }
    public static Optional<Class<?>> getBundleDelimiterClass() { return optionalNative("net.minecraft.network.protocol.game.ClientboundBundlePacket$Delimiter", "net.minecraft.network.protocol.game.PacketPlayOutBundle$Delimiter"); }
    public static Class<?> getIChatBaseComponentArrayClass() { return Array.newInstance(getIChatBaseComponentClass(), 0).getClass(); }
    public static Class<?> getChatComponentTextClass() { return String.class; }
    public static Class<?> getChatSerializerClass() { return WrappedChatComponent.class; }
    public static Class<?> getStyleSerializerClass() { return WrappedChatComponent.class; }
    public static Class<?> getServerPingClass() { return WrappedServerPing.class; }
    public static Class<?> getServerPingServerDataClass() { return ServerPingRecord.ServerData.class; }
    public static Class<?> getServerPingPlayerSampleClass() { return ServerPingRecord.PlayerSample.class; }
    public static Class<?> getMinecraftServerClass() { return nativeOr(Object.class, "net.minecraft.server.MinecraftServer"); }
    public static Class<?> getStatisticClass() { return org.bukkit.Statistic.class; }
    public static Class<?> getStatisticListClass() { return Map.class; }
    public static Class<?> getPlayerListClass() { return nativeOr(Object.class, "net.minecraft.server.players.PlayerList", "net.minecraft.server.players.PlayerList"); }
    public static Class<?> getPlayerConnectionClass() { return nativeOr(Object.class, "net.minecraft.server.network.ServerGamePacketListenerImpl", "net.minecraft.server.network.PlayerConnection"); }
    public static Class<?> getNetworkManagerClass() { return nativeOr(ByteBuf.class, "net.minecraft.network.Connection", "net.minecraft.network.NetworkManager"); }
    public static Class<?> getItemStackClass() { return nativeOr(com.github.retrooper.packetevents.protocol.item.ItemStack.class, "net.minecraft.world.item.ItemStack"); }
    public static Class<?> getBlockClass() { return nativeOr(WrappedBlockData.class, "net.minecraft.world.level.block.Block"); }
    public static Class<?> getItemClass() { return nativeOr(Object.class, "net.minecraft.world.item.Item"); }
    public static Class<?> getFluidTypeClass() { return nativeOr(Object.class, "net.minecraft.world.level.material.Fluid"); }
    public static Class<?> getParticleTypeClass() { return nativeOr(WrappedParticle.class, "net.minecraft.core.particles.ParticleType"); }
    public static Class<?> getParticleClass() { return nativeOr(WrappedParticle.class, "net.minecraft.core.particles.ParticleOptions"); }
    public static Class<?> getWorldTypeClass() { return World.Environment.class; }
    public static Class<?> getDataWatcherClass() { return WrappedDataWatcher.class; }
    public static Class<?> getBlockPositionClass() { return BlockPosition.class; }
    public static Class<?> getVec3DClass() { return Vector3F.class; }
    public static Class<?> getChunkCoordIntPair() { return ChunkCoordIntPair.class; }
    public static Class<?> getDataWatcherItemClass() { return com.comphenix.protocol.wrappers.WrappedWatchableObject.class; }
    public static Class<?> getDataWatcherObjectClass() { return WrappedDataWatcher.WrappedDataWatcherObject.class; }
    public static boolean watcherObjectExists() { return true; }
    public static Class<?> getDataWatcherSerializerClass() { return WrappedDataWatcher.Serializer.class; }
    public static Class<?> getDataWatcherRegistryClass() { return WrappedDataWatcher.Registry.class; }
    public static Class<?> getMinecraftKeyClass() { return com.comphenix.protocol.wrappers.MinecraftKey.class; }
    public static Class<?> getMobEffectListClass() { return nativeOr(Object.class, "net.minecraft.world.effect.MobEffect"); }
    public static Class<?> getDamageTypeClass() { return nativeOr(Object.class, "net.minecraft.world.damagesource.DamageType"); }
    public static Class<?> getSoundEffectClass() { return nativeOr(Object.class, "net.minecraft.sounds.SoundEvent"); }
    public static Class<?> getServerConnectionClass() { return nativeOr(Object.class, "net.minecraft.server.network.ServerConnectionListener"); }
    public static Class<?> getNBTBaseClass() { return NbtBase.class; }
    public static Class<?> getNBTReadLimiterClass() { return nativeOr(Object.class, "net.minecraft.nbt.NbtAccounter", "net.minecraft.nbt.NBTReadLimiter"); }
    public static Class<?> getNBTCompoundClass() { return NbtCompound.class; }
    public static Class<?> getEntityTrackerClass() { return nativeOr(Object.class, "net.minecraft.server.level.ServerEntity"); }
    public static Class<?> getAttributeSnapshotClass() { return WrappedAttribute.class; }
    public static Class<?> getIntHashMapClass() { return Map.class; }
    public static Class<?> getAttributeModifierClass() { return nativeOr(Object.class, "net.minecraft.world.entity.ai.attributes.AttributeModifier"); }
    public static Class<?> getMobEffectClass() { return getMobEffectListClass(); }
    public static Class<?> getPacketDataSerializerClass() { return ByteBuf.class; }
    public static Class<?> getNbtCompressedStreamToolsClass() { return NbtBase.class; }
    public static Class<?> getTileEntityClass() { return nativeOr(Object.class, "net.minecraft.world.level.block.entity.BlockEntity", "net.minecraft.world.level.block.entity.TileEntity"); }
    public static Optional<Class<?>> getTeamParametersClass() { return optionalNative("net.minecraft.world.scores.PlayerTeam$Packed", "net.minecraft.world.scores.Team$Packed"); }
    public static Class<?> getComponentStyleClass() { return com.comphenix.protocol.wrappers.WrappedComponentStyle.class; }
    public static Optional<Class<?>> getNumberFormatClass() { return Optional.of(WrappedNumberFormat.class); }
    public static Optional<Class<?>> getBlankFormatClass() { return Optional.of(WrappedNumberFormat.Blank.class); }
    public static Optional<Class<?>> getFixedFormatClass() { return Optional.of(WrappedNumberFormat.Fixed.class); }
    public static Optional<Class<?>> getStyledFormatClass() { return Optional.of(WrappedNumberFormat.Styled.class); }
    public static Optional<Class<?>> getTeamColorClass() { return optionalNative("net.minecraft.ChatFormatting"); }
    public static Class<?> getMinecraftGsonClass() { return com.google.gson.Gson.class; }
    public static Class<?> getItemStackArrayClass() { return Array.newInstance(getItemStackClass(), 0).getClass(); }
    public static Class<?> getArrayClass(Class<?> component) { return Array.newInstance(component, 0).getClass(); }
    public static Class<?> getCraftItemStackClass() { return nativeOr(ItemStack.class, getCraftBukkitPackage() + ".inventory.CraftItemStack"); }
    public static Class<?> getCraftPlayerClass() { return nativeOr(Player.class, getCraftBukkitPackage() + ".entity.CraftPlayer"); }
    public static Class<?> getCraftWorldClass() { return nativeOr(World.class, getCraftBukkitPackage() + ".CraftWorld"); }
    public static Class<?> getCraftEntityClass() { return nativeOr(Entity.class, getCraftBukkitPackage() + ".entity.CraftEntity"); }
    public static Class<?> getCraftMessageClass() { return WrappedChatComponent.class; }
    public static Class<?> getPlayerInfoDataClass() { return PlayerInfoData.class; }
    public static Class<?> getEnumEntityUseActionClass() { return com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction.class; }
    public static MethodAccessor getEntityUseActionEnumMethodAccessor() { return unsupportedMethod("entity-use-action enum accessor"); }
    public static FieldAccessor getHandEntityUseActionEnumFieldAccessor(Object handle) { return unsupportedField("entity-use-action hand accessor"); }
    public static FieldAccessor getVec3EntityUseActionEnumFieldAccessor(Object handle) { return unsupportedField("entity-use-action vector accessor"); }
    public static boolean isPlayerInfoData(Object value) { return value instanceof PlayerInfoData || simpleName(value, "PlayerInfoData"); }
    public static Class<?> getIBlockDataClass() { return WrappedBlockData.class; }
    public static Class<?> getMultiBlockChangeInfoClass() { return com.comphenix.protocol.wrappers.MultiBlockChangeInfo.class; }
    public static Class<?> getMultiBlockChangeInfoArrayClass() { return Array.newInstance(getMultiBlockChangeInfoClass(), 0).getClass(); }
    public static Class<?> getGameStateClass() { return PacketType.class; }
    public static boolean signUpdateExists() { return true; }
    public static Class<?> getNonNullListClass() { return List.class; }
    public static MethodAccessor getNonNullListCreateAccessor() { return unsupportedMethod("non-null-list factory"); }
    public static Class<?> getCraftSoundClass() { return org.bukkit.Sound.class; }
    public static Class<?> getSectionPositionClass() { return BlockPosition.class; }
    public static ItemStack getBukkitItemStack(Object value) {
        if (value instanceof ItemStack) return (ItemStack) value;
        if (value instanceof com.github.retrooper.packetevents.protocol.item.ItemStack) {
            return SpigotConversionUtil.toBukkitItemStack((com.github.retrooper.packetevents.protocol.item.ItemStack) value);
        }
        return null;
    }
    public static Object getMinecraftItemStack(ItemStack stack) { return stack == null ? null : SpigotConversionUtil.fromBukkitItemStack(stack); }
    public static Optional<Class<?>> getOptionalClass(String name) { return optionalNative(name); }
    public static Class<?> getCraftBukkitClass(String name) { return getNullableNMS(new String[] {getCraftBukkitPackage() + "." + name, name}); }
    public static Class<?> getMinecraftClass(String name) { return getNullableNMS(new String[] {"net.minecraft." + name, name}); }
    public static Optional<Class<?>> getOptionalNMS(String packageName, String... names) { return Optional.ofNullable(getNullableNMS(packageName, names)); }
    public static Class<?> getNullableNMS(String packageName, String... names) {
        String[] candidates = new String[names.length + 1];
        candidates[0] = packageName;
        System.arraycopy(names, 0, candidates, 1, names.length);
        return getNullableNMS(candidates);
    }
    public static Class<?> getMinecraftClass(String packageName, String... names) { return getNullableNMS(packageName, names); }
    public static Class<?> getMinecraftLibraryClass(String name) { return getLibraryClass(name); }
    public static Optional<Class<?>> getOptionalLibraryClass(String name) { return Optional.ofNullable(getLibraryClassOrNull(name)); }
    public static String getNetworkManagerName() { return "net.minecraft.network.Connection"; }
    public static Object getPacketDataSerializer(Object value) { return value instanceof ByteBuf ? value : invokeNoArg(value, "getBuffer", "buffer"); }
    public static Object createPacketDataSerializer(int capacity) { return Unpooled.buffer(Math.max(0, capacity)); }
    public static Class<?> getNbtTagTypes() { return NbtBase.class; }
    public static Class<?> getChatDeserializer() { return WrappedChatComponent.class; }
    public static Class<?> getChatMutableComponentClass() { return WrappedChatComponent.class; }
    public static Class<?> getDimensionManager() { return World.Environment.class; }
    public static Class<?> getMerchantRecipeList() { return List.class; }
    public static Class<?> getResourceKey() { return com.comphenix.protocol.wrappers.MinecraftKey.class; }
    public static Class<?> getEntityTypes() { return org.bukkit.entity.EntityType.class; }
    public static Class<?> getParticleParam() { return WrappedParticle.class; }
    public static Class<?> getSectionPosition() { return BlockPosition.class; }
    public static Class<?> getChunkProviderServer() { return nativeOr(World.class, "net.minecraft.server.level.ServerChunkCache"); }
    public static Class<?> getPlayerChunkMap() { return nativeOr(Object.class, "net.minecraft.server.level.ChunkMap"); }
    public static Class<?> getIRegistry() { return Map.class; }
    public static Class<?> getBuiltInRegistries() { return Map.class; }
    public static Class<?> getAttributeBase() { return WrappedAttribute.class; }
    public static Class<?> getProfilePublicKeyClass() { return WrappedProfilePublicKey.class; }
    public static Class<?> getMessageSignatureClass() { return WrappedMessageSignature.class; }
    public static Class<?> getSaltedSignatureClass() { return WrappedSaltedSignature.class; }
    public static Class<?> getProfilePublicKeyDataClass() { return WrappedProfilePublicKey.WrappedProfileKeyData.class; }
    public static Class<?> getRemoteChatSessionClass() { return WrappedRemoteChatSessionData.class; }
    public static Class<?> getRemoteChatSessionDataClass() { return WrappedRemoteChatSessionData.class; }
    public static Class<?> getFastUtilClass(String name) { return getLibraryClass("it.unimi.dsi.fastutil." + name); }
    public static Class<?> getInt2ObjectMapClass() { return Map.class; }
    public static Class<?> getIntArrayListClass() { return List.class; }
    public static Class<?> getLibraryClass(String name) { return getLibraryClassOrNull(name); }
    public static Class<?> getLevelChunkPacketDataClass() { return WrappedLevelChunkData.ChunkData.class; }
    public static Class<?> getLightUpdatePacketDataClass() { return WrappedLevelChunkData.LightData.class; }
    public static Class<?> getBlockEntityTypeClass() { return WrappedRegistrable.class; }
    public static Class<?> getBlockEntityInfoClass() { return WrappedLevelChunkData.BlockEntityInfo.class; }
    public static Class<?> getDynamicOpsClass() { return com.comphenix.protocol.wrappers.codecs.WrappedDynamicOps.class; }
    public static Class<?> getJsonOpsClass() { return com.comphenix.protocol.wrappers.codecs.WrappedDynamicOps.class; }
    public static Class<?> getNbtOpsClass() { return com.comphenix.protocol.wrappers.codecs.WrappedDynamicOps.class; }
    public static Class<?> getCodecClass() { return com.comphenix.protocol.wrappers.codecs.WrappedCodec.class; }
    public static Class<?> getHolderClass() { return WrappedRegistrable.class; }
    public static Class<?> getCraftServer() { try { return Bukkit.getServer().getClass(); } catch (Throwable ignored) { return Object.class; } }
    public static Class<?> getHolderLookupProviderClass() { return Object.class; }
    public static Class<?> getRegistryAccessClass() { return Map.class; }
    public static Class<?> getProtocolInfoClass() { return ProtocolInfoWrapper.class; }
    public static Class<?> getProtocolInfoUnboundClass() { return ProtocolInfoWrapper.class; }
    public static Class<?> getPacketFlowClass() { return PacketType.Sender.class; }
    public static Class<?> getStreamCodecClass() { return WrappedStreamCodec.class; }
    public static Optional<Class<?>> getRegistryFriendlyByteBufClass() { return optionalNative("net.minecraft.network.RegistryFriendlyByteBuf"); }
    public static boolean isMojangMapped() { return getNullableNMS(new String[] {"net.minecraft.world.entity.Entity"}) != null; }
    public static Class<?> getPositionMoveRotationClass() { return WrappedPositionMoveRotation.class; }

    public static Class<?> getNullableNMS(String... names) {
        for (String name : names) {
            if (name == null || name.isEmpty()) continue;
            try { return Class.forName(name, false, MinecraftReflection.class.getClassLoader()); }
            catch (ClassNotFoundException ignored) { }
        }
        return null;
    }

    private static Optional<Class<?>> optionalNative(String... names) { return Optional.ofNullable(getNullableNMS(names)); }
    private static Class<?> nativeOr(Class<?> fallback, String... names) {
        Class<?> value = getNullableNMS(names);
        return value == null ? fallback : value;
    }
    private static boolean simpleName(Object value, String... names) {
        if (value == null) return false;
        String simple = value instanceof Class<?> c ? c.getSimpleName() : value.getClass().getSimpleName();
        for (String name : names) if (simple.equals(name)) return true;
        return false;
    }
    private static Object invokeNoArg(Object target, String... names) {
        if (target == null) return null;
        for (String name : names) {
            try {
                Method method = target.getClass().getMethod(name);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (ReflectiveOperationException | RuntimeException ignored) { }
        }
        return null;
    }
    private static Class<?> getLibraryClassOrNull(String name) {
        if (name == null || name.isEmpty()) return null;
        Class<?> direct = getNullableNMS(new String[] {name});
        if (direct != null) return direct;
        return getNullableNMS(new String[] {"net.minecraft." + name});
    }
    private static MethodAccessor unsupportedMethod(String capability) {
        throw new UnsupportedOperationException("ProtocolLib reflection capability unavailable: " + capability);
    }
    private static FieldAccessor unsupportedField(String capability) {
        throw new UnsupportedOperationException("ProtocolLib reflection capability unavailable: " + capability);
    }

    private static final class ComponentClassHolder {
        private static final Class<?> COMPONENT = resolveComponent();
        private static Class<?> resolveComponent() {
            try { return Class.forName("net.kyori.adventure.text.Component", false, MinecraftReflection.class.getClassLoader()); }
            catch (ClassNotFoundException ignored) { return WrappedChatComponent.class; }
        }
    }
}
