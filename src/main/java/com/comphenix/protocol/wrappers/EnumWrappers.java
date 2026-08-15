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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.Difficulty;
import com.github.retrooper.packetevents.protocol.world.Direction;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.EnumSet;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;

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
public abstract class EnumWrappers {

    public EnumWrappers() { }

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

    // Exact ProtocolLib names retained alongside the early P2P *0 aliases.
    public enum ClientCommand { PERFORM_RESPAWN, REQUEST_STATS, REQUEST_GAMERULE_VALUES, @Deprecated OPEN_INVENTORY_ACHIEVEMENT }
    public enum ChatVisibility { FULL, SYSTEM, HIDDEN }
    public enum Difficulty { PEACEFUL, EASY, NORMAL, HARD }
    public enum EntityUseAction { INTERACT, ATTACK, INTERACT_AT }
    public enum NativeGameMode {
        NOT_SET, SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR, @Deprecated NONE;
        public GameMode toBukkit(){switch(this){case SURVIVAL:return GameMode.SURVIVAL;case CREATIVE:return GameMode.CREATIVE;case ADVENTURE:return GameMode.ADVENTURE;case SPECTATOR:return GameMode.SPECTATOR;default:return null;}}
        public static NativeGameMode fromBukkit(GameMode mode){if(mode==null)return null;switch(mode){case SURVIVAL:return SURVIVAL;case CREATIVE:return CREATIVE;case ADVENTURE:return ADVENTURE;case SPECTATOR:return SPECTATOR;default:return null;}}
    }
    public enum ResourcePackStatus { SUCCESSFULLY_LOADED, DECLINED, FAILED_DOWNLOAD, ACCEPTED, DOWNLOADED, INVALID_URL, FAILED_RELOAD, DISCARDED }
    public enum PlayerInfoAction { ADD_PLAYER, INITIALIZE_CHAT, UPDATE_GAME_MODE, UPDATE_LISTED, UPDATE_LATENCY, UPDATE_DISPLAY_NAME, UPDATE_LIST_ORDER, UPDATE_HAT, @Deprecated REMOVE_PLAYER }
    public enum TitleAction { TITLE, SUBTITLE, ACTIONBAR, TIMES, CLEAR, RESET }
    public enum WorldBorderAction { SET_SIZE, LERP_SIZE, SET_CENTER, INITIALIZE, SET_WARNING_TIME, SET_WARNING_BLOCKS }
    public enum CombatEventType { ENTER_COMBAT, END_COMBAT, ENTITY_DIED }
    public enum PlayerAction implements AliasedEnum { @Deprecated START_SNEAKING("PRESS_SHIFT_KEY"), @Deprecated STOP_SNEAKING("RELEASE_SHIFT_KEY"), STOP_SLEEPING, START_SPRINTING, STOP_SPRINTING, START_RIDING_JUMP, STOP_RIDING_JUMP, OPEN_INVENTORY, START_FALL_FLYING; private final String[] aliases; PlayerAction(String... aliases){this.aliases=aliases;} public String[] getAliases(){return aliases;} }
    public enum ScoreboardAction { CHANGE, REMOVE }
    public enum Particle {
        EXPLOSION_NORMAL("explode",0,true), EXPLOSION_LARGE("largeexplode",1,true), EXPLOSION_HUGE("hugeexplosion",2,true), FIREWORKS_SPARK("fireworksSpark",3,false), WATER_BUBBLE("bubble",4,false), WATER_SPLASH("splash",5,false), WATER_WAKE("wake",6,false), SUSPENDED("suspended",7,false), SUSPENDED_DEPTH("depthsuspend",8,false), CRIT("crit",9,false), CRIT_MAGIC("magicCrit",10,false), SMOKE_NORMAL("smoke",11,false), SMOKE_LARGE("largesmoke",12,false), SPELL("spell",13,false), SPELL_INSTANT("instantSpell",14,false), SPELL_MOB("mobSpell",15,false), SPELL_MOB_AMBIENT("mobSpellAmbient",16,false), SPELL_WITCH("witchMagic",17,false), DRIP_WATER("dripWater",18,false), DRIP_LAVA("dripLava",19,false), VILLAGER_ANGRY("angryVillager",20,false), VILLAGER_HAPPY("happyVillager",21,false), TOWN_AURA("townaura",22,false), NOTE("note",23,false), PORTAL("portal",24,false), ENCHANTMENT_TABLE("enchantmenttable",25,false), FLAME("flame",26,false), LAVA("lava",27,false), FOOTSTEP("footstep",28,false), CLOUD("cloud",29,false), REDSTONE("reddust",30,false), SNOWBALL("snowballpoof",31,false), SNOW_SHOVEL("snowshovel",32,false), SLIME("slime",33,false), HEART("heart",34,false), BARRIER("barrier",35,false), ITEM_CRACK("iconcrack",36,false,2), BLOCK_CRACK("blockcrack",37,false,1), BLOCK_DUST("blockdust",38,false,1), WATER_DROP("droplet",39,false), ITEM_TAKE("take",40,false), MOB_APPEARANCE("mobappearance",41,true), DRAGON_BREATH("dragonbreath",42,false), END_ROD("endRod",43,false), DAMAGE_INDICATOR("damageIndicator",44,true), SWEEP_ATTACK("sweepAttack",45,true), FALLING_DUST("fallingdust",46,false,1), TOTEM("totem",47,false), SPIT("spit",48,true);
        private final String name; private final int id; private final boolean longDistance; private final int dataLength;
        Particle(String name,int id,boolean longDistance){this(name,id,longDistance,0);} Particle(String name,int id,boolean longDistance,int dataLength){this.name=name;this.id=id;this.longDistance=longDistance;this.dataLength=dataLength;}
        public String getName(){return name;} public int getId(){return id;} public boolean isLongDistance(){return longDistance;} public int getDataLength(){return dataLength;}
        public static Particle getByName(String name){if(name==null)return null;for(Particle p:values())if(p.name.equalsIgnoreCase(name))return p;return null;} public static Particle getById(int id){for(Particle p:values())if(p.id==id)return p;return null;}
    }
    public enum SoundCategory { MASTER("master"), MUSIC("music"), RECORDS("record"), WEATHER("weather"), BLOCKS("block"), HOSTILE("hostile"), NEUTRAL("neutral"), PLAYERS("player"), AMBIENT("ambient"), VOICE("voice"), UI("ui"); private final String key; SoundCategory(String key){this.key=key;} public String getKey(){return key;} public static SoundCategory getByKey(String key){if(key==null)return null;for(SoundCategory c:values())if(c.key.equalsIgnoreCase(key))return c;return null;} }
    public enum Direction { DOWN, UP, NORTH, SOUTH, WEST, EAST }
    public enum ChatType { CHAT, SYSTEM, GAME_INFO; public byte getId(){return (byte)ordinal();} }
    public enum EntityPose { STANDING, FALL_FLYING, SLEEPING, SWIMMING, SPIN_ATTACK, CROUCHING, LONG_JUMPING, DYING, CROAKING, USING_TONGUE, SITTING, ROARING, SNIFFING, EMERGING, DIGGING, SLIDING, SHOOTING, INHALING; public static EntityPose fromNms(Object value){return value==null?null:byName(EntityPose.class,value.toString());} public Object toNms(){return this;} }
    public enum Dimension { OVERWORLD(0), THE_NETHER(-1), THE_END(1); private final int id; Dimension(int id){this.id=id;} public int getId(){return id;} public static Dimension fromId(int id){for(Dimension d:values())if(d.id==id)return d;throw new IllegalArgumentException("Invalid dimension ID: "+id);} }
    public enum DisplaySlot { LIST, SIDEBAR, BELOW_NAME, TEAM_BLACK, TEAM_DARK_BLUE, TEAM_DARK_GREEN, TEAM_DARK_AQUA, TEAM_DARK_RED, TEAM_DARK_PURPLE, TEAM_GOLD, TEAM_GRAY, TEAM_DARK_GRAY, TEAM_BLUE, TEAM_GREEN, TEAM_AQUA, TEAM_RED, TEAM_LIGHT_PURPLE, TEAM_YELLOW, TEAM_WHITE }
    public enum RenderType { INTEGER, HEARTS }
    public enum ChatFormatting { BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE, OBFUSCATED, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET; public ChatColor toBukkit(){try{return ChatColor.valueOf(name());}catch(IllegalArgumentException e){return null;}} public static ChatFormatting fromBukkit(ChatColor color){return color==null?null:byName(ChatFormatting.class,color.name());} }
    public enum ClientIntent { PERFORM_RESPAWN, REQUEST_STATS, REQUEST_GAMERULE_VALUES, LOGIN, STATUS, TRANSFER }
    public enum TeamCollisionRule { ALWAYS, NEVER, PUSH_OTHER_TEAMS, PUSH_OWN_TEAM; public static TeamCollisionRule fromName(String name){return name==null?null:byName(TeamCollisionRule.class,name.toUpperCase(Locale.ROOT));} @Override public String toString(){return name().toLowerCase(Locale.ROOT);} }
    public enum TeamVisibility { ALWAYS, NEVER, HIDE_FOR_OTHER_TEAMS, HIDE_FOR_OWN_TEAM; public static TeamVisibility fromName(String name){return name==null?null:byName(TeamVisibility.class,name.toUpperCase(Locale.ROOT));} @Override public String toString(){return name().toLowerCase(Locale.ROOT);} }
    public enum HeightmapType { WORLD_SURFACE, OCEAN_FLOOR, MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES, OCEAN_FLOOR_WG, WORLD_SURFACE_WG }

