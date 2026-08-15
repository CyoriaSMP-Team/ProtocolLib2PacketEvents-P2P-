package com.comphenix.protocol.wrappers.codecs;

import com.comphenix.protocol.wrappers.AbstractWrapper;
import java.lang.reflect.Method;

/** Reflective codec facade with a logical pass-through fallback. */
public class WrappedCodec extends AbstractWrapper {
    private WrappedCodec(Object handle){super(Object.class);setHandle(handle==null?this:handle);}
    public static WrappedCodec fromHandle(Object handle){return new WrappedCodec(handle);}
    public WrappedDataResult encode(Object object,WrappedDynamicOps ops){return invoke("encode",object,ops==null?null:ops.getHandle());}
    public WrappedDataResult parse(Object value,WrappedDynamicOps ops){return invoke("parse",value,ops==null?null:ops.getHandle());}
    private WrappedDataResult invoke(String name,Object value,Object ops){if(handle!=this)for(Method method:handle.getClass().getMethods())if(method.getName().equals(name)&&method.getParameterCount()==2)try{return WrappedDataResult.success(method.invoke(handle,value,ops));}catch(ReflectiveOperationException e){return WrappedDataResult.error(e.getMessage());}return WrappedDataResult.success(value);}
}
