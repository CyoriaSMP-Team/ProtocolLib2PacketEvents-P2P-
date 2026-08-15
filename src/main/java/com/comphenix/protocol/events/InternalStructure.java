package com.comphenix.protocol.events;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import java.util.Optional;

/** PacketLib's nested-structure view backed by the same logical handle. */
public class InternalStructure extends AbstractStructure {
    public InternalStructure(Object handle, StructureModifier<Object> structureModifier) { super(handle, structureModifier); }

    protected static final EquivalentConverter<InternalStructure> CONVERTER = new EquivalentConverter<>() {
        @Override public Object getGeneric(InternalStructure specific) { return specific == null ? null : specific.handle; }
        @Override public InternalStructure getSpecific(Object generic) { return generic == null ? null : new InternalStructure(generic, new StructureModifier<Object>(generic, Object.class)); }
        @Override public Class<InternalStructure> getSpecificType() { return InternalStructure.class; }
        @Override public Class<?> getGenericType() { return Object.class; }
    };

    public static EquivalentConverter<InternalStructure> getConverter() { return CONVERTER; }
    public StructureModifier<InternalStructure> getStructures() { return structureModifier.withType((Class) Object.class, CONVERTER); }
    public StructureModifier<Optional<InternalStructure>> getOptionalStructures() { return (StructureModifier) structureModifier.withType(Optional.class); }
    @Override public String toString() { return "InternalStructure[handle=" + handle + "]"; }
}
