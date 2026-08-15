package com.comphenix.protocol.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.StreamSerializer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
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
import com.comphenix.protocol.wrappers.WrappedTeamParameters;
import com.comphenix.protocol.wrappers.WrappedStatistic;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.comphenix.protocol.wrappers.CustomPacketPayloadWrapper;
import com.comphenix.protocol.wrappers.Either;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.time.Instant;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** Common typed field views shared by PacketContainer and InternalStructure. */
public abstract class AbstractStructure {
    protected Object handle;
    protected StructureModifier<Object> structureModifier;

    protected AbstractStructure() {
        this(null, new StructureModifier<Object>(Object.class));
    }

    protected AbstractStructure(Object handle, StructureModifier<Object> modifier) {
        this.handle = handle;
        this.structureModifier = modifier == null
                ? new StructureModifier<>(handle, Object.class) : modifier.withTarget(handle);
    }

    public Object getHandle() { return handle; }
    public StructureModifier<Object> getModifier() { return structureModifier; }
    public <T> StructureModifier<T> getSpecificModifier(Class<T> primitiveType) { return structureModifier.withType(primitiveType); }
    public StructureModifier<Byte> getBytes() { return getSpecificModifier(byte.class); }
    public StructureModifier<Boolean> getBooleans() { return getSpecificModifier(boolean.class); }
    public StructureModifier<Short> getShorts() { return getSpecificModifier(short.class); }
    public StructureModifier<Integer> getIntegers() { return getSpecificModifier(int.class); }
    public StructureModifier<Long> getLongs() { return getSpecificModifier(long.class); }
    public StructureModifier<Float> getFloats() { return getSpecificModifier(float.class); }
    public StructureModifier<Float> getFloat() { return getFloats(); }
    public StructureModifier<Double> getDoubles() { return getSpecificModifier(double.class); }
    public StructureModifier<String> getStrings() { return getSpecificModifier(String.class); }
    public StructureModifier<UUID> getUUIDs() { return getSpecificModifier(UUID.class); }
    public StructureModifier<String[]> getStringArrays() { return getSpecificModifier(String[].class); }
    public StructureModifier<byte[]> getByteArrays() { return getSpecificModifier(byte[].class); }
    public StreamSerializer getByteArraySerializer() { return StreamSerializer.getDefault(); }
    public StructureModifier<int[]> getIntegerArrays() { return getSpecificModifier(int[].class); }
    public StructureModifier<short[]> getShortArrays() { return getSpecificModifier(short[].class); }
    public StructureModifier<ItemStack> getItemModifier() { return getSpecificModifier(ItemStack.class); }
    public StructureModifier<ItemStack[]> getItemArrayModifier() { return getSpecificModifier(ItemStack[].class); }
    public StructureModifier<List<ItemStack>> getItemListModifier() { return getSpecificModifier((Class) List.class); }
    public StructureModifier<Map<WrappedStatistic, Integer>> getStatisticMaps() { return getSpecificModifier((Class) Map.class); }
    public StructureModifier<WorldType> getWorldTypeModifier() { return getSpecificModifier(WorldType.class); }
    public StructureModifier<WrappedDataWatcher> getDataWatcherModifier() { return getSpecificModifier(WrappedDataWatcher.class); }
    public StructureModifier<Entity> getEntityModifier(World world) { return getSpecificModifier(Entity.class); }
    public StructureModifier<Entity> getEntityModifier(PacketEvent event) { return getSpecificModifier(Entity.class); }
    public StructureModifier<EntityType> getEntityTypeModifier() { return getSpecificModifier(EntityType.class); }
    public StructureModifier<BlockPosition> getBlockPositionModifier() { return getSpecificModifier(BlockPosition.class); }
    public StructureModifier<WrappedRegistrable> getRegistrableModifier(Class<?> genericType) { return getSpecificModifier((Class) genericType); }
    public StructureModifier<WrappedRegistrable> getBlockEntityTypeModifier() { return getSpecificModifier(WrappedRegistrable.class); }
    public StructureModifier<ChunkCoordIntPair> getChunkCoordIntPairs() { return getSpecificModifier(ChunkCoordIntPair.class); }
    public StructureModifier<NbtBase<?>> getNbtModifier() { return getSpecificModifier((Class) NbtBase.class); }
    public StructureModifier<List<NbtBase<?>>> getListNbtModifier() { return getSpecificModifier((Class) List.class); }
    public StructureModifier<Vector> getVectors() { return getSpecificModifier(Vector.class); }
    public StructureModifier<List<WrappedAttribute>> getAttributeCollectionModifier() { return getSpecificModifier((Class) List.class); }
    public StructureModifier<List<BlockPosition>> getBlockPositionCollectionModifier() { return getSpecificModifier((Class) List.class); }
    public StructureModifier<List<WrappedWatchableObject>> getWatchableCollectionModifier() { return getSpecificModifier((Class) List.class); }
    public StructureModifier<List<WrappedDataValue>> getDataValueCollectionModifier() { return getSpecificModifier((Class) List.class); }
    public StructureModifier<Material> getBlocks() { return getSpecificModifier(Material.class); }
    public StructureModifier<WrappedGameProfile> getGameProfiles() { return getSpecificModifier(WrappedGameProfile.class); }
    public StructureModifier<WrappedBlockData> getBlockData() { return getSpecificModifier(WrappedBlockData.class); }
    public StructureModifier<WrappedBlockData[]> getBlockDataArrays() { return getSpecificModifier(WrappedBlockData[].class); }
    public StructureModifier<MultiBlockChangeInfo[]> getMultiBlockChangeInfoArrays() { return getSpecificModifier(MultiBlockChangeInfo[].class); }
    public StructureModifier<WrappedChatComponent> getChatComponents() { return getSpecificModifier(WrappedChatComponent.class); }
    public StructureModifier<WrappedChatComponent[]> getChatComponentArrays() { return getSpecificModifier(WrappedChatComponent[].class); }
    public StructureModifier<WrappedServerPing> getServerPings() { return getSpecificModifier(WrappedServerPing.class); }
    public StructureModifier<List<PlayerInfoData>> getPlayerInfoDataLists() { return getSpecificModifier((Class) List.class); }
    public StructureModifier<WrappedParticle> getNewParticles() { return getSpecificModifier(WrappedParticle.class); }
    public StructureModifier<List<PlayerInfoData>> getPlayerInfoData() { return getPlayerInfoDataLists(); }
    public StructureModifier<PotionEffectType> getEffectTypes() { return getSpecificModifier(PotionEffectType.class); }
    public StructureModifier<Sound> getSoundEffects() { return getSpecificModifier(Sound.class); }
    public StructureModifier<EnumWrappers.ItemSlot> getItemSlots() { return getSpecificModifier(EnumWrappers.ItemSlot.class); }
    public StructureModifier<EnumWrappers.Hand> getHands() { return getSpecificModifier(EnumWrappers.Hand.class); }
    public StructureModifier<EnumWrappers.Direction> getDirections() { return getSpecificModifier(EnumWrappers.Direction.class); }
    public StructureModifier<EnumWrappers.ChatType> getChatTypes() { return getSpecificModifier(EnumWrappers.ChatType.class); }
    public StructureModifier<EnumWrappers.DisplaySlot> getDisplaySlots() { return getSpecificModifier(EnumWrappers.DisplaySlot.class); }
    public StructureModifier<EnumWrappers.RenderType> getRenderTypes() { return getSpecificModifier(EnumWrappers.RenderType.class); }
    public StructureModifier<EnumWrappers.ChatFormatting> getChatFormattings() { return getSpecificModifier(EnumWrappers.ChatFormatting.class); }
    public StructureModifier<Optional<WrappedTeamParameters>> getOptionalTeamParameters() { return getSpecificModifier((Class) Optional.class); }
    public StructureModifier<WrappedNumberFormat> getNumberFormats() { return getSpecificModifier(WrappedNumberFormat.class); }
    public StructureModifier<CustomPacketPayloadWrapper> getCustomPacketPayloads() { return getSpecificModifier(CustomPacketPayloadWrapper.class); }
    public StructureModifier<Integer> getDimensions() { return getSpecificModifier(int.class); }
    public StructureModifier<World> getDimensionTypes() { return getSpecificModifier(World.class); }
    public StructureModifier<List<MerchantRecipe>> getMerchantRecipeLists() { return getSpecificModifier((Class) List.class); }
    public StructureModifier<List<Pair<EnumWrappers.ItemSlot, ItemStack>>> getSlotStackPairLists() { return getSpecificModifier((Class) List.class); }
    public StructureModifier<MovingObjectPositionBlock> getMovingBlockPositions() { return getSpecificModifier(MovingObjectPositionBlock.class); }
    public StructureModifier<World> getWorldKeys() { return getSpecificModifier(World.class); }
    public StructureModifier<BlockPosition> getSectionPositions() { return getSpecificModifier(BlockPosition.class); }
    public StructureModifier<Integer> getGameStateIDs() { return getSpecificModifier(int.class); }
    public StructureModifier<List<Integer>> getIntLists() { return getSpecificModifier((Class) List.class); }
    public StructureModifier<List<UUID>> getUUIDLists() { return getSpecificModifier((Class) List.class); }
    public StructureModifier<Instant> getInstants() { return getSpecificModifier(Instant.class); }
    public StructureModifier<WrappedProfilePublicKey> getProfilePublicKeys() { return getSpecificModifier(WrappedProfilePublicKey.class); }
    public StructureModifier<WrappedProfilePublicKey.WrappedProfileKeyData> getProfilePublicKeyData() { return getSpecificModifier(WrappedProfilePublicKey.WrappedProfileKeyData.class); }
    public StructureModifier<WrappedRemoteChatSessionData> getRemoteChatSessionData() { return getSpecificModifier(WrappedRemoteChatSessionData.class); }
    public StructureModifier<WrappedLevelChunkData.ChunkData> getLevelChunkData() { return getSpecificModifier(WrappedLevelChunkData.ChunkData.class); }
    public StructureModifier<WrappedLevelChunkData.LightData> getLightUpdateData() { return getSpecificModifier(WrappedLevelChunkData.LightData.class); }
    public StructureModifier<Either<byte[], WrappedSaltedSignature>> getLoginSignatures() { return getSpecificModifier((Class) Either.class); }
    public StructureModifier<WrappedSaltedSignature> getSignatures() { return getSpecificModifier(WrappedSaltedSignature.class); }
    public StructureModifier<WrappedMessageSignature> getMessageSignatures() { return getSpecificModifier(WrappedMessageSignature.class); }
    public StructureModifier<EnumWrappers.ClientIntent> getClientIntents() { return getSpecificModifier(EnumWrappers.ClientIntent.class); }
    public StructureModifier<EnumWrappers.ClientCommand> getClientCommands() { return getSpecificModifier(EnumWrappers.ClientCommand.class); }
    public StructureModifier<EnumWrappers.ChatVisibility> getChatVisibilities() { return getSpecificModifier(EnumWrappers.ChatVisibility.class); }
    public StructureModifier<EnumWrappers.Difficulty> getDifficulties() { return getSpecificModifier(EnumWrappers.Difficulty.class); }
    public StructureModifier<EnumWrappers.EntityUseAction> getEntityUseActions() { return getSpecificModifier(EnumWrappers.EntityUseAction.class); }
    public StructureModifier<WrappedEnumEntityUseAction> getEnumEntityUseActions() { return getSpecificModifier(WrappedEnumEntityUseAction.class); }
    public StructureModifier<EnumWrappers.NativeGameMode> getGameModes() { return getSpecificModifier(EnumWrappers.NativeGameMode.class); }
    public StructureModifier<EnumWrappers.ResourcePackStatus> getResourcePackStatus() { return getSpecificModifier(EnumWrappers.ResourcePackStatus.class); }
    public StructureModifier<EnumWrappers.PlayerInfoAction> getPlayerInfoAction() { return getSpecificModifier(EnumWrappers.PlayerInfoAction.class); }
    public StructureModifier<Set<EnumWrappers.PlayerInfoAction>> getPlayerInfoActions() { return getSpecificModifier((Class) Set.class); }
    public StructureModifier<EnumWrappers.TitleAction> getTitleActions() { return getSpecificModifier(EnumWrappers.TitleAction.class); }
    public StructureModifier<EnumWrappers.WorldBorderAction> getWorldBorderActions() { return getSpecificModifier(EnumWrappers.WorldBorderAction.class); }
    public StructureModifier<EnumWrappers.CombatEventType> getCombatEvents() { return getSpecificModifier(EnumWrappers.CombatEventType.class); }
    public StructureModifier<EnumWrappers.PlayerDigType> getPlayerDigTypes() { return getSpecificModifier(EnumWrappers.PlayerDigType.class); }
    public StructureModifier<EnumWrappers.PlayerAction> getPlayerActions() { return getSpecificModifier(EnumWrappers.PlayerAction.class); }
    public StructureModifier<EnumWrappers.ScoreboardAction> getScoreboardActions() { return getSpecificModifier(EnumWrappers.ScoreboardAction.class); }
    public StructureModifier<EnumWrappers.Particle> getParticles() { return getSpecificModifier(EnumWrappers.Particle.class); }
    public StructureModifier<EnumWrappers.SoundCategory> getSoundCategories() { return getSpecificModifier(EnumWrappers.SoundCategory.class); }
    public StructureModifier<WrappedPositionMoveRotation> getPositionMoveRotation() { return getSpecificModifier(WrappedPositionMoveRotation.class); }
    public StructureModifier<MinecraftKey> getMinecraftKeys() { return getSpecificModifier(MinecraftKey.class); }
    public StructureModifier<PacketType.Protocol> getProtocols() { return getSpecificModifier(PacketType.Protocol.class); }
    public <T> StructureModifier<T> getHolders(Class<?> genericType, EquivalentConverter<T> converter) { return structureModifier.withType((Class) genericType, converter); }
    public <L,R> StructureModifier<Either<L, R>> getEithers(EquivalentConverter<L> left, EquivalentConverter<R> right) { return getSpecificModifier((Class) Either.class); }
    public <K,V> StructureModifier<Map<K,V>> getMaps(EquivalentConverter<K> key, EquivalentConverter<V> value) { return getSpecificModifier((Class) Map.class); }
    public <E> StructureModifier<Set<E>> getSets(EquivalentConverter<E> converter) { return getSpecificModifier((Class) Set.class); }
    public <E> StructureModifier<List<E>> getLists(EquivalentConverter<E> converter) { return getSpecificModifier((Class) List.class); }
    public <T extends Enum<T>> StructureModifier<T> getEnumModifier(Class<T> enumClass, Class<?> nmsClass) { return getSpecificModifier(enumClass); }
    public <T extends Enum<T>> StructureModifier<T> getEnumModifier(Class<T> enumClass, int index) { return getSpecificModifier(enumClass); }
    public <T> StructureModifier<Optional<T>> getOptionals(EquivalentConverter<T> converter) { return getSpecificModifier((Class) Optional.class); }
    public StructureModifier<Iterable<PacketContainer>> getPacketBundles() { return getSpecificModifier((Class) Iterable.class); }

