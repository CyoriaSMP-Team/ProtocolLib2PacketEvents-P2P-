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
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.Difficulty;
import com.github.retrooper.packetevents.protocol.world.Direction;

/**
 * ProtocolLib's enum mirrors, mapped onto the equivalent PacketEvents enums.
 * <p>
 * ProtocolLib declares its own copies of these because the NMS originals are obfuscated and
 * move between versions. Here the underlying enums are PacketEvents' stable ones, so each
 * mirror is a thin, order-independent translation: mapping is by <em>name</em>, never by
 * ordinal, so a reordering upstream cannot silently change meaning.
 * <p>
 * Every converter returns {@code null} for values with no counterpart rather than guessing.
 */
public final class EnumWrappers {

    private EnumWrappers() {
    }

    /** Which hand was used. Mirrors PacketEvents' {@link InteractionHand}. */
    public enum Hand {
        MAIN_HAND, OFF_HAND;

        public InteractionHand toPacketEvents() {
            return byName(InteractionHand.class, name());
        }

        public static Hand fromPacketEvents(InteractionHand hand) {
            return hand == null ? null : byName(Hand.class, hand.name());
        }
    }

    /** Equipment slot. Mirrors PacketEvents' {@link EquipmentSlot}. */
    public enum ItemSlot {
        MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD, BODY, SADDLE;

        /**
         * ProtocolLib and PacketEvents spell these differently, so the mapping is explicit
         * rather than name-based.
         */
        public EquipmentSlot toPacketEvents() {
            switch (this) {
                case MAINHAND: return EquipmentSlot.MAIN_HAND;
                case OFFHAND:  return EquipmentSlot.OFF_HAND;
                case FEET:     return EquipmentSlot.BOOTS;
                case LEGS:     return EquipmentSlot.LEGGINGS;
                case CHEST:    return EquipmentSlot.CHEST_PLATE;
                case HEAD:     return EquipmentSlot.HELMET;
                case BODY:     return EquipmentSlot.BODY;
                case SADDLE:   return EquipmentSlot.SADDLE;
                default:       return null;
            }
        }

        public static ItemSlot fromPacketEvents(EquipmentSlot slot) {
            if (slot == null) {
                return null;
            }
            switch (slot) {
                case MAIN_HAND:   return MAINHAND;
                case OFF_HAND:    return OFFHAND;
                case BOOTS:       return FEET;
                case LEGGINGS:    return LEGS;
                case CHEST_PLATE: return CHEST;
                case HELMET:      return HEAD;
                case BODY:        return BODY;
                case SADDLE:      return SADDLE;
                default:          return null;
            }
        }
    }

    /** World difficulty. Mirrors PacketEvents' {@link Difficulty}. */
    public enum Difficulty0 {
        PEACEFUL, EASY, NORMAL, HARD
    }

    /** Block face / direction. Mirrors PacketEvents' {@link Direction}. */
    public enum Direction0 {
        DOWN, UP, NORTH, SOUTH, WEST, EAST
    }

    /** Player digging action. Mirrors PacketEvents' {@link DiggingAction}. */
    public enum PlayerDigType {
        START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS, DROP_ITEM, RELEASE_USE_ITEM, SWAP_HELD_ITEMS;

        public DiggingAction toPacketEvents() {
            switch (this) {
                case START_DESTROY_BLOCK: return DiggingAction.START_DIGGING;
                case ABORT_DESTROY_BLOCK: return DiggingAction.CANCELLED_DIGGING;
                case STOP_DESTROY_BLOCK:  return DiggingAction.FINISHED_DIGGING;
                case DROP_ALL_ITEMS:      return DiggingAction.DROP_ITEM_STACK;
                case DROP_ITEM:           return DiggingAction.DROP_ITEM;
                case RELEASE_USE_ITEM:    return DiggingAction.RELEASE_USE_ITEM;
                case SWAP_HELD_ITEMS:     return DiggingAction.SWAP_ITEM_WITH_OFFHAND;
                default:                  return null;
            }
        }

