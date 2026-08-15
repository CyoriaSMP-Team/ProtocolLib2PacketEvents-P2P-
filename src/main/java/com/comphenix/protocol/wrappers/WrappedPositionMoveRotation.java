/* ProtocolLib2PacketEvents - clean-room position/rotation wrapper. */
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import org.bukkit.util.Vector;

public class WrappedPositionMoveRotation {
    private Vector position;
    private Vector deltaMovement;
    private float yRot;
    private float xRot;
    private WrappedPositionMoveRotation(Vector position, Vector deltaMovement, float yRot, float xRot) {
        this.position = position == null ? new Vector() : position.clone();
        this.deltaMovement = deltaMovement == null ? new Vector() : deltaMovement.clone();
        this.yRot = yRot;
        this.xRot = xRot;
    }
    public static WrappedPositionMoveRotation fromHandle(Object handle) {
        return handle instanceof WrappedPositionMoveRotation value ? value :
                new WrappedPositionMoveRotation(new Vector(), new Vector(), 0F, 0F);
    }
    public static WrappedPositionMoveRotation create(Vector position, Vector deltaMovement, float yRot, float xRot) {
        return new WrappedPositionMoveRotation(position, deltaMovement, yRot, xRot);
    }
    public Vector getPosition() { return position.clone(); }
    public void setPosition(Vector position) { this.position = position.clone(); }
    public Vector getDeltaMovement() { return deltaMovement.clone(); }
    public void setDeltaMovement(Vector deltaMovement) { this.deltaMovement = deltaMovement.clone(); }
    public float getYRot() { return yRot; }
    public void setYRot(float yRot) { this.yRot = yRot; }
    public float getXRot() { return xRot; }
    public void setXRot(float xRot) { this.xRot = xRot; }
    public Object getHandle() { return this; }
    public static EquivalentConverter<WrappedPositionMoveRotation> getConverter() {
        return new EquivalentConverter<>() {
            @Override public WrappedPositionMoveRotation getSpecific(Object generic) { return fromHandle(generic); }
            @Override public Object getGeneric(WrappedPositionMoveRotation specific) { return specific; }
            @Override public Class<WrappedPositionMoveRotation> getSpecificType() { return WrappedPositionMoveRotation.class; }
            @Override public Class<?> getGenericType() { return WrappedPositionMoveRotation.class; }
        };
    }
}
