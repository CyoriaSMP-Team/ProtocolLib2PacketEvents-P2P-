package com.comphenix.protocol.wrappers.codecs;

import com.comphenix.protocol.wrappers.AbstractWrapper;
import java.util.Optional;
import java.util.NoSuchElementException;
import java.util.function.Function;

/** Result object for the logical codec facade, retaining success/error state. */
public class WrappedDataResult extends AbstractWrapper {
    private final Object result;
    private final String error;
    public WrappedDataResult(Object handle){this(handle,null,null);}
    private WrappedDataResult(Object handle,Object result,String error){super(Object.class);setHandle(handle==null?result:handle);this.result=result;this.error=error;}
    public static WrappedDataResult success(Object value){return new WrappedDataResult(value,value,null);}
    public static WrappedDataResult error(String message){return new WrappedDataResult(message,null,message);}
    public static WrappedDataResult fromHandle(Object handle){return new WrappedDataResult(handle);}
    public Optional<Object> getResult(){return Optional.ofNullable(result);}
    public Optional<String> getErrorMessage(){return Optional.ofNullable(error);}
    public Object getOrThrow(Function<String,Throwable> errorHandler){if(error!=null){Throwable t=errorHandler.apply(error);if(t instanceof RuntimeException r)throw r;throw new IllegalStateException(t);}if(result==null)throw new NoSuchElementException();return result;}
}