    public interface AliasedEnum { String[] getAliases(); }
    public static class EnumConverter<T extends Enum<T>> implements EquivalentConverter<T> {
        private final Class<?> generic; private final Class<T> specific; public EnumConverter(Class<?> generic,Class<T> specific){this.generic=generic;this.specific=specific;}
        public T getSpecific(Object value){return value==null?null:byName(specific,value.toString());} public Object getGeneric(T value){return value;} public Class<T> getSpecificType(){return specific;} public Class<?> getGenericType(){return generic;}
    }
    public static class AliasedEnumConverter<T extends Enum<T> & AliasedEnum> extends EnumConverter<T> { public AliasedEnumConverter(Class<?> generic,Class<T> specific){super(generic,specific);} @Override public T getSpecific(Object value){if(value==null)return null;T direct=super.getSpecific(value);if(direct!=null)return direct;for(T e:values(getSpecificType()))for(String alias:e.getAliases())if(alias.equalsIgnoreCase(value.toString()))return e;return null;} @Override public Class<T> getSpecificType(){return super.getSpecificType();} @Override public Object getGeneric(T value){return super.getGeneric(value);} private static <T extends Enum<T>> T[] values(Class<T> type){return type.getEnumConstants();} }
    public static class FauxEnumConverter<T extends Enum<T>> extends EnumConverter<T> { public FauxEnumConverter(Class<T> specific,Class<?> generic){super(generic,specific);} @Override public T getSpecific(Object value){return super.getSpecific(value);} @Override public Class<T> getSpecificType(){return super.getSpecificType();} @Override public Object getGeneric(T value){return super.getGeneric(value);} }
    public static class IndexedEnumConverter<T extends Enum<T>> extends EnumConverter<T> { public IndexedEnumConverter(Class<T> specific,Class<?> generic){super(generic,specific);} @Override public T getSpecific(Object value){return super.getSpecific(value);} @Override public Class<T> getSpecificType(){return super.getSpecificType();} @Override public Object getGeneric(T value){return super.getGeneric(value);} }

