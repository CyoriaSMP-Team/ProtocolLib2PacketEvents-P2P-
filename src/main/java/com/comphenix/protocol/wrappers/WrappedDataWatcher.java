/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 *
 * Copyright (C) 2026 CyoriaSMP Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData;
import com.github.retrooper.packetevents.protocol.entity.villager.level.VillagerLevel;
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfessions;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import com.github.retrooper.packetevents.protocol.nbt.NBTByteArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTDouble;
import com.github.retrooper.packetevents.protocol.nbt.NBTEnd;
import com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.nbt.NBTIntArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTLong;
import com.github.retrooper.packetevents.protocol.nbt.NBTLongArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTNumber;
import com.github.retrooper.packetevents.protocol.nbt.NBTShort;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Collections;
import java.util.Locale;

/**
 * Entity metadata container, mirroring ProtocolLib's {@code WrappedDataWatcher}.
 * <p>
 * ProtocolLib wraps NMS's {@code DataWatcher}; this wraps the {@code List<EntityData>} that
 * PacketEvents' entity metadata wrappers actually hold, so it works across every Minecraft
 * version PacketEvents supports without any version-specific field lookups.
 * <p>
 * One deliberate difference from ProtocolLib: because PacketEvents stores the serializer type
 * alongside each entry, writing a <em>new</em> index requires knowing the type. Use
 * {@link #setObject(int, EntityDataType, Object)} for new indices; {@link #setObject(int, Object)}
 * infers the type from the Java class of the value and is intended for updating entries that
 * already exist.
 */
public class WrappedDataWatcher implements IDataWatcher {

    private final List<EntityData<?>> handle;
    private Entity entity;

    public WrappedDataWatcher() {
        this(new ArrayList<>());
    }

    public WrappedDataWatcher(List<?> handle) {
        boolean packetEventsList = handle != null;
        if (handle != null) {
            for (Object value : handle) {
                if (!(value instanceof EntityData<?>)) {
                    packetEventsList = false;
                    break;
                }
            }
        }
        if (packetEventsList) {
            @SuppressWarnings("unchecked")
            List<EntityData<?>> shared = (List<EntityData<?>>) (List<?>) handle;
            this.handle = shared;
        } else {
            this.handle = new ArrayList<>();
            if (handle != null) {
                for (Object value : handle) {
                    if (value instanceof WrappedWatchableObject watchable
                            && watchable.getEntityData() != null) {
                        this.handle.add(watchable.getEntityData());
                    }
                }
            }
        }
    }

    /** Wraps a raw PacketEvents metadata list when supplied through an Object-typed API. */
    @SuppressWarnings("unchecked")
    public WrappedDataWatcher(Object handle) {
        this(handle instanceof List<?> ? (List<?>) handle : new ArrayList<>());
    }

    /** Wraps the metadata list held by a PacketEvents wrapper, sharing storage with it. */
    public static WrappedDataWatcher fromHandle(List<EntityData<?>> handle) {
        return handle == null ? null : new WrappedDataWatcher(handle);
    }

    public WrappedDataWatcher(Entity entity) {
        this();
        this.entity = entity;
    }

    public static WrappedDataWatcher getEntityWatcher(Entity entity) {
        return new WrappedDataWatcher(entity);
    }

    /** The value at the given metadata index, or {@code null} if the index is not present. */
    public Object getObject(int index) {
        EntityData<?> data = find(index);
        return data == null ? null : data.getValue();
    }

    /** The full entry at the given index, or {@code null} if absent. */
    public WrappedWatchableObject getWatchableObject(int index) {
        EntityData<?> data = find(index);
        return data == null ? null : new WrappedWatchableObject(data);
    }

    public WrappedWatchableObject removeObject(int index) {
        WrappedWatchableObject old = getWatchableObject(index);
        handle.removeIf(data -> data.getIndex() == index);
        return old;
    }

    public boolean hasIndex(int index) {
        return find(index) != null;
    }

    /**
     * Sets the value at an index, inferring the PacketEvents serializer from the value's Java
     * type when the index does not exist yet.
     *
     * @throws IllegalArgumentException if the index is new and the type cannot be inferred
     */
    @SuppressWarnings("unchecked")
    public void setObject(int index, Object value) {
        EntityData<?> existing = find(index);
        if (existing != null) {
            ((EntityData<Object>) existing).setValue(value);
            return;
        }
        EntityDataType<?> type = inferType(value);
        if (type == null) {
            throw new IllegalArgumentException("Cannot infer an entity data type for "
                    + (value == null ? "null" : value.getClass().getName()) + " at index " + index
                    + "; use setObject(int, EntityDataType, Object) to state the type explicitly");
        }
        setObject(index, type, value);
    }

