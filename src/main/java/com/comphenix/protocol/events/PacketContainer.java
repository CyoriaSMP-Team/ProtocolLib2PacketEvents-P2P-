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
package com.comphenix.protocol.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.ObjectAllocator;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.PacketTypeData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

/**
 * ProtocolLib-style handle around a single packet, mirroring {@code PacketContainer}.
 * <p>
 * Internally this holds a PacketEvents {@link PacketWrapper}: decoded live from a
 * {@link PacketReceiveEvent}/{@link PacketSendEvent}, or, for packets created from scratch,
 * allocated via {@link ObjectAllocator} with its version/type bookkeeping fields populated
 * reflectively. A freshly created packet has all other fields at their zero value, so the
 * caller must set everything it needs before sending.
 * <p>
 * Typed accessors ({@link #getIntegers()}, {@link #getItemModifier()}, ...) are
 * {@link StructureModifier}s over the wrapper's declared fields, selected by type and ordered
 * by declaration - the same indexing model ProtocolLib uses over NMS packet fields. Note that
 * the field layout is PacketEvents' rather than NMS's, so <em>indices are not guaranteed to
 * match those of real ProtocolLib</em> for a given packet; see the README.
 * <p>
 * Not every packet type has a PacketEvents wrapper. Those still construct fine and expose
 * {@link #getRawBuffer()} for direct byte-level access, but the typed accessors will report
 * zero fields; check {@link #hasStructuredAccess()} first.
 */
public class PacketContainer {

    private final PacketType type;
    private final PacketWrapper<?> handle;

    public PacketContainer(PacketType type, PacketReceiveEvent event) {
        this.type = type;
        this.handle = decode(type, event, null);
    }

    public PacketContainer(PacketType type, PacketSendEvent event) {
        this.type = type;
        this.handle = decode(type, null, event);
    }

    public PacketContainer(PacketType type) {
        this.type = type;
        this.handle = allocate(type);
    }

    public PacketContainer(PacketType type, PacketWrapper<?> handle) {
        this.type = type;
        this.handle = handle;
    }

    public PacketType getType() {
        return type;
    }

    /** The underlying PacketEvents wrapper, or {@code null} for an unwrapped packet type. */
    public PacketWrapper<?> getHandle() {
        return handle;
    }

    /**
     * Whether PacketEvents provides a typed wrapper for this packet. When false, the typed
     * accessors are empty and {@link #getRawBuffer()} is the only way to reach the contents.
     */
    public boolean hasStructuredAccess() {
        return handle != null;
    }

    /**
     * The packet's raw network buffer (a Netty {@code ByteBuf}), for packet types PacketEvents
     * does not model. Returns {@code null} if the packet is not backed by a live buffer.
     */
    public Object getRawBuffer() {
        return handle == null ? null : handle.buffer;
    }

    // --- primitive modifiers ----------------------------------------------------------

    public StructureModifier<Object> getModifier() {
        return new StructureModifier<>(handle, Object.class);
    }

    public StructureModifier<Integer> getIntegers() {
        return new StructureModifier<>(handle, int.class);
    }

    public StructureModifier<Long> getLongs() {
        return new StructureModifier<>(handle, long.class);
    }

    public StructureModifier<Short> getShorts() {
        return new StructureModifier<>(handle, short.class);
    }

    public StructureModifier<Byte> getBytes() {
        return new StructureModifier<>(handle, byte.class);
    }

    public StructureModifier<Float> getFloats() {
        return new StructureModifier<>(handle, float.class);
    }

    public StructureModifier<Double> getDoubles() {
        return new StructureModifier<>(handle, double.class);
    }

    public StructureModifier<Boolean> getBooleans() {
        return new StructureModifier<>(handle, boolean.class);
    }

    public StructureModifier<String> getStrings() {
        return new StructureModifier<>(handle, String.class);
    }

    public StructureModifier<UUID> getUUIDs() {
        return new StructureModifier<>(handle, UUID.class);
    }

    public StructureModifier<byte[]> getByteArrays() {
        return new StructureModifier<>(handle, byte[].class);
    }

    public StructureModifier<int[]> getIntegerArrays() {
        return new StructureModifier<>(handle, int[].class);
    }

    /** Every {@code List} field on the packet, untyped. */
    public StructureModifier<List<?>> getLists() {
        return new StructureModifier<>(handle, List.class);
    }

    // --- converted modifiers ----------------------------------------------------------

    /** Item stack fields, as Bukkit item stacks. */
    public StructureModifier<org.bukkit.inventory.ItemStack> getItemModifier() {
        return convert(BukkitConverters.getItemStackConverter());
    }

    /** Item stack fields, as PacketEvents item stacks (no Bukkit conversion). */
    public StructureModifier<ItemStack> getPacketEventsItemModifier() {
        return new StructureModifier<>(handle, ItemStack.class);
    }

    /** Chat component fields. */
    public StructureModifier<WrappedChatComponent> getChatComponents() {
        return convert(WrappedChatComponent.getConverter());
    }

    /** Block position fields. */
    public StructureModifier<BlockPosition> getBlockPositionModifier() {
        return convert(BlockPosition.getConverter());
    }

    /** Player profile fields. */
    public StructureModifier<WrappedGameProfile> getGameProfiles() {
        return convert(WrappedGameProfile.getConverter());
    }

