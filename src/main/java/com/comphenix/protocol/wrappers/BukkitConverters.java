package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor.Unwrapper;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** Common Bukkit/PacketEvents conversion registry. Unsupported NMS-only values fail explicitly. */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitConverters {
    public BukkitConverters() {}
    public static <T> EquivalentConverter<Map<T,T>> getMapConverter(EquivalentConverter<T> key,EquivalentConverter<T> value){return new EquivalentConverter<>(){public Map<T,T> getSpecific(Object raw){if(!(raw instanceof Map<?,?> map))return null;Map<T,T> out=new HashMap<>();for(Map.Entry<?,?> e:map.entrySet())out.put(key.getSpecific(e.getKey()),value.getSpecific(e.getValue()));return out;}public Object getGeneric(Map<T,T> value){Map<Object,Object> out=new HashMap<>();for(Map.Entry<T,T> e:value.entrySet())out.put(key.getGeneric(e.getKey()),valueConverter(value,e.getValue()));return out;}private Object valueConverter(Map<T,T> ignored,T v){return value.getGeneric(v);}public Class<Map<T,T>> getSpecificType(){return (Class)Map.class;}public Class<?> getGenericType(){return Map.class;}};}
    public static <T> EquivalentConverter<List<T>> getListConverter(Class<?> listClass,EquivalentConverter<T> item){return listConverter(item);}
    public static <T> EquivalentConverter<List<T>> getListConverter(EquivalentConverter<T> item){return listConverter(item);}
    public static <T> EquivalentConverter<Iterable<? extends T>> getArrayConverter(Class<?> itemClass,EquivalentConverter<T> item){return (EquivalentConverter) Converters.iterable(item,ArrayList::new,ArrayList::new);}
    private static <T> EquivalentConverter<List<T>> listConverter(EquivalentConverter<T> item){return new EquivalentConverter<>(){public List<T> getSpecific(Object raw){if(!(raw instanceof Iterable<?> values))return null;List<T> out=new ArrayList<>();for(Object v:values)out.add(item.getSpecific(v));return out;}public Object getGeneric(List<T> values){List<Object> out=new ArrayList<>();for(T v:values)out.add(item.getGeneric(v));return out;}public Class<List<T>> getSpecificType(){return (Class)List.class;}public Class<?> getGenericType(){return List.class;}};}
    public static <T> EquivalentConverter<Set<T>> getSetConverter(EquivalentConverter<T> item){return new EquivalentConverter<>(){public Set<T> getSpecific(Object raw){if(!(raw instanceof Iterable<?> values))return null;Set<T> out=new java.util.HashSet<>();for(Object v:values)out.add(item.getSpecific(v));return out;}public Object getGeneric(Set<T> values){Set<Object> out=new java.util.HashSet<>();for(T v:values)out.add(item.getGeneric(v));return out;}public Class<Set<T>> getSpecificType(){return (Class)Set.class;}public Class<?> getGenericType(){return Set.class;}};}
    public static <A,B> EquivalentConverter<Pair<A,B>> getPairConverter(EquivalentConverter<A> a,EquivalentConverter<B> b){return new EquivalentConverter<>(){public Pair<A,B> getSpecific(Object raw){if(!(raw instanceof Pair<?,?> p))return null;return new Pair<>(a.getSpecific(p.getFirst()),b.getSpecific(p.getSecond()));}public Object getGeneric(Pair<A,B> p){return p==null?null:new Pair<>(a.getGeneric(p.getFirst()),b.getGeneric(p.getSecond()));}public Class<Pair<A,B>> getSpecificType(){return (Class)Pair.class;}public Class<?> getGenericType(){return Pair.class;}};}
    public static <A,B> EquivalentConverter<Either<A,B>> getEitherConverter(EquivalentConverter<A> a,EquivalentConverter<B> b){return new EquivalentConverter<>(){public Either<A,B> getSpecific(Object raw){return raw instanceof Either<?,?> e?(Either<A,B>)e:null;}public Object getGeneric(Either<A,B> e){return e;}public Class<Either<A,B>> getSpecificType(){return (Class)Either.class;}public Class<?> getGenericType(){return Either.class;}};}
    public static EquivalentConverter<WrappedGameProfile> getWrappedGameProfileConverter(){return WrappedGameProfile.getConverter();}
    public static EquivalentConverter<WrappedChatComponent> getWrappedChatComponentConverter(){return WrappedChatComponent.getConverter();}
    public static EquivalentConverter<WrappedBlockData> getWrappedBlockDataConverter(){return new EquivalentConverter<>(){public WrappedBlockData getSpecific(Object v){return WrappedBlockData.fromHandle(v);}public Object getGeneric(WrappedBlockData v){return v==null?null:v.getHandle();}public Class<WrappedBlockData> getSpecificType(){return WrappedBlockData.class;}public Class<?> getGenericType(){return WrappedBlockData.class;}};}
    public static EquivalentConverter<WrappedRegistrable> getWrappedRegistrable(Class<?> type){return Converters.passthrough(WrappedRegistrable.class);}
    public static EquivalentConverter<WrappedAttribute> getWrappedAttributeConverter(){return new EquivalentConverter<>(){public WrappedAttribute getSpecific(Object v){return v instanceof WrappedAttribute a?a:null;}public Object getGeneric(WrappedAttribute v){return v;}public Class<WrappedAttribute> getSpecificType(){return WrappedAttribute.class;}public Class<?> getGenericType(){return WrappedAttribute.class;}};}
    public static EquivalentConverter<WrappedProfilePublicKey> getWrappedProfilePublicKeyConverter(){return Converters.passthrough(WrappedProfilePublicKey.class);}
    public static EquivalentConverter<WrappedProfilePublicKey.WrappedProfileKeyData> getWrappedPublicKeyDataConverter(){return Converters.passthrough(WrappedProfilePublicKey.WrappedProfileKeyData.class);}
    public static EquivalentConverter<WrappedRemoteChatSessionData> getWrappedRemoteChatSessionDataConverter(){return Converters.passthrough(WrappedRemoteChatSessionData.class);}
    public static EquivalentConverter<WrappedSaltedSignature> getWrappedSignatureConverter(){return Converters.passthrough(WrappedSaltedSignature.class);}
    public static EquivalentConverter<WrappedMessageSignature> getWrappedMessageSignatureConverter(){return Converters.passthrough(WrappedMessageSignature.class);}
    public static EquivalentConverter<WrappedLevelChunkData.ChunkData> getWrappedChunkDataConverter(){return Converters.passthrough(WrappedLevelChunkData.ChunkData.class);}
    public static EquivalentConverter<WrappedLevelChunkData.LightData> getWrappedLightDataConverter(){return Converters.passthrough(WrappedLevelChunkData.LightData.class);}
    public static EquivalentConverter<WrappedTeamParameters> getWrappedTeamParametersConverter(){return Converters.passthrough(WrappedTeamParameters.class);}
    public static EquivalentConverter<WrappedNumberFormat> getWrappedNumberFormatConverter(){return Converters.passthrough(WrappedNumberFormat.class);}
    public static EquivalentConverter<PacketContainer> getPacketContainerConverter(){return Converters.passthrough(PacketContainer.class);}
    public static EquivalentConverter<WrappedDataWatcher> getDataWatcherConverter(){return WrappedDataWatcher.getConverter();}
    public static EquivalentConverter<WrappedDataValue> getDataValueConverter(){return Converters.handle(v->v.getHandle(),WrappedDataValue::new,WrappedDataValue.class);}
    public static EquivalentConverter<WrappedParticle> getParticleConverter(){return Converters.passthrough(WrappedParticle.class);}
    public static EquivalentConverter<WrappedWatchableObject> getWatchableObjectConverter(){return Converters.handle(WrappedWatchableObject::getHandle,v->new WrappedWatchableObject((com.github.retrooper.packetevents.protocol.entity.data.EntityData<?>)v),WrappedWatchableObject.class);}
    public static EquivalentConverter<WrappedServerPing> getWrappedServerPingConverter(){return new EquivalentConverter<>(){public WrappedServerPing getSpecific(Object v){return WrappedServerPing.fromHandle(v);}public Object getGeneric(WrappedServerPing v){return v==null?null:v.getHandle();}public Class<WrappedServerPing> getSpecificType(){return WrappedServerPing.class;}public Class<?> getGenericType(){return WrappedServerPing.class;}};}
    public static EquivalentConverter<WrappedStatistic> getWrappedStatisticConverter(){return new EquivalentConverter<>(){public WrappedStatistic getSpecific(Object v){return WrappedStatistic.fromHandle(v);}public Object getGeneric(WrappedStatistic v){return v==null?null:v.getHandle();}public Class<WrappedStatistic> getSpecificType(){return WrappedStatistic.class;}public Class<?> getGenericType(){return WrappedStatistic.class;}};}
    public static EquivalentConverter<ItemStack> getItemStackConverter(){return new EquivalentConverter<>(){public ItemStack getSpecific(Object v){return v instanceof com.github.retrooper.packetevents.protocol.item.ItemStack i?io.github.retrooper.packetevents.util.SpigotConversionUtil.toBukkitItemStack(i):(ItemStack)v;}public Object getGeneric(ItemStack v){return v==null?null:io.github.retrooper.packetevents.util.SpigotConversionUtil.fromBukkitItemStack(v);}public Class<ItemStack> getSpecificType(){return ItemStack.class;}public Class<?> getGenericType(){return com.github.retrooper.packetevents.protocol.item.ItemStack.class;}};}
    public static EquivalentConverter<Vector> getVectorConverter(){return Converters.passthrough(Vector.class);}
    public static EquivalentConverter<PotionEffectType> getEffectTypeConverter(){return Converters.passthrough(PotionEffectType.class);}
    public static EquivalentConverter<DamageType> getDamageTypeConverter(){return Converters.passthrough(DamageType.class);}
    public static EquivalentConverter<World> getWorldKeyConverter(){return Converters.passthrough(World.class);}
    public static EquivalentConverter<World> getDimensionConverter(){return Converters.passthrough(World.class);}
    public static EquivalentConverter<Integer> getDimensionIDConverter(){return Converters.passthrough(Integer.class);}
    public static EquivalentConverter<List<MerchantRecipe>> getMerchantRecipeListConverter(){return (EquivalentConverter) Converters.passthrough(List.class);}
    public static EquivalentConverter<BlockPosition> getSectionPositionConverter(){return BlockPosition.getConverter();}
    public static EquivalentConverter<Integer> getGameStateConverter(){return Converters.passthrough(Integer.class);}
    public static EquivalentConverter<EntityType> getEntityTypeConverter(){return Converters.passthrough(EntityType.class);}
    public static EquivalentConverter<Material> getBlockConverter(){return Converters.passthrough(Material.class);}
    public static EquivalentConverter<World> getWorldConverter(){return Converters.passthrough(World.class);}
    public static EquivalentConverter<WorldType> getWorldTypeConverter(){return Converters.passthrough(WorldType.class);}
    public static EquivalentConverter<PotionEffect> getPotionEffectConverter(){return Converters.passthrough(PotionEffect.class);}
    public static EquivalentConverter<Sound> getSoundConverter(){return Converters.passthrough(Sound.class);}
    public static EquivalentConverter<Advancement> getAdvancementConverter(){return Converters.passthrough(Advancement.class);}
    public static EquivalentConverter<Entity> getEntityConverter(World world){return Converters.passthrough(Entity.class);}
    public static EquivalentConverter<NbtBase<?>> getNbtConverter(){return new EquivalentConverter<>(){public NbtBase<?> getSpecific(Object v){return v instanceof NbtBase<?> n?n:null;}public Object getGeneric(NbtBase<?> v){return v;}public Class<NbtBase<?>> getSpecificType(){return (Class)NbtBase.class;}public Class<?> getGenericType(){return NbtBase.class;}};}
    public static <T> Unwrapper asUnwrapper(Class<?> nativeType,EquivalentConverter<T> converter){return value->converter.getSpecificType().isInstance(value)?converter.getGeneric((T)value):null;}
    public static Map<Class<?>,EquivalentConverter<Object>> getConvertersForGeneric(){return Collections.emptyMap();}
    public static List<Unwrapper> getUnwrappers(){return Collections.emptyList();}

    enum DimensionImpl { OVERWORLD_IMPL, THE_NETHER_IMPL, THE_END_IMPL }
    public abstract static class IgnoreNullConverter<TType> implements EquivalentConverter<TType> {
        public final Object getGeneric(TType value){return value==null?null:getGenericValue(value);}
        public abstract Object getGenericValue(TType value);
        public final TType getSpecific(Object value){return value==null?null:getSpecificValue(value);}
        public abstract TType getSpecificValue(Object value);
        public boolean equals(Object value){return value!=null&&getClass()==value.getClass();} public int hashCode(){return getClass().hashCode();}
    }
    abstract static class WorldSpecificConverter<TType> implements EquivalentConverter<TType> {
        public boolean equals(Object value){return value!=null&&getClass()==value.getClass();} public int hashCode(){return getClass().hashCode();}
    }
}