    /** Sets the value at an index with an explicit PacketEvents serializer type. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public WrappedDataWatcher setObject(int index, EntityDataType<?> type, Object value) {
        if (type == null) {
            throw new IllegalArgumentException("entity data type cannot be null");
        }
        value = normalizeValue(type, value);
        EntityData<?> existing = find(index);
        if (existing != null) {
            ((EntityData<Object>) existing).setType((EntityDataType) type);
            ((EntityData<Object>) existing).setValue(value);
            return this;
        }
        handle.add(new EntityData(index, type, value));
        return this;
    }

    public void setObject(int index, Object value, boolean update) { setObject(index, value); }

    public void setObject(int index, Serializer serializer, Object value) {
        setObject(index, serializer == null ? null : serializer.getEntityDataType(), value);
    }

    public void setObject(int index, Serializer serializer, Object value, boolean update) {
        setObject(index, serializer == null ? null : serializer.getEntityDataType(), value);
    }

    public void setObject(int index, WrappedWatchableObject value) { setObject(index, value, true); }

    public void setObject(int index, WrappedWatchableObject value, boolean update) {
        if (value == null || value.getEntityData() == null) return;
        removeObject(index);
        value.getEntityData().setIndex(index);
        handle.add(value.getEntityData());
    }

    /** ProtocolLib-compatible object/serializer overload. */
    public void setObject(WrappedDataWatcherObject object, Object value) {
        if (object == null) {
            throw new IllegalArgumentException("data watcher object cannot be null");
        }
        setObject(object, value, true);
    }

    public void setObject(WrappedDataWatcherObject object, Object value, boolean update) {
        if (object == null || object.getSerializer() == null) {
            throw new IllegalArgumentException("data watcher object/serializer cannot be null");
        }
        setObject(object.getIndex(), object.getSerializer().getEntityDataType(), value);
    }

    public void setObject(WrappedDataWatcherObject object, WrappedWatchableObject value) {
        setObject(object, value, true);
    }

    public void setObject(WrappedDataWatcherObject object, WrappedWatchableObject value, boolean update) {
        if (object == null) throw new IllegalArgumentException("data watcher object cannot be null");
        setObject(object.getIndex(), value, update);
    }

    public WrappedWatchableObject remove(int index) { return removeObject(index); }

    public Set<Integer> getIndexes() { return new LinkedHashSet<>(indexSet()); }
    public Set<Integer> indexSet() {
        Set<Integer> indexes = new LinkedHashSet<>();
        for (EntityData<?> data : handle) indexes.add(data.getIndex());
        return indexes;
    }
    public void clear() { handle.clear(); }

    public int size() {
        return handle.size();
    }

    /** Every entry, keyed by metadata index. */
    public Map<Integer, WrappedWatchableObject> asMap() {
        Map<Integer, WrappedWatchableObject> out = new LinkedHashMap<>();
        for (EntityData<?> data : handle) {
            out.put(data.getIndex(), new WrappedWatchableObject(data));
        }
        return out;
    }

    public List<WrappedWatchableObject> getWatchableObjects() {
        List<WrappedWatchableObject> out = new ArrayList<>(handle.size());
        for (EntityData<?> data : handle) {
            out.add(new WrappedWatchableObject(data));
        }
        return out;
    }

    public Byte getByte(int index) { return number(index, Byte.class); }
    public void setByte(int index, byte value, boolean update) { setObject(index, EntityDataTypes.BYTE, value); }
    public Short getShort(int index) { return number(index, Short.class); }
    public Integer getInteger(int index) { return number(index, Integer.class); }
    public void setInteger(int index, Integer value, boolean update) { setObject(index, EntityDataTypes.INT, value); }
    public Long getLong(int index) { return number(index, Long.class); }
    public void setLong(int index, Long value, boolean update) { setObject(index, EntityDataTypes.LONG, value); }
    public Float getFloat(int index) { return number(index, Float.class); }
    public void setFloat(int index, Float value, boolean update) { setObject(index, EntityDataTypes.FLOAT, value); }
    public String getString(int index) { return value(index, String.class); }
    public void setString(int index, String value, boolean update) { setObject(index, EntityDataTypes.STRING, value); }

    public WrappedChatComponent getChatComponent(int index) {
        Object value = getObject(index);
        return value instanceof WrappedChatComponent wrapped ? wrapped
                : value instanceof Component component ? WrappedChatComponent.fromComponent(component) : null;
    }
    public void setChatComponent(int index, WrappedChatComponent value, boolean update) {
        setObject(index, EntityDataTypes.ADV_COMPONENT, value == null ? null : value.getComponent());
    }
    public Optional<WrappedChatComponent> getOptionalChatComponent(int index) {
        Object value = getObject(index);
        if (!(value instanceof Optional<?> optional)) return Optional.empty();
        Object component = optional.orElse(null);
        WrappedChatComponent result = component instanceof WrappedChatComponent wrapped ? wrapped
                : component instanceof Component c ? WrappedChatComponent.fromComponent(c) : null;
        return Optional.ofNullable(result);
    }
    public void setOptionalChatComponent(int index, Optional<WrappedChatComponent> value, boolean update) {
        setObject(index, EntityDataTypes.OPTIONAL_ADV_COMPONENT,
                value == null ? Optional.empty() : value.map(WrappedChatComponent::getComponent));
    }

    public org.bukkit.inventory.ItemStack getItemStack(int index) {
        Object value = getObject(index);
        if (value instanceof org.bukkit.inventory.ItemStack item) return item;
        if (value instanceof ItemStack item) return SpigotConversionUtil.toBukkitItemStack(item);
        return null;
    }
    public void setItemStack(int index, org.bukkit.inventory.ItemStack value, boolean update) {
        setObject(index, EntityDataTypes.ITEMSTACK,
                value == null ? null : SpigotConversionUtil.fromBukkitItemStack(value));
    }
    public Boolean getBoolean(int index) { return value(index, Boolean.class); }
    public void setBoolean(int index, Boolean value, boolean update) { setObject(index, EntityDataTypes.BOOLEAN, value); }

