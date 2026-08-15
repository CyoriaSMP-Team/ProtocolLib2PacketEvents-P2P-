package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import org.bukkit.util.Vector;

public class WrappedEnumEntityUseAction extends AbstractWrapper implements ClonableWrapper {
    public static final EquivalentConverter<WrappedEnumEntityUseAction> CONVERTER=new EquivalentConverter<>(){public WrappedEnumEntityUseAction getSpecific(Object g){return fromHandle(g);}public Object getGeneric(WrappedEnumEntityUseAction s){return s==null?null:s.getHandle();}public Class<WrappedEnumEntityUseAction> getSpecificType(){return WrappedEnumEntityUseAction.class;}public Class<?> getGenericType(){return WrappedEnumEntityUseAction.class;}};
    private EnumWrappers.EntityUseAction action; private EnumWrappers.Hand hand; private Vector position;
    private WrappedEnumEntityUseAction(EnumWrappers.EntityUseAction action,EnumWrappers.Hand hand,Vector position){super(WrappedEnumEntityUseAction.class);this.action=action;this.hand=hand;this.position=position;handle=this;}
    public WrappedEnumEntityUseAction(Object handle){super(handle==null?Object.class:handle.getClass());if(handle!=null)setHandle(handle);this.action=handle instanceof WrappedEnumEntityUseAction v?v.action:EnumWrappers.EntityUseAction.ATTACK;}
    public static WrappedEnumEntityUseAction fromHandle(Object handle){return handle instanceof WrappedEnumEntityUseAction v?v:new WrappedEnumEntityUseAction(handle);}
    public static WrappedEnumEntityUseAction attack(){return new WrappedEnumEntityUseAction(EnumWrappers.EntityUseAction.ATTACK,null,null);} public static WrappedEnumEntityUseAction interact(EnumWrappers.Hand hand){return new WrappedEnumEntityUseAction(EnumWrappers.EntityUseAction.INTERACT,hand,null);} public static WrappedEnumEntityUseAction interactAt(EnumWrappers.Hand hand,Vector pos){return new WrappedEnumEntityUseAction(EnumWrappers.EntityUseAction.INTERACT_AT,hand,pos);}
    public EnumWrappers.EntityUseAction getAction(){return action;} public EnumWrappers.Hand getHand(){return hand;} public void setHand(EnumWrappers.Hand v){hand=v;} public Vector getPosition(){return position;} public void setPosition(Vector v){position=v;} public WrappedEnumEntityUseAction deepClone(){return new WrappedEnumEntityUseAction(action,hand,position==null?null:position.clone());}
    public FieldAccessor getPositionAccessor(){
        if (action != EnumWrappers.EntityUseAction.INTERACT_AT) {
            throw new IllegalArgumentException("Position is only available for INTERACT_AT");
        }
        if (handle == this) {
            return new FieldAccessor() {
                @Override public Object get(Object instance) { return position == null ? null : position.clone(); }
                @Override public void set(Object instance, Object value) { position = value instanceof Vector v ? v.clone() : null; }
                @Override public java.lang.reflect.Field getField() { return null; }
            };
        }
        for (Class<?> type = handle.getClass(); type != null; type = type.getSuperclass()) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (Vector.class.isAssignableFrom(field.getType())
                        || field.getType().getSimpleName().toLowerCase(java.util.Locale.ROOT).contains("vec3")) {
                    try { field.setAccessible(true); return com.comphenix.protocol.reflect.accessors.Accessors.getFieldAccessor(field); }
                    catch (RuntimeException ignored) { }
                }
            }
        }
        throw new IllegalArgumentException("Unable to locate the INTERACT_AT position field on " + handle.getClass());
    }
    public ClonableWrapper deepCloneAsInterface(){return deepClone();}
}