    /**
     * Entity metadata fields.
     * <p>
     * PacketEvents stores metadata as a plain {@code List}, so this modifier selects
     * <em>every</em> list field on the packet. On packets that carry both metadata and another
     * list, check the index rather than assuming index 0.
     */
    public StructureModifier<WrappedDataWatcher> getDataWatcherModifier() {
        return convert(WrappedDataWatcher.getConverter());
    }

    public StructureModifier<EnumWrappers.Hand> getHands() {
        return convert(EnumWrappers.getHandConverter());
    }

    public StructureModifier<EnumWrappers.ItemSlot> getItemSlots() {
        return convert(EnumWrappers.getItemSlotConverter());
    }

    public StructureModifier<EnumWrappers.Direction0> getDirections() {
        return convert(EnumWrappers.getDirectionConverter());
    }

    public StructureModifier<EnumWrappers.Difficulty0> getDifficulties() {
        return convert(EnumWrappers.getDifficultyConverter());
    }

    public StructureModifier<EnumWrappers.PlayerDigType> getPlayerDigTypes() {
        return convert(EnumWrappers.getPlayerDigTypeConverter());
    }

    /** A modifier over an arbitrary field type. */
    public <T> StructureModifier<T> getSpecificModifier(Class<T> type) {
        return new StructureModifier<>(handle, type);
    }

    /** A modifier driven by a caller-supplied converter. */
    public <T> StructureModifier<T> convert(EquivalentConverter<T> converter) {
        return new StructureModifier<>(handle, converter.getGenericType(), converter);
    }

    /** A deep-ish copy sharing no field references with this container's wrapper. */
    public PacketContainer shallowClone() {
        if (handle == null) {
            return new PacketContainer(type, (PacketWrapper<?>) null);
        }
        PacketContainer copy = new PacketContainer(type);
        StructureModifier<Object> source = getModifier();
        StructureModifier<Object> target = copy.getModifier();
        for (int i = 0; i < source.size() && i < target.size(); i++) {
            target.write(i, source.read(i));
        }
        return copy;
    }

    @Override
    public String toString() {
        return "PacketContainer[" + type + (handle == null ? ", raw" : "") + "]";
    }

    // --- construction -----------------------------------------------------------------

    /**
     * Decodes the packet using PacketEvents' wrapper for its type. Returns {@code null} when
     * no wrapper exists, leaving the container in raw-buffer mode rather than throwing - a
     * listener registered for such a packet should still fire and still be able to cancel it.
     */
    private static PacketWrapper<?> decode(PacketType type, PacketReceiveEvent receive, PacketSendEvent send) {
        Class<? extends PacketWrapper<?>> wrapperClass = wrapperClassOf(type);
        if (wrapperClass == null) {
            return null;
        }
        try {
            if (receive != null) {
                Constructor<? extends PacketWrapper<?>> ctor = wrapperClass.getConstructor(PacketReceiveEvent.class);
                return ctor.newInstance(receive);
            }
            Constructor<? extends PacketWrapper<?>> ctor = wrapperClass.getConstructor(PacketSendEvent.class);
            return ctor.newInstance(send);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to decode " + type + " via " + wrapperClass.getName(), e);
        }
    }

    private static PacketWrapper<?> allocate(PacketType type) {
        Class<? extends PacketWrapper<?>> wrapperClass = wrapperClassOf(type);
        if (wrapperClass == null) {
            throw new IllegalStateException("PacketEvents has no wrapper for " + type
                    + ", so it cannot be constructed from scratch. Build the packet bytes yourself and send "
                    + "them through PacketEvents directly.");
        }
        PacketWrapper<?> wrapper = ObjectAllocator.allocate(wrapperClass);
        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        ClientVersion clientVersion = serverVersion.toClientVersion();
        int id = type.toPacketEvents().getId(clientVersion);
        try {
            setField(wrapper, "clientVersion", clientVersion);
            setField(wrapper, "serverVersion", serverVersion);
            setField(wrapper, "packetTypeData", new PacketTypeData(type.toPacketEvents(), id));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to initialize an allocated wrapper for " + type, e);
        }
        return wrapper;
    }

    private static Class<? extends PacketWrapper<?>> wrapperClassOf(PacketType type) {
        return type.toPacketEvents() == null ? null : type.toPacketEvents().getWrapperClass();
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = PacketWrapper.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Converters that need Bukkit types. Kept nested so the Bukkit-facing conversion lives
     * next to the accessors that use it.
     */
    static final class BukkitConverters {

        private BukkitConverters() {
        }

        static EquivalentConverter<org.bukkit.inventory.ItemStack> getItemStackConverter() {
            return ITEM_STACK;
        }

        private static final EquivalentConverter<org.bukkit.inventory.ItemStack> ITEM_STACK =
                new EquivalentConverter<>() {
                    @Override
                    public org.bukkit.inventory.ItemStack getSpecific(Object generic) {
                        return generic == null ? null
                                : io.github.retrooper.packetevents.util.SpigotConversionUtil
                                        .toBukkitItemStack((ItemStack) generic);
                    }

                    @Override
                    public Object getGeneric(org.bukkit.inventory.ItemStack specific) {
                        return specific == null ? null
                                : io.github.retrooper.packetevents.util.SpigotConversionUtil
                                        .fromBukkitItemStack(specific);
                    }

                    @Override
                    public Class<org.bukkit.inventory.ItemStack> getSpecificType() {
                        return org.bukkit.inventory.ItemStack.class;
                    }

                    @Override
                    public Class<?> getGenericType() {
                        return ItemStack.class;
                    }
                };
    }
}