    static class ComponentArrayConverter implements EquivalentConverter<WrappedChatComponent[]> {
        public WrappedChatComponent[] getSpecific(Object value) { return value instanceof WrappedChatComponent[] array ? array : null; }
        public Object getGeneric(WrappedChatComponent[] value) { return value; }
        public Class<WrappedChatComponent[]> getSpecificType() { return WrappedChatComponent[].class; }
        public Class<?> getGenericType() { return WrappedChatComponent[].class; }
    }
    static class ItemStackArrayConverter implements EquivalentConverter<ItemStack[]> {
        public ItemStack[] getSpecific(Object value) { return value instanceof ItemStack[] array ? array : null; }
        public Object getGeneric(ItemStack[] value) { return value; }
        public Class<ItemStack[]> getSpecificType() { return ItemStack[].class; }
    }
    static class LegacyComponentConverter implements EquivalentConverter<WrappedChatComponent[]> {
        public WrappedChatComponent[] getSpecific(Object value) { return value instanceof WrappedChatComponent[] array ? array : null; }
        public Object getGeneric(WrappedChatComponent[] value) { return value; }
        public Class<WrappedChatComponent[]> getSpecificType() { return WrappedChatComponent[].class; }
    }
    static class NBTComponentConverter implements EquivalentConverter<WrappedChatComponent[]> {
        public WrappedChatComponent[] getSpecific(Object value) { return value instanceof WrappedChatComponent[] array ? array : null; }
        public Object getGeneric(WrappedChatComponent[] value) { return value; }
        public Class<WrappedChatComponent[]> getSpecificType() { return WrappedChatComponent[].class; }
    }
}
