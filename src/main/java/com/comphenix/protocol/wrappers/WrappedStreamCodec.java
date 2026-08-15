package com.comphenix.protocol.wrappers;

import java.lang.reflect.Method;

/** Version-neutral facade for the 1.20.5+ stream codec handle. */
public class WrappedStreamCodec extends AbstractWrapper {
    public WrappedStreamCodec(Object handle){super(handle == null ? Object.class : handle.getClass());setHandle(handle);}
    public Object decode(Object buffer){return invoke("decode",buffer);}
    public void encode(Object buffer,Object value){invoke("encode",buffer,value);}
    private Object invoke(String name,Object... args){for(Method m:handle.getClass().getMethods()){if(m.getName().equals(name)&&m.getParameterCount()==args.length){try{return m.invoke(handle,args);}catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot invoke codec "+name,e);}}}throw new UnsupportedOperationException("Codec has no "+name+" method");}
}
