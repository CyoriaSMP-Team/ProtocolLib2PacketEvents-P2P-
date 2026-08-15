/* ProtocolLib2PacketEvents - clean-room villager-data wrapper. */
package com.comphenix.protocol.wrappers;

public class WrappedVillagerData extends AbstractWrapper implements ClonableWrapper {
    public enum Type { DESERT, JUNGLE, PLAINS, SAVANNA, SNOW, SWAMP, TAIGA }
    public enum Profession { NONE, ARMORER, BUTCHER, CARTOGRAPHER, CLERIC, FARMER, FISHERMAN,
        FLETCHER, LEATHERWORKER, LIBRARIAN, MASON, NITWIT, SHEPHERD, TOOLSMITH, WEAPONSMITH }
    private Type type;
    private Profession profession;
    private int level;
    private WrappedVillagerData(Type type, Profession profession, int level) {
        super(WrappedVillagerData.class);
        this.type = type; this.profession = profession; this.level = level;
        this.handle = this;
    }
    public static WrappedVillagerData fromHandle(Object handle) {
        return handle instanceof WrappedVillagerData value ? value : fromValues(Type.PLAINS, Profession.NONE, 1);
    }
    public static WrappedVillagerData fromValues(Type type, Profession profession, int level) {
        return new WrappedVillagerData(type, profession, level);
    }
    public static Class<?> getNmsClass() { return WrappedVillagerData.class; }
    public int getLevel() { return level; }
    public Type getType() { return type; }
    public Profession getProfession() { return profession; }
    public WrappedVillagerData deepClone() { return fromValues(type, profession, level); }
    public Object getHandle() { return this; }
}