    /** Player digging action. Mirrors PacketEvents' {@link DiggingAction}. */
    public enum PlayerDigType implements AliasedEnum {
        START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS, DROP_ITEM, RELEASE_USE_ITEM, SWAP_HELD_ITEMS, STAB("START_DESTROY_BLOCK");

        private final String[] aliases;
        PlayerDigType(String... aliases) { this.aliases = aliases; }
        public String[] getAliases() { return aliases.clone(); }

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

    public static EquivalentConverter<Difficulty> getDifficultyConverter() { return DIFFICULTY_EXACT; }
    public static EquivalentConverter<Difficulty0> getDifficulty0Converter() { return DIFFICULTY; }

    public static EquivalentConverter<Direction> getDirectionConverter() { return DIRECTION_EXACT; }
    public static EquivalentConverter<Direction0> getDirection0Converter() { return DIRECTION; }

    public static EquivalentConverter<PlayerDigType> getPlayerDigTypeConverter() {
        return DIG_TYPE;
    }
    public static EquivalentConverter<PlayerDigType> getPlayerDiggingActionConverter() { return DIG_TYPE; }
    public static EquivalentConverter<PlayerAction> getEntityActionConverter() { return new EnumConverter<>(Object.class, PlayerAction.class); }
    public static EquivalentConverter<ScoreboardAction> getUpdateScoreActionConverter() { return new EnumConverter<>(Object.class, ScoreboardAction.class); }
    public static EquivalentConverter<Particle> getParticleConverter() { return new EnumConverter<>(Object.class, Particle.class); }
    public static EquivalentConverter<SoundCategory> getSoundCategoryConverter() { return new EnumConverter<>(Object.class, SoundCategory.class); }
    public static EquivalentConverter<ChatType> getChatTypeConverter() { return new EnumConverter<>(Object.class, ChatType.class); }
    public static EquivalentConverter<DisplaySlot> getDisplaySlotConverter() { return new EnumConverter<>(Object.class, DisplaySlot.class); }
    public static EquivalentConverter<RenderType> getRenderTypeConverter() { return new EnumConverter<>(Object.class, RenderType.class); }
    public static EquivalentConverter<ChatFormatting> getChatFormattingConverter() { return new EnumConverter<>(Object.class, ChatFormatting.class); }
    public static EquivalentConverter<ClientIntent> getClientIntentConverter() { return new EnumConverter<>(Object.class, ClientIntent.class); }
    public static EquivalentConverter<TeamCollisionRule> getTeamCollisionRuleConverter() { return new EnumConverter<>(Object.class, TeamCollisionRule.class); }
    public static EquivalentConverter<TeamVisibility> getTeamVisibilityConverter() { return new EnumConverter<>(Object.class, TeamVisibility.class); }
    public static EquivalentConverter<HeightmapType> getHeightmapTypeConverter() { return new EnumConverter<>(Object.class, HeightmapType.class); }
    public static EquivalentConverter<EntityPose> getEntityPoseConverter() { return new EnumConverter<>(Object.class, EntityPose.class); }
    public static EquivalentConverter<PacketType.Protocol> getProtocolConverter() { return new EnumConverter<>(Object.class, PacketType.Protocol.class); }
    public static EquivalentConverter<ClientCommand> getClientCommandConverter() { return new EnumConverter<>(Object.class, ClientCommand.class); }
    public static EquivalentConverter<ChatVisibility> getChatVisibilityConverter() { return new EnumConverter<>(Object.class, ChatVisibility.class); }
    public static EquivalentConverter<EntityUseAction> getEntityUseActionConverter() { return new EnumConverter<>(Object.class, EntityUseAction.class); }
    public static EquivalentConverter<NativeGameMode> getGameModeConverter() { return new EnumConverter<>(Object.class, NativeGameMode.class); }
    public static EquivalentConverter<ResourcePackStatus> getResourcePackStatusConverter() { return new EnumConverter<>(Object.class, ResourcePackStatus.class); }
    public static EquivalentConverter<PlayerInfoAction> getPlayerInfoActionConverter() { return new EnumConverter<>(Object.class, PlayerInfoAction.class); }
    public static EquivalentConverter<TitleAction> getTitleActionConverter() { return new EnumConverter<>(Object.class, TitleAction.class); }
    public static EquivalentConverter<WorldBorderAction> getWorldBorderActionConverter() { return new EnumConverter<>(Object.class, WorldBorderAction.class); }
    public static EquivalentConverter<CombatEventType> getCombatEventTypeConverter() { return new EnumConverter<>(Object.class, CombatEventType.class); }
    public static EquivalentConverter<Difficulty> getExactDifficultyConverter() { return DIFFICULTY_EXACT; }
    public static <T extends Enum<T>> EquivalentConverter<T> getGenericConverter(Class<?> genericClass,Class<T> specificType){return new EnumConverter<>(genericClass,specificType);}
    @SuppressWarnings({"rawtypes", "unchecked"}) public static <E extends Enum<E>> EnumSet<E> createEmptyEnumSet(Class<?> enumClass){return EnumSet.noneOf((Class) enumClass);}

