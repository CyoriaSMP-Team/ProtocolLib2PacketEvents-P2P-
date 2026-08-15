package com.comphenix.protocol.wrappers;

import com.google.gson.JsonElement;

public class WrappedComponentStyle extends AbstractWrapper {
    private final JsonElement json;
    public WrappedComponentStyle(Object handle){super(handle==null?Object.class:handle.getClass());if(handle!=null)setHandle(handle);json=handle instanceof JsonElement?((JsonElement)handle):null;}
    private WrappedComponentStyle(JsonElement json){super(JsonElement.class);this.json=json;handle=json;}
    public JsonElement getJson(){return json;}
    public static WrappedComponentStyle fromJson(JsonElement json){return json==null?null:new WrappedComponentStyle(json);}
}