    public BlockPosition getPosition(int index) {
        Object value = getObject(index);
        if (value instanceof BlockPosition position) return position;
        if (value instanceof com.github.retrooper.packetevents.util.Vector3i vector) {
            return BlockPosition.fromPacketEvents(vector);
        }
        return null;
    }
    public void setPosition(int index, BlockPosition value, boolean update) {
        setObject(index, EntityDataTypes.BLOCK_POSITION,
                value == null ? null : value.toPacketEvents());
    }
    public Optional<BlockPosition> getOptionalPosition(int index) {
        Object value = getObject(index);
        if (!(value instanceof Optional<?> optional)) return Optional.empty();
        Object position = optional.orElse(null);
        if (position instanceof com.github.retrooper.packetevents.util.Vector3i vector) {
            return Optional.of(BlockPosition.fromPacketEvents(vector));
        }
        return position instanceof BlockPosition block ? Optional.of(block) : Optional.empty();
    }
    public void setOptionalPosition(int index, Optional<BlockPosition> value, boolean update) {
        setObject(index, EntityDataTypes.OPTIONAL_BLOCK_POSITION,
                value == null ? Optional.empty() : value.map(BlockPosition::toPacketEvents));
    }

    public EnumWrappers.Direction getDirection(int index) {
        Object value = getObject(index);
        if (value instanceof EnumWrappers.Direction direction) return direction;
        return value instanceof BlockFace face ? enumByPath(EnumWrappers.Direction.class, face.name()) : null;
    }
    public void setDirection(int index, EnumWrappers.Direction value, boolean update) {
        setObject(index, EntityDataTypes.BLOCK_FACE, toPacketEventsFace(value));
    }
    public Optional<UUID> getOptionalUUID(int index) {
        Object value = getObject(index);
        return value instanceof Optional<?> optional && optional.orElse(null) instanceof UUID uuid
                ? Optional.of(uuid) : Optional.empty();
    }
    public void setOptionalUUID(int index, Optional<UUID> value, boolean update) {
        setObject(index, EntityDataTypes.OPTIONAL_UUID, value == null ? Optional.empty() : value);
    }

    public WrappedBlockData getBlockState(int index) {
        Object value = getObject(index);
        if (value instanceof WrappedBlockData block) return block;
        if (value instanceof Number number) {
            return getBlockStateValue(number);
        }
        return WrappedBlockData.fromHandle(value);
    }
    public void setBlockState(int index, WrappedBlockData value, boolean update) {
        setObject(index, EntityDataTypes.BLOCK_STATE, toPacketEventsBlockState(value));
    }
    public Optional<WrappedBlockData> getOptionalBlockState(int index) {
        Object value = getObject(index);
        if (!(value instanceof Optional<?> optional)) return Optional.empty();
        Object block = optional.orElse(null);
        if (block instanceof WrappedBlockData wrapped) return Optional.of(wrapped);
        if (block instanceof Number number) {
            WrappedBlockData converted = getBlockStateValue(number);
            return converted == null ? Optional.empty() : Optional.of(converted);
        }
        return Optional.empty();
    }
    public void setOptionalBlockState(int index, Optional<WrappedBlockData> value, boolean update) {
        setObject(index, EntityDataTypes.OPTIONAL_BLOCK_STATE,
                value == null ? Optional.empty() : value.map(WrappedDataWatcher::toPacketEventsBlockState));
    }

