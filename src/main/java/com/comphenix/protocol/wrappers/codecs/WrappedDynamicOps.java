package com.comphenix.protocol.wrappers.codecs;

import com.comphenix.protocol.wrappers.AbstractWrapper;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;

/** Logical JSON/NBT dynamic-ops selector used by the clean-room codec facade. */
public class WrappedDynamicOps extends AbstractWrapper {
    public static final FieldAccessor[] JSON_ACCESSORS = new FieldAccessor[0];
    public static final FieldAccessor NBT_ACCESSOR = null;
    private final boolean json;
    private WrappedDynamicOps(Object handle, boolean json){super(Object.class);this.json=json;setHandle(handle==null?(json?"json":"nbt"):handle);}
    public static WrappedDynamicOps fromHandle(Object handle){return new WrappedDynamicOps(handle, false);}
    public static WrappedDynamicOps json(boolean compressed){return new WrappedDynamicOps(compressed?"json-compressed":"json",true);}
    public static WrappedDynamicOps nbt(){return new WrappedDynamicOps("nbt",false);}
    public boolean isJson(){return json;}
}
