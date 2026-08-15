/* ProtocolLib2PacketEvents - clean-room attribute snapshot wrapper. */
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.events.PacketContainer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class WrappedAttribute {
    private final String attributeKey;
    private final double baseValue;
    private final Set<WrappedAttributeModifier> modifiers;
    private final PacketContainer packet;
    private WrappedAttribute(String key, double base, Collection<WrappedAttributeModifier> modifiers, PacketContainer packet) {
        this.attributeKey = key; this.baseValue = base; this.modifiers = new LinkedHashSet<>(modifiers); this.packet = packet;
    }
    public static WrappedAttribute fromHandle(Object handle) { return handle instanceof WrappedAttribute value ? value : null; }
    public static Builder newBuilder() { return new Builder(null); }
    public static Builder newBuilder(WrappedAttribute template) { return new Builder(template); }
    public String getAttributeKey() { return attributeKey; }
    public WrappedAttributeBase getBase() { return new WrappedAttributeBase(baseValue, false, attributeKey); }
    public double getBaseValue() { return baseValue; }
    public double getFinalValue() {
        double x = baseValue;
        double y = x;
        for (WrappedAttributeModifier modifier : modifiers) if (modifier.getOperation() == WrappedAttributeModifier.Operation.ADD_NUMBER) x += modifier.getAmount();
        y = x;
        for (WrappedAttributeModifier modifier : modifiers) if (modifier.getOperation() == WrappedAttributeModifier.Operation.MULTIPLY_PERCENTAGE) y += x * modifier.getAmount();
        for (WrappedAttributeModifier modifier : modifiers) if (modifier.getOperation() == WrappedAttributeModifier.Operation.ADD_PERCENTAGE) y *= 1 + modifier.getAmount();
        return y;
    }
    public PacketContainer getParentPacket() { return packet; }
    public boolean hasModifier(UUID id) { return getModifierByUUID(id) != null; }
    public WrappedAttributeModifier getModifierByUUID(UUID id) { for (var value : modifiers) if (Objects.equals(id, value.getUUID())) return value; return null; }
    public Set<WrappedAttributeModifier> getModifiers() { return Collections.unmodifiableSet(modifiers); }
    public WrappedAttribute withModifiers(Collection<WrappedAttributeModifier> values) { return new WrappedAttribute(attributeKey, baseValue, values, packet); }
    public WrappedAttribute shallowClone() { return withModifiers(modifiers); }
    @Override public boolean equals(Object other) { return other instanceof WrappedAttribute value && Objects.equals(attributeKey, value.attributeKey) && baseValue == value.baseValue && modifiers.equals(value.modifiers); }
    @Override public int hashCode() { return Objects.hash(attributeKey, baseValue, modifiers); }
    @Override public String toString() { return "WrappedAttribute[key=" + attributeKey + ", base=" + baseValue + ", final=" + getFinalValue() + ", modifiers=" + modifiers + "]"; }
    public static class WrappedAttributeBase {
        public double defaultValue; public boolean unknown; public String key;
        public WrappedAttributeBase() { this(0D, false, null); }
        public WrappedAttributeBase(double defaultValue, boolean unknown, String key) { this.defaultValue = defaultValue; this.unknown = unknown; this.key = key; }
    }
    public static class Builder {
        private String key = "generic.max_health"; private double base; private Set<WrappedAttributeModifier> modifiers = new LinkedHashSet<>(); private PacketContainer packet;
        private Builder(WrappedAttribute template) { if (template != null) { key = template.attributeKey; base = template.baseValue; modifiers.addAll(template.modifiers); packet = template.packet; } }
        public Builder baseValue(double value) { if (!Double.isFinite(value)) throw new IllegalArgumentException("baseValue must be finite"); base = value; return this; }
        public Builder attributeKey(String value) { key = Objects.requireNonNull(value, "attributeKey"); return this; }
        public Builder modifiers(Collection<WrappedAttributeModifier> value) { modifiers = new LinkedHashSet<>(value); return this; }
        public Builder addModifier(WrappedAttributeModifier value) { modifiers.add(value); return this; }
        public Builder packet(PacketContainer value) { packet = value; return this; }
        public WrappedAttribute build() { return new WrappedAttribute(key, base, modifiers, packet); }
    }
}