    public com.comphenix.protocol.wrappers.nbt.NbtCompound getNBTCompound(int index) {
        Object value = getObject(index);
        if (value instanceof com.comphenix.protocol.wrappers.nbt.NbtCompound compound) return compound;
        if (value instanceof NBTCompound compound) {
            var converted = fromPacketEventsNbt(compound, "");
            return converted instanceof com.comphenix.protocol.wrappers.nbt.NbtCompound
                    ? (com.comphenix.protocol.wrappers.nbt.NbtCompound) converted : null;
        }
        return null;
    }
    public void setNBTCompound(int index, com.comphenix.protocol.wrappers.nbt.NbtCompound value, boolean update) {
        setObject(index, EntityDataTypes.NBT, value == null ? null : toPacketEventsNbt(value));
    }
    public WrappedParticle<?> getParticle(int index) {
        Object value = getObject(index);
        if (value instanceof WrappedParticle<?> particle) return particle;
        if (value instanceof Particle<?> particle) {
            Enum<?> bukkit = (Enum<?>) SpigotConversionUtil.toBukkitParticle(particle.getType());
            if (bukkit instanceof org.bukkit.Particle type) {
                return WrappedParticle.create(type, particle.getData());
            }
        }
        return null;
    }
    public void setParticle(int index, WrappedParticle<?> value, boolean update) {
        setObject(index, EntityDataTypes.PARTICLE, toPacketEventsParticle(value));
    }
    public WrappedVillagerData getVillagerData(int index) {
        Object value = getObject(index);
        if (value instanceof WrappedVillagerData villager) return villager;
        if (!(value instanceof VillagerData data)) return null;
        String typeName = data.getType() == null || data.getType().getName() == null
                ? "PLAINS" : data.getType().getName().getKey();
        String professionName = data.getProfession() == null || data.getProfession().getName() == null
                ? "NONE" : data.getProfession().getName().getKey();
        return WrappedVillagerData.fromValues(enumByPath(WrappedVillagerData.Type.class, typeName),
                enumByPath(WrappedVillagerData.Profession.class, professionName), data.getLevel());
    }
    public void setVillagerData(int index, WrappedVillagerData value, boolean update) {
        setObject(index, EntityDataTypes.VILLAGER_DATA, toPacketEventsVillager(value));
    }
    public Optional<Integer> getOptionalInteger(int index) {
        Object value = getObject(index);
        return value instanceof Optional<?> optional && optional.orElse(null) instanceof Integer integer
                ? Optional.of(integer) : Optional.empty();
    }
    public void setOptionalInteger(int index, Optional<Integer> value, boolean update) {
        setObject(index, EntityDataTypes.OPTIONAL_INT, value == null ? Optional.empty() : value);
    }
    public EnumWrappers.EntityPose getPose(int index) {
        Object value = getObject(index);
        if (value instanceof EnumWrappers.EntityPose pose) return pose;
        return value instanceof EntityPose pose ? enumByPath(EnumWrappers.EntityPose.class, pose.name()) : null;
    }
    public void setPose(int index, EnumWrappers.EntityPose value, boolean update) {
        setObject(index, EntityDataTypes.ENTITY_POSE,
                value == null ? null : enumByPath(EntityPose.class, value.name()));
    }
    public Vector3F getVector3F(int index) {
        Object value = getObject(index);
        if (value instanceof Vector3F vector) return vector;
        if (value instanceof com.github.retrooper.packetevents.util.Vector3f vector) {
            return new Vector3F(vector.getX(), vector.getY(), vector.getZ());
        }
        return null;
    }
    public void setVector3F(int index, Vector3F value, boolean update) {
        setObject(index, EntityDataTypes.VECTOR3F,
                value == null ? null : new com.github.retrooper.packetevents.util.Vector3f(value.getX(), value.getY(), value.getZ()));
    }

    public Object getObject(WrappedDataWatcherObject object) {
        return object == null ? null : getObject(object.getIndex());
    }

    /** The live PacketEvents list. Mutating it mutates the packet. */
    public Object getHandle() {
        return handle;
    }

    /** PacketEvents-typed view used internally by converters. */
    public List<EntityData<?>> getEntityDataList() {
        return handle;
    }

    public WrappedDataWatcher deepClone() {
        List<EntityData<?>> copy = new ArrayList<>(handle.size());
        for (EntityData<?> data : handle) {
            copy.add(copyData(data));
        }
        WrappedDataWatcher result = new WrappedDataWatcher(copy);
        result.entity = entity;
        return result;
    }

    public Entity getEntity() { return entity; }
    public void setEntity(Entity entity) { this.entity = entity; }

    public List<WrappedDataValue> toDataValueCollection() {
        List<WrappedDataValue> result = new ArrayList<>(handle.size());
        for (EntityData<?> data : handle) result.add(new WrappedDataValue(data));
        return result;
    }

    public static Integer getTypeID(Class<?> type) {
        if (type == null) return null;
        List<Class<?>> classes = knownTypeClasses();
        int index = classes.indexOf(type);
        return index < 0 ? null : index;
    }