    public static Map<Class<?>,EquivalentConverter<?>> getFromNativeMap(){return java.util.Collections.emptyMap();}
    public static Map<Class<?>,EquivalentConverter<?>> getFromWrapperMap(){return java.util.Collections.emptyMap();}
    public static Class<?> getProtocolClass(){return Object.class;}
    public static Class<?> getClientCommandClass(){return ClientCommand.class;}
    public static Class<?> getChatVisibilityClass(){return ChatVisibility.class;}
    public static Class<?> getDifficultyClass(){return Difficulty.class;}
    public static Class<?> getEntityUseActionClass(){return EntityUseAction.class;}
    public static Class<?> getGameModeClass(){return NativeGameMode.class;}
    public static Class<?> getResourcePackStatusClass(){return ResourcePackStatus.class;}
    public static Class<?> getPlayerInfoActionClass(){return PlayerInfoAction.class;}
    public static Class<?> getTitleActionClass(){return TitleAction.class;}
    public static Class<?> getWorldBorderActionClass(){return WorldBorderAction.class;}
    public static Class<?> getCombatEventTypeClass(){return CombatEventType.class;}
    public static Class<?> getPlayerDigTypeClass(){return PlayerDigType.class;}
    public static Class<?> getPlayerActionClass(){return PlayerAction.class;}
    public static Class<?> getScoreboardActionClass(){return ScoreboardAction.class;}
    public static Class<?> getParticleClass(){return Particle.class;}
    public static Class<?> getSoundCategoryClass(){return SoundCategory.class;}
    public static Class<?> getItemSlotClass(){return ItemSlot.class;}
    public static Class<?> getHandClass(){return Hand.class;}
    public static Class<?> getDirectionClass(){return Direction.class;}
    public static Class<?> getChatTypeClass(){return ChatType.class;}
    public static Class<?> getEntityPoseClass(){return EntityPose.class;}
    public static Class<?> getDisplaySlotClass(){return DisplaySlot.class;}
    public static Class<?> getRenderTypeClass(){return RenderType.class;}
    public static Class<?> getChatFormattingClass(){return ChatFormatting.class;}
    public static Class<?> getClientIntentClass(){return ClientIntent.class;}
    public static Class<?> getTeamCollisionRuleClass(){return TeamCollisionRule.class;}
    public static Class<?> getTeamVisibilityClass(){return TeamVisibility.class;}
    public static Class<?> getHeightmapTypeClass(){return HeightmapType.class;}

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

    private static final EquivalentConverter<Difficulty> DIFFICULTY_EXACT = new EnumConverter<>(Object.class, Difficulty.class);
    private static final EquivalentConverter<Direction> DIRECTION_EXACT = new EnumConverter<>(Object.class, Direction.class);

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
