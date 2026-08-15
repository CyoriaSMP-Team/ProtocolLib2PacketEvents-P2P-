package com.comphenix.protocol.wrappers;

import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class AdventureComponentConverter {
    private static final GsonComponentSerializer SERIALIZER = GsonComponentSerializer.gson();
    public static Component fromWrapper(WrappedChatComponent value){return value==null?null:SERIALIZER.deserialize(value.getJson());}
    public static Object fromWrapperAsObject(WrappedChatComponent value){return fromWrapper(value);}
    public static Component fromJson(String json){return json==null?null:SERIALIZER.deserialize(json);}
    public static Object fromJsonAsObject(String json){return fromJson(json);}
    public static WrappedChatComponent fromComponent(Component value){return value==null?null:WrappedChatComponent.fromJson(SERIALIZER.serialize(value));}
    public static WrappedChatComponent fromComponentObject(Object value){return value instanceof Component c?fromComponent(c):null;}
    public static Style fromWrapper(WrappedComponentStyle value){
        return value == null || value.getJson() == null ? null
                : SERIALIZER.serializer().fromJson(value.getJson(), Style.class);
    }
    public static WrappedComponentStyle fromStyle(Style value){
        return value == null ? null : WrappedComponentStyle.fromJson(SERIALIZER.serializer().toJsonTree(value));
    }
    public static Class<?> getComponentClass(){return Component.class;}
    public static Component clone(Object value){return value instanceof Component c?c:null;}
}
