package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Reflection-backed field-by-field wrapper builder for version adapters. */
public class AutoWrapper<T> implements EquivalentConverter<T> {
    private final Class<T> wrapperClass;
    private final Class<?> handleClass;
    private final Map<Integer,Function<Object,Object>> wrappers=new HashMap<>();
    private final Map<Integer,Function<Object,Object>> unwrappers=new HashMap<>();
    private AutoWrapper(Class<T> wrapperClass,Class<?> handleClass){this.wrapperClass=wrapperClass;this.handleClass=handleClass;}
    public static <T> AutoWrapper<T> wrap(Class<T> wrapperClass,Class<?> nmsClass){return new AutoWrapper<>(wrapperClass,nmsClass);}
    public static <T> AutoWrapper<T> wrap(Class<T> wrapperClass,String nmsClassName){try{return wrap(wrapperClass,Class.forName(nmsClassName));}catch(ClassNotFoundException e){throw new IllegalArgumentException("Missing handle class "+nmsClassName,e);}}
    public static <T> AutoWrapper<T> wrap(Class<T> wrapperClass,String nmsClassName,String... aliases){try{return wrap(wrapperClass,Class.forName(nmsClassName));}catch(ClassNotFoundException e){for(String alias:aliases)try{return wrap(wrapperClass,Class.forName(alias));}catch(ClassNotFoundException ignored){}throw new IllegalArgumentException("Missing handle class "+nmsClassName,e);}}
    public AutoWrapper<T> field(int index,Function<Object,Object> wrapper,Function<Object,Object> unwrapper){wrappers.put(index,wrapper);unwrappers.put(index,unwrapper);return this;}
    public AutoWrapper<T> field(int index,EquivalentConverter<?> converter){return field(index,value->((EquivalentConverter)converter).getSpecific(value),value->((EquivalentConverter)converter).getGeneric(value));}
    public T wrap(Object handle){if(handle==null)throw new NullPointerException("handle");try{T out=wrapperClass.getDeclaredConstructor().newInstance();Field[] source=instanceFields(handleClass);Field[] target=instanceFields(wrapperClass);if(source.length!=target.length)throw new InvalidWrapperException("Field count mismatch",null);for(int i=0;i<source.length;i++){Object value=source[i].get(handle);if(wrappers.containsKey(i))value=wrappers.get(i).apply(value);target[i].set(out,value);}return out;}catch(ReflectiveOperationException e){throw new InvalidWrapperException("Unable to wrap "+wrapperClass.getName(),e);}}
    public Object unwrap(Object wrapper){if(wrapper==null)throw new NullPointerException("wrapper");try{Constructor<?> constructor=handleClass.getDeclaredConstructors()[0];constructor.setAccessible(true);Object[] args=new Object[constructor.getParameterCount()];Object out=constructor.newInstance(args);Field[] source=instanceFields(wrapperClass);Field[] target=instanceFields(handleClass);if(source.length!=target.length)throw new InvalidWrapperException("Field count mismatch",null);for(int i=0;i<source.length;i++){Object value=source[i].get(wrapper);if(unwrappers.containsKey(i))value=unwrappers.get(i).apply(value);target[i].set(out,value);}return out;}catch(ReflectiveOperationException e){throw new InvalidWrapperException("Unable to unwrap "+wrapperClass.getName(),e);}}
    private static Field[] instanceFields(Class<?> type){return java.util.Arrays.stream(type.getDeclaredFields()).filter(f->!Modifier.isStatic(f.getModifiers())).peek(f->f.setAccessible(true)).toArray(Field[]::new);}
    public T getSpecific(Object generic){return wrap(generic);} public Object getGeneric(Object specific){return unwrap(specific);} public Class<T> getSpecificType(){return wrapperClass;} public Class<?> getGenericType(){return handleClass;}
    public static class InvalidWrapperException extends RuntimeException{private InvalidWrapperException(String message,Throwable cause){super(message,cause);}}
}
