/* ProtocolLib2PacketEvents - clean-room attribute-modifier wrapper. */
package com.comphenix.protocol.wrappers;

import java.util.Objects;
import java.util.UUID;

public class WrappedAttributeModifier {
    public enum Operation {
        ADD_NUMBER(0), MULTIPLY_PERCENTAGE(1), ADD_PERCENTAGE(2);
        private final int id;
        Operation(int id) { this.id = id; }
        public int getId() { return id; }
        public static Operation fromId(int id) {
            for (Operation value : values()) if (value.id == id) return value;
            throw new IllegalArgumentException("Unknown operation " + id);
        }
    }
    private final MinecraftKey key;
    private final UUID uuid;
    private final String name;
    private final Operation operation;
    private final double amount;
    private volatile boolean pendingSynchronization;
    private WrappedAttributeModifier(MinecraftKey key, UUID uuid, String name, double amount, Operation operation) {
        this.key = key; this.uuid = uuid; this.name = name; this.amount = amount; this.operation = operation;
    }
    public static Builder newBuilder() { return new Builder(null).uuid(UUID.randomUUID()); }
    public static Builder newBuilder(UUID id) { return new Builder(null).uuid(id); }
    public static Builder newBuilder(WrappedAttributeModifier template) { return new Builder(template); }
    public static WrappedAttributeModifier fromHandle(Object handle) { return handle instanceof WrappedAttributeModifier value ? value : null; }
    public MinecraftKey getKey() { return key; }
    public UUID getUUID() { return uuid; }
    public String getName() { return name; }
    public Operation getOperation() { return operation; }
    public double getAmount() { return amount; }
    public Object getHandle() { return this; }
    public void setPendingSynchronization(boolean value) { pendingSynchronization = value; }
    public boolean isPendingSynchronization() { return pendingSynchronization; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof WrappedAttributeModifier value)) return false;
        return key != null ? key.equals(value.key) : Objects.equals(uuid, value.uuid);
    }
    @Override public int hashCode() { return key != null ? key.hashCode() : Objects.hashCode(uuid); }
    @Override public String toString() { return "[key=" + key + ", amount=" + amount + ", operation=" + operation + "]"; }

    public static class Builder {
        private UUID uuid;
        private MinecraftKey key;
        private String name = "Unknown";
        private double amount;
        private Operation operation = Operation.ADD_NUMBER;
        private Builder(WrappedAttributeModifier template) {
            if (template != null) { uuid = template.uuid; key = template.key; name = template.name; amount = template.amount; operation = template.operation; }
        }
        public Builder uuid(UUID value) { uuid = Objects.requireNonNull(value, "uuid"); return this; }
        public Builder operation(Operation value) { operation = Objects.requireNonNull(value, "operation"); return this; }
        public Builder name(String value) { name = Objects.requireNonNull(value, "name"); return this; }
        public Builder key(MinecraftKey value) { key = Objects.requireNonNull(value, "key"); return this; }
        public Builder key(String prefix, String value) { return key(new MinecraftKey(prefix, value)); }
        public Builder amount(double value) { if (!Double.isFinite(value)) throw new IllegalArgumentException("amount must be finite"); amount = value; return this; }
        public WrappedAttributeModifier build() {
            if (uuid == null) throw new IllegalStateException("uuid is required");
            return new WrappedAttributeModifier(key, uuid, name, amount, operation);
        }
    }

    static class IndexedEnumConverter<T extends Enum<T>> implements com.comphenix.protocol.reflect.EquivalentConverter<T> {
        private final Class<T> type; private final Class<?> generic;
        IndexedEnumConverter(Class<T> type, Class<?> generic) { this.type=type;this.generic=generic; }
        public Object getGeneric(T value){return value==null?null:value.ordinal();}
        public T getSpecific(Object value){if(!(value instanceof Number n))return null;int i=n.intValue();T[] values=type.getEnumConstants();return i<0||i>=values.length?null:values[i];}
        public Class<T> getSpecificType(){return type;} public Class<?> getGenericType(){return generic;}
    }
}
