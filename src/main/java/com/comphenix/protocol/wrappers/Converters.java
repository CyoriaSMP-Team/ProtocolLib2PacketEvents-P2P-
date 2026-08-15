package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/** Generic converter factories shared by wrapper and packet-constructor code. */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class Converters {
    public Converters() {}
    public static <T> EquivalentConverter<T> ignoreNull(EquivalentConverter<T> converter) {
        return new EquivalentConverter<>() { public T getSpecific(Object value){return value==null?null:converter.getSpecific(value);} public Object getGeneric(T value){return value==null?null:converter.getGeneric(value);} public Class<T> getSpecificType(){return converter.getSpecificType();} public Class<?> getGenericType(){return converter.getGenericType();} };
    }
    public static <T> EquivalentConverter<T> passthrough(Class<T> type) { return new EquivalentConverter<>() { public T getSpecific(Object value){return (T)value;} public Object getGeneric(T value){return value;} public Class<T> getSpecificType(){return type;} public Class<?> getGenericType(){return type;} }; }
    public static <T> EquivalentConverter<T> handle(Function<T,Object> toHandle,Function<Object,T> fromHandle,Class<T> type){return new EquivalentConverter<>(){public T getSpecific(Object v){return fromHandle.apply(v);}public Object getGeneric(T v){return toHandle.apply(v);}public Class<T> getSpecificType(){return type;}public Class<?> getGenericType(){return Object.class;}};}
    public static <T> EquivalentConverter<T[]> array(Class<?> genericItem,EquivalentConverter<T> converter){return new EquivalentConverter<>(){public T[] getSpecific(Object value){if(value==null)return null;int length=Array.getLength(value);T[] out=(T[])Array.newInstance(converter.getSpecificType(),length);for(int i=0;i<length;i++)out[i]=converter.getSpecific(Array.get(value,i));return out;}public Object getGeneric(T[] values){if(values==null)return null;Object out=Array.newInstance(genericItem,values.length);for(int i=0;i<values.length;i++)Array.set(out,i,converter.getGeneric(values[i]));return out;}public Class<T[]> getSpecificType(){return (Class<T[]>)Array.newInstance(converter.getSpecificType(),0).getClass();}public Class<?> getGenericType(){return Array.newInstance(genericItem,0).getClass();}};}
    public static <T> EquivalentConverter<Optional<T>> optional(EquivalentConverter<T> converter){return new EquivalentConverter<>(){public Optional<T> getSpecific(Object value){if(!(value instanceof Optional<?> optional))return Optional.empty();return optional.map(converter::getSpecific);}public Object getGeneric(Optional<T> value){return value==null?Optional.empty():value.map(converter::getGeneric);}public Class<Optional<T>> getSpecificType(){return (Class)Optional.class;}public Class<?> getGenericType(){return Optional.class;}};}
    public static <T,C extends Collection<T>> EquivalentConverter<C> collection(EquivalentConverter<T> element,Function<Collection<Object>,C> specificFactory,Function<C,Collection<?>> genericFactory){return ignoreNull(new EquivalentConverter<>(){public C getSpecific(Object value){if(!(value instanceof Collection<?> source))return null;C result=specificFactory.apply((Collection<Object>)source);for(Object item:source){T converted=element.getSpecific(item);if(converted!=null)result.add(converted);}return result;}public Object getGeneric(C value){Collection<Object> target=(Collection<Object>)genericFactory.apply(value);for(T item:value){Object converted=element.getGeneric(item);if(converted!=null)target.add(converted);}return target;}public Class<C> getSpecificType(){return (Class)Collection.class;}public Class<?> getGenericType(){return Collection.class;}});}
    public static <T> EquivalentConverter<Iterable<T>> iterable(EquivalentConverter<T> element,Supplier<List<T>> specificFactory,Supplier<List<?>> genericFactory){return ignoreNull(new EquivalentConverter<>(){public Iterable<T> getSpecific(Object value){if(!(value instanceof Iterable<?> source))return null;List<T> result=specificFactory.get();for(Object item:source){T converted=element.getSpecific(item);if(converted!=null)result.add(converted);}return result;}public Object getGeneric(Iterable<T> value){List<Object> result=(List<Object>)genericFactory.get();for(T item:value)result.add(element.getGeneric(item));return result;}public Class<Iterable<T>> getSpecificType(){return (Class)Iterable.class;}public Class<?> getGenericType(){return Iterable.class;}});}
    public static <T> List<T> toList(Iterable<? extends T> iterable){if(iterable instanceof List<?> list)return (List<T>)list;List<T> result=new ArrayList<>();for(T value:iterable)result.add(value);return result;}
    public static <T> EquivalentConverter<T> holder(EquivalentConverter<T> converter, WrappedRegistry registry){return converter;}
}