    public static Class<?> getTypeClass(int type) {
        List<Class<?>> classes = knownTypeClasses();
        return type < 0 || type >= classes.size() ? null : classes.get(type);
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof WrappedDataWatcher watcher)) return false;
        return asMap().equals(watcher.asMap());
    }

    @Override public int hashCode() { return asMap().hashCode(); }

    @Override
    public Iterator<WrappedWatchableObject> iterator() {
        return getWatchableObjects().iterator();
    }

    @Override
    public String toString() {
        return "WrappedDataWatcher" + asMap();
    }

    private EntityData<?> find(int index) {
        for (EntityData<?> data : handle) {
            if (data.getIndex() == index) {
                return data;
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static EntityData<?> copyData(EntityData<?> source) {
        return new EntityData(source.getIndex(), source.getType(), cloneValue(source.getValue()));
    }

    private static Object cloneValue(Object value) {
        if (value instanceof byte[] bytes) return bytes.clone();
        if (value instanceof int[] ints) return ints.clone();
        if (value instanceof long[] longs) return longs.clone();
        if (value instanceof WrappedChatComponent chat) return chat.deepClone();
        if (value instanceof WrappedBlockData block) return block.deepClone();
        return value;
    }

    private static BlockFace toPacketEventsFace(EnumWrappers.Direction value) {
        return value == null ? null : BlockFace.valueOf(value.name());
    }

    private static WrappedBlockData getBlockStateValue(Number value) {
        if (value == null) return null;
        WrappedBlockState state = WrappedBlockState.getByGlobalId(value.intValue());
        if (state == null) return null;
        BlockData data = SpigotConversionUtil.toBukkitBlockData(state);
        return data == null ? null : WrappedBlockData.createData(data.getMaterial());
    }

    private static Integer toPacketEventsBlockState(WrappedBlockData value) {
        if (value == null) {
            return null;
        }
        try {
            BlockData bukkitData = value.getType().createBlockData();
            WrappedBlockState state = SpigotConversionUtil.fromBukkitBlockData(bukkitData);
            if (state == null) {
                throw new IllegalStateException("PacketEvents returned no block-state mapping for " + value.getType());
            }
            return state.getGlobalId();
        } catch (RuntimeException error) {
            throw new UnsupportedOperationException("Cannot convert block state " + value
                    + " to PacketEvents global state ID", error);
        }
    }

    private static Particle<?> toPacketEventsParticle(WrappedParticle<?> value) {
        if (value == null) {
            return null;
        }
        Object handle = value.getHandle();
        if (handle instanceof Particle<?> particle) {
            return particle;
        }
        Enum<?> particleEnum = value.getParticle();
        if (particleEnum == null) {
            throw new UnsupportedOperationException("Particle is not a Bukkit enum: " + value.getParticle());
        }
        ParticleType<?> type = SpigotConversionUtil.fromBukkitParticle(particleEnum);
        if (type == null) {
            throw new UnsupportedOperationException("PacketEvents has no particle mapping for " + particleEnum);
        }
        Object data = value.getData();
        if (data == null) {
            return new Particle<>(type);
        }
        if (data instanceof ParticleData particleData) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Particle<?> result = new Particle(type, particleData);
            return result;
        }
        throw new UnsupportedOperationException("Bukkit particle data conversion is unavailable for "
                + data.getClass().getName() + "; supply a PacketEvents ParticleData handle");
    }

    private static VillagerData toPacketEventsVillager(WrappedVillagerData value) {
        if (value == null) {
            return null;
        }
        String typeName = value.getType() == null ? "plains" : value.getType().name().toLowerCase(Locale.ROOT);
        String professionName = value.getProfession() == null ? "none"
                : value.getProfession().name().toLowerCase(Locale.ROOT);
        var type = VillagerTypes.getByName(typeName);
        var profession = VillagerProfessions.getByName(professionName);
        if (type == null || profession == null) {
            throw new UnsupportedOperationException("PacketEvents has no villager mapping for "
                    + typeName + "/" + professionName);
        }
        return new VillagerData(type, profession, value.getLevel());
    }

    private static <E extends Enum<E>> E enumByPath(Class<E> type, String path) {
        if (path == null) {
            return null;
        }
        String normalized = path;
        int separator = normalized.lastIndexOf(':');
        if (separator >= 0) {
            normalized = normalized.substring(separator + 1);
        }
        try {
            return Enum.valueOf(type, normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /** Convert ProtocolLib's logical NBT tree into PacketEvents' typed NBT tree. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static NBT toPacketEventsNbt(com.comphenix.protocol.wrappers.nbt.NbtBase<?> value) {
        if (value == null) {
            return null;
        }
        Object raw = value.getValue();
        return switch (value.getType()) {
            case TAG_END -> new NBTEnd();
            case TAG_BYTE -> new NBTByte(((Number) raw).byteValue());
            case TAG_SHORT -> new NBTShort(((Number) raw).shortValue());
            case TAG_INT -> new NBTInt(((Number) raw).intValue());
            case TAG_LONG -> new NBTLong(((Number) raw).longValue());
            case TAG_FLOAT -> new NBTFloat(((Number) raw).floatValue());
            case TAG_DOUBLE -> new NBTDouble(((Number) raw).doubleValue());
            case TAG_STRING -> new NBTString(String.valueOf(raw));
            case TAG_BYTE_ARRAY -> new NBTByteArray(((byte[]) raw).clone());
            case TAG_INT_ARRAY -> new NBTIntArray(((int[]) raw).clone());
            case TAG_LONG_ARRAY -> new NBTLongArray(((long[]) raw).clone());
            case TAG_COMPOUND -> {
                NBTCompound compound = new NBTCompound();
                for (Map.Entry<String, com.comphenix.protocol.wrappers.nbt.NbtBase<?>> entry
                        : ((com.comphenix.protocol.wrappers.nbt.NbtCompound) value).getValue().entrySet()) {
                    NBT child = toPacketEventsNbt(entry.getValue());
                    if (child != null) compound.setTag(entry.getKey(), child);
                }
                yield compound;
            }
            case TAG_LIST -> {
                List<NBT> tags = new ArrayList<>();
                for (Object child : ((com.comphenix.protocol.wrappers.nbt.NbtList<?>) value).getValue()) {
                    NBT converted = toPacketEventsNbt((com.comphenix.protocol.wrappers.nbt.NbtBase<?>) child);
                    if (converted != null) tags.add(converted);
                }
                NBTType elementType = tags.isEmpty() ? NBTType.END : tags.get(0).getType();
                yield new NBTList(elementType, tags);
            }
        };
    }

    /** Convert PacketEvents' typed NBT tree back to ProtocolLib's logical wrapper tree. */
    private static com.comphenix.protocol.wrappers.nbt.NbtBase<?> fromPacketEventsNbt(NBT value, String name) {
        if (value == null || value instanceof NBTEnd) {
            return null;
        }
        if (value instanceof NBTByte tag) return com.comphenix.protocol.wrappers.nbt.NbtFactory.of(name, tag.getAsByte());
        if (value instanceof NBTShort tag) return com.comphenix.protocol.wrappers.nbt.NbtFactory.of(name, tag.getAsShort());
        if (value instanceof NBTInt tag) return com.comphenix.protocol.wrappers.nbt.NbtFactory.of(name, tag.getAsInt());
        if (value instanceof NBTLong tag) return com.comphenix.protocol.wrappers.nbt.NbtFactory.of(name, tag.getAsLong());
        if (value instanceof NBTFloat tag) return com.comphenix.protocol.wrappers.nbt.NbtFactory.of(name, tag.getAsFloat());
        if (value instanceof NBTDouble tag) return com.comphenix.protocol.wrappers.nbt.NbtFactory.of(name, tag.getAsDouble());
        if (value instanceof NBTString tag) return com.comphenix.protocol.wrappers.nbt.NbtFactory.of(name, tag.getValue());
        if (value instanceof NBTByteArray tag) return com.comphenix.protocol.wrappers.nbt.NbtFactory.of(name, tag.getValue());
        if (value instanceof NBTIntArray tag) return com.comphenix.protocol.wrappers.nbt.NbtFactory.of(name, tag.getValue());
        if (value instanceof NBTLongArray tag) return com.comphenix.protocol.wrappers.nbt.NbtFactory.of(name, tag.getValue());
        if (value instanceof NBTCompound tag) {
            com.comphenix.protocol.wrappers.nbt.NbtCompound compound =
                    com.comphenix.protocol.wrappers.nbt.NbtFactory.ofCompound(name);
            for (Map.Entry<String, NBT> entry : tag.getTags().entrySet()) {
                com.comphenix.protocol.wrappers.nbt.NbtBase<?> child =
                        fromPacketEventsNbt(entry.getValue(), entry.getKey());
                if (child != null) compound.put(entry.getKey(), child);
            }
            return compound;
        }
        if (value instanceof NBTList<?> tag) {
            List<Object> children = new ArrayList<>();
            for (NBT child : tag.getTags()) {
                com.comphenix.protocol.wrappers.nbt.NbtBase<?> converted = fromPacketEventsNbt(child, "");
                if (converted != null) children.add(converted);
            }
            return com.comphenix.protocol.wrappers.nbt.NbtFactory.ofList(name, children);
        }
        throw new UnsupportedOperationException("Unsupported PacketEvents NBT type: " + value.getClass().getName());
    }

    private static List<Class<?>> knownTypeClasses() {
        return List.of(Byte.class, Short.class, Integer.class, Long.class, Float.class,
                String.class, Component.class, Boolean.class, com.github.retrooper.packetevents.util.Vector3f.class,
                com.github.retrooper.packetevents.util.Vector3i.class, UUID.class);
    }

    private <T> T value(int index, Class<T> type) {
        Object value = getObject(index);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    @SuppressWarnings("unchecked")
    private <T extends Number> T number(int index, Class<T> type) {
        Object value = getObject(index);
        if (!(value instanceof Number number)) return null;
        if (type == Byte.class) return (T) Byte.valueOf(number.byteValue());
        if (type == Short.class) return (T) Short.valueOf(number.shortValue());
        if (type == Integer.class) return (T) Integer.valueOf(number.intValue());
        if (type == Long.class) return (T) Long.valueOf(number.longValue());
        if (type == Float.class) return (T) Float.valueOf(number.floatValue());
        return (T) number;
    }

    /**
     * Maps a Java value onto the PacketEvents serializer for it. Only the unambiguous
     * primitive-ish types are inferred - anything where several entity data types share a Java
     * class (components, optionals, item stacks, particles, ...) returns null so the caller is
     * forced to be explicit rather than silently picking the wrong serializer.
     */
    private static EntityDataType<?> inferType(Object value) {
        if (value instanceof Byte) return EntityDataTypes.BYTE;
        if (value instanceof Short) return EntityDataTypes.SHORT;
        if (value instanceof Integer) return EntityDataTypes.INT;
        if (value instanceof Long) return EntityDataTypes.LONG;
        if (value instanceof Float) return EntityDataTypes.FLOAT;
        if (value instanceof Boolean) return EntityDataTypes.BOOLEAN;
        if (value instanceof String) return EntityDataTypes.STRING;
        return null;
    }

    private static Object normalizeValue(EntityDataType<?> type, Object value) {
        if (value == null) return null;
        if (type == EntityDataTypes.ADV_COMPONENT && value instanceof WrappedChatComponent component) {
            return component.getComponent();
        }
        if (type == EntityDataTypes.OPTIONAL_ADV_COMPONENT && value instanceof Optional<?> optional) {
            return optional.map(entry -> entry instanceof WrappedChatComponent component
                    ? component.getComponent() : entry);
        }
        if (type == EntityDataTypes.ITEMSTACK && value instanceof org.bukkit.inventory.ItemStack item) {
            return SpigotConversionUtil.fromBukkitItemStack(item);
        }
        if (type == EntityDataTypes.BLOCK_POSITION && value instanceof BlockPosition position) {
            return position.toPacketEvents();
        }
        if (type == EntityDataTypes.OPTIONAL_BLOCK_POSITION && value instanceof Optional<?> optional) {
            return optional.map(entry -> entry instanceof BlockPosition position
                    ? position.toPacketEvents() : entry);
        }
        if (type == EntityDataTypes.BLOCK_FACE && value instanceof EnumWrappers.Direction direction) {
            return toPacketEventsFace(direction);
        }
        if (type == EntityDataTypes.BLOCK_STATE && value instanceof WrappedBlockData block) {
            return toPacketEventsBlockState(block);
        }
        if (type == EntityDataTypes.OPTIONAL_BLOCK_STATE && value instanceof Optional<?> optional) {
            return optional.map(entry -> entry instanceof WrappedBlockData block
                    ? toPacketEventsBlockState(block) : entry);
        }
        if (type == EntityDataTypes.NBT && value instanceof com.comphenix.protocol.wrappers.nbt.NbtBase<?> nbt) {
            return toPacketEventsNbt(nbt);
        }
        if (type == EntityDataTypes.PARTICLE && value instanceof WrappedParticle<?> particle) {
            return toPacketEventsParticle(particle);
        }
        if (type == EntityDataTypes.VILLAGER_DATA && value instanceof WrappedVillagerData villager) {
            return toPacketEventsVillager(villager);
        }
        if (type == EntityDataTypes.ENTITY_POSE && value instanceof EnumWrappers.EntityPose pose) {
            return enumByPath(EntityPose.class, pose.name());
        }
        return value;
    }

    public static EquivalentConverter<WrappedDataWatcher> getConverter() {
        return CONVERTER;
    }

    private static final EquivalentConverter<WrappedDataWatcher> CONVERTER = new EquivalentConverter<>() {
        @Override
        @SuppressWarnings("unchecked")
        public WrappedDataWatcher getSpecific(Object generic) {
            return fromHandle((List<EntityData<?>>) generic);
        }

        @Override
        public Object getGeneric(WrappedDataWatcher specific) {
            return specific == null ? null : specific.getEntityDataList();
        }

        @Override
        public Class<WrappedDataWatcher> getSpecificType() {
            return WrappedDataWatcher.class;
        }

        @Override
        public Class<?> getGenericType() {
            return List.class;
        }
    };

    /** PacketEvents serializer wrapper retained for ProtocolLib source/binary compatibility. */
    public static class Serializer {
        private final EntityDataType<?> handle;
        private final Class<?> type;
        private final Type genericType;
        private final boolean optional;

        public Serializer(EntityDataType<?> handle) {
            this(handle, inferJavaType(handle));
        }

        public Serializer(Object handle) {
            this(handle instanceof EntityDataType ? (EntityDataType<?>) handle : null);
        }

        public Serializer(Class<?> type, Object handle, boolean supported) {
            this(type, handle, supported, false);
        }

        public Serializer(Type type, Object handle, boolean supported) {
            this(type, handle, supported, false);
        }

        private Serializer(Type type, Object handle, boolean supported, boolean optional) {
            this.handle = handle instanceof EntityDataType ? (EntityDataType<?>) handle : null;
            this.genericType = type == null ? Object.class : type;
            this.type = type instanceof Class<?> clazz ? clazz : Object.class;
            this.optional = optional;
        }

        private Serializer(EntityDataType<?> handle, Class<?> type) {
            this.handle = handle;
            this.type = type;
            this.genericType = type == null ? Object.class : type;
            this.optional = false;
        }

        public Object getHandle() {
            return handle;
        }

        public Class<?> getType() {
            return type;
        }

        public Type getGenericType() {
            return genericType;
        }

        public boolean isOptional() {
            return optional;
        }

        public boolean isSupported() {
            return handle != null;
        }

        @Override public String toString() {
            return "Serializer[type=" + type + ", genericType=" + genericType + ", optional=" + optional + "]";
        }

        EntityDataType<?> getEntityDataType() {
            return handle;
        }
    }

    /** Registry facade for common metadata serializers. */
    public static final class Registry {
        public Registry() { }

        public static Serializer get(Class<?> type) {
            if (type == null) return null;
            if (type == Byte.class || type == byte.class) return new Serializer(EntityDataTypes.BYTE);
            if (type == Short.class || type == short.class) return new Serializer(EntityDataTypes.SHORT);
            if (type == Integer.class || type == int.class) return new Serializer(EntityDataTypes.INT);
            if (type == Long.class || type == long.class) return new Serializer(EntityDataTypes.LONG);
            if (type == Float.class || type == float.class) return new Serializer(EntityDataTypes.FLOAT);
            if (type == Boolean.class || type == boolean.class) return new Serializer(EntityDataTypes.BOOLEAN);
            if (type == String.class) return new Serializer(EntityDataTypes.STRING);
            if (type == Component.class) return new Serializer(EntityDataTypes.ADV_COMPONENT);
            if (type == ItemStack.class || type.getName().equals("org.bukkit.inventory.ItemStack")) {
                return new Serializer(EntityDataTypes.ITEMSTACK);
            }
            return null;
        }

        public static Serializer get(Type type) {
            return type instanceof Class<?> clazz ? get(clazz) : null;
        }

        public static Serializer get(Class<?> type, boolean optional) {
            Serializer base = get(type);
            if (base == null) return null;
            return new Serializer(type, base.getHandle(), base.isSupported(), optional);
        }

        public static Serializer get(Type type, boolean optional) {
            Serializer base = get(type);
            if (base == null) return null;
            return new Serializer(type, base.getHandle(), base.isSupported(), optional);
        }

        public static Serializer fromHandle(Object handle) {
            return handle instanceof EntityDataType ? new Serializer((EntityDataType<?>) handle) : null;
        }

        public static Serializer getChatComponentSerializer() {
            return new Serializer(EntityDataTypes.ADV_COMPONENT);
        }

        public static Serializer getChatComponentSerializer(boolean optional) {
            EntityDataType<?> type = optional ? EntityDataTypes.OPTIONAL_ADV_COMPONENT : EntityDataTypes.ADV_COMPONENT;
            return new Serializer(inferJavaType(type), type, optional);
        }

        public static Serializer getItemStackSerializer(boolean optional) {
            return new Serializer(optional ? EntityDataTypes.OPTIONAL_ITEMSTACK : EntityDataTypes.ITEMSTACK,
                    inferJavaType(optional ? EntityDataTypes.OPTIONAL_ITEMSTACK : EntityDataTypes.ITEMSTACK));
        }

        public static Serializer getBlockDataSerializer(boolean optional) {
            return new Serializer(optional ? EntityDataTypes.OPTIONAL_BLOCK_STATE : EntityDataTypes.BLOCK_STATE,
                    WrappedBlockData.class);
        }

        public static Serializer getVectorSerializer() {
            return new Serializer(EntityDataTypes.VECTOR3F);
        }

        public static Serializer getBlockPositionSerializer(boolean optional) {
            return new Serializer(optional ? EntityDataTypes.OPTIONAL_BLOCK_POSITION : EntityDataTypes.BLOCK_POSITION,
                    BlockPosition.class);
        }

        public static Serializer getDirectionSerializer() {
            return new Serializer(EntityDataTypes.BLOCK_FACE);
        }

        public static Serializer getUUIDSerializer(boolean optional) {
            return new Serializer(UUID.class, EntityDataTypes.OPTIONAL_UUID, optional);
        }

        public static Serializer getNBTCompoundSerializer() {
            return new Serializer(EntityDataTypes.NBT);
        }
    }

    /** Pair of an entity metadata index and its serializer. */
    public static class WrappedDataWatcherObject {
        private int index;
        private Serializer serializer;

        public WrappedDataWatcherObject(int index, Serializer serializer) {
            this.index = index;
            this.serializer = serializer;
        }

        public WrappedDataWatcherObject(Object handle) {
            if (handle instanceof WrappedDataWatcherObject object) {
                this.index = object.index;
                this.serializer = object.serializer;
            } else if (handle instanceof EntityData<?> data) {
                this.index = data.getIndex();
                this.serializer = new Serializer(data.getType());
            } else {
                throw new IllegalArgumentException("Unsupported data watcher object: " + handle);
            }
        }

        public int getIndex() {
            return index;
        }

        public Serializer getSerializer() {
            return serializer;
        }

        public Object getHandle() {
            return this;
        }

        public void checkSerializer() {
            if (serializer == null || !serializer.isSupported()) {
                throw new IllegalStateException("Data watcher serializer is unavailable");
            }
        }

        public Class<?> getHandleType() { return serializer == null ? Object.class : serializer.getType(); }

        @Override public String toString() { return "WrappedDataWatcherObject[index=" + index + ", serializer=" + serializer + "]"; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof WrappedDataWatcherObject object)) return false;
            return index == object.index && Objects.equals(serializer == null ? null : serializer.getHandle(),
                    object.serializer == null ? null : object.serializer.getHandle());
        }
        @Override public int hashCode() { return Objects.hash(index, serializer == null ? null : serializer.getHandle()); }
    }

    static class DummyWatcherObject extends WrappedDataWatcherObject {
        public DummyWatcherObject(int index) { super(index, null); }
        @Override public int getIndex(){return super.getIndex();}
        @Override public Serializer getSerializer(){return null;}
        @Override public Object getHandle(){return this;}
        public Class<?> getHandleType(){return getClass();}
        public void checkSerializer(){if(getSerializer()==null)throw new IllegalStateException("serializer is unavailable");}
        @Override public boolean equals(Object other){return other instanceof WrappedDataWatcherObject && getIndex()==((WrappedDataWatcherObject)other).getIndex();}
    }

    private static Class<?> inferJavaType(EntityDataType<?> type) {
        if (type == EntityDataTypes.BYTE) return Byte.class;
        if (type == EntityDataTypes.SHORT) return Short.class;
        if (type == EntityDataTypes.INT) return Integer.class;
        if (type == EntityDataTypes.LONG) return Long.class;
        if (type == EntityDataTypes.FLOAT) return Float.class;
        if (type == EntityDataTypes.BOOLEAN) return Boolean.class;
        if (type == EntityDataTypes.STRING || type == EntityDataTypes.COMPONENT) return String.class;
        if (type == EntityDataTypes.ADV_COMPONENT) return Component.class;
        return Object.class;
    }
}
