package com.comphenix.protocol.wrappers;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public final class ComponentConverter {
    private ComponentConverter() { }
    public static BaseComponent[] fromWrapper(WrappedChatComponent value){return value==null?null:TextComponent.fromLegacyText(value.getLegacyText());}
    public static WrappedChatComponent fromBaseComponent(BaseComponent... value){return value==null?null:WrappedChatComponent.fromLegacyText(BaseComponent.toLegacyText(value));}
    public static Class<?> getBaseComponentArrayClass(){return BaseComponent[].class;}
    public static BaseComponent[] clone(BaseComponent... value){return value==null?null:value.clone();}
}