        public static PlayerDigType fromPacketEvents(DiggingAction action) {
            if (action == null) {
                return null;
            }
            switch (action) {
                case START_DIGGING:          return START_DESTROY_BLOCK;
                case CANCELLED_DIGGING:      return ABORT_DESTROY_BLOCK;
                case FINISHED_DIGGING:       return STOP_DESTROY_BLOCK;
                case DROP_ITEM_STACK:        return DROP_ALL_ITEMS;
                case DROP_ITEM:              return DROP_ITEM;
                case RELEASE_USE_ITEM:       return RELEASE_USE_ITEM;
                case SWAP_ITEM_WITH_OFFHAND: return SWAP_HELD_ITEMS;
                default:                     return null;
            }
        }
    }

    // --- converters -------------------------------------------------------------------

    public static EquivalentConverter<Hand> getHandConverter() {
        return HAND;
    }

    public static EquivalentConverter<ItemSlot> getItemSlotConverter() {
        return ITEM_SLOT;
    }

    public static EquivalentConverter<Difficulty0> getDifficultyConverter() {
        return DIFFICULTY;
    }

    public static EquivalentConverter<Direction0> getDirectionConverter() {
        return DIRECTION;
    }

    public static EquivalentConverter<PlayerDigType> getPlayerDigTypeConverter() {
        return DIG_TYPE;
    }

    private static final EquivalentConverter<Hand> HAND = new EquivalentConverter<>() {
        @Override public Hand getSpecific(Object generic) { return Hand.fromPacketEvents((InteractionHand) generic); }
        @Override public Object getGeneric(Hand specific) { return specific == null ? null : specific.toPacketEvents(); }
        @Override public Class<Hand> getSpecificType() { return Hand.class; }
        @Override public Class<?> getGenericType() { return InteractionHand.class; }
    };

    private static final EquivalentConverter<ItemSlot> ITEM_SLOT = new EquivalentConverter<>() {
        @Override public ItemSlot getSpecific(Object generic) { return ItemSlot.fromPacketEvents((EquipmentSlot) generic); }
        @Override public Object getGeneric(ItemSlot specific) { return specific == null ? null : specific.toPacketEvents(); }
        @Override public Class<ItemSlot> getSpecificType() { return ItemSlot.class; }
        @Override public Class<?> getGenericType() { return EquipmentSlot.class; }
    };

    private static final EquivalentConverter<Difficulty0> DIFFICULTY = new EquivalentConverter<>() {
        @Override public Difficulty0 getSpecific(Object generic) {
            return generic == null ? null : byName(Difficulty0.class, ((Difficulty) generic).name());
        }
        @Override public Object getGeneric(Difficulty0 specific) {
            return specific == null ? null : byName(Difficulty.class, specific.name());
        }
        @Override public Class<Difficulty0> getSpecificType() { return Difficulty0.class; }
        @Override public Class<?> getGenericType() { return Difficulty.class; }
    };

    private static final EquivalentConverter<Direction0> DIRECTION = new EquivalentConverter<>() {
        @Override public Direction0 getSpecific(Object generic) {
            return generic == null ? null : byName(Direction0.class, ((Direction) generic).name());
        }
        @Override public Object getGeneric(Direction0 specific) {
            return specific == null ? null : byName(Direction.class, specific.name());
        }
        @Override public Class<Direction0> getSpecificType() { return Direction0.class; }
        @Override public Class<?> getGenericType() { return Direction.class; }
    };

    private static final EquivalentConverter<PlayerDigType> DIG_TYPE = new EquivalentConverter<>() {
        @Override public PlayerDigType getSpecific(Object generic) { return PlayerDigType.fromPacketEvents((DiggingAction) generic); }
        @Override public Object getGeneric(PlayerDigType specific) { return specific == null ? null : specific.toPacketEvents(); }
        @Override public Class<PlayerDigType> getSpecificType() { return PlayerDigType.class; }
        @Override public Class<?> getGenericType() { return DiggingAction.class; }
    };

    /** Name-based enum lookup that yields null instead of throwing on an unknown constant. */
    private static <E extends Enum<E>> E byName(Class<E> type, String name) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
