package com.comphenix.protocol.reflect.fuzzy;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.MethodInfo;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public class FuzzyClassContract implements AbstractFuzzyMatcher<Class<?>> {
    private final ImmutableList<AbstractFuzzyMatcher<Field>> fields; private final ImmutableList<AbstractFuzzyMatcher<MethodInfo>> methods,constructors; private final ImmutableList<AbstractFuzzyMatcher<Class<?>>> bases,interfaces;
    private FuzzyClassContract(Builder b){fields=ImmutableList.copyOf(b.fields);methods=ImmutableList.copyOf(b.methods);constructors=ImmutableList.copyOf(b.constructors);bases=ImmutableList.copyOf(b.bases);interfaces=ImmutableList.copyOf(b.interfaces);}
    public static Builder newBuilder(){return new Builder();} public ImmutableList<AbstractFuzzyMatcher<Field>> getFieldContracts(){return fields;} public ImmutableList<AbstractFuzzyMatcher<MethodInfo>> getMethodContracts(){return methods;} public ImmutableList<AbstractFuzzyMatcher<MethodInfo>> getConstructorContracts(){return constructors;} public ImmutableList<AbstractFuzzyMatcher<Class<?>>> getBaseclassContracts(){return bases;} public ImmutableList<AbstractFuzzyMatcher<Class<?>>> getInterfaceContracts(){return interfaces;}
    @Override public boolean isMatch(Class<?> value,Object parent){if(value==null)return false;FuzzyReflection reflection=FuzzyReflection.fromClass(value,true);return match(value.getSuperclass(),bases,parent)&&match(value.getInterfaces(),interfaces,value)&&match(reflection.getFields(),fields,value)&&match(MethodInfo.fromMethods(reflection.getMethods()),methods,value)&&match(MethodInfo.fromConstructors(value.getDeclaredConstructors()),constructors,value);}
    private <T> boolean match(T value,List<AbstractFuzzyMatcher<T>> list,Object parent){return list.isEmpty()||list.stream().allMatch(m->m.isMatch(value,parent));}
    private <T> boolean match(Collection<T> values,List<AbstractFuzzyMatcher<T>> list,Object parent){if(list.isEmpty())return true;for(AbstractFuzzyMatcher<T> matcher:list){boolean found=false;for(T value:values)if(matcher.isMatch(value,parent)){found=true;break;}if(!found)return false;}return true;}
    private <T> boolean match(T[] values,List<AbstractFuzzyMatcher<T>> list,Object parent){if(list.isEmpty())return true;for(AbstractFuzzyMatcher<T> matcher:list){boolean found=false;for(T value:values)if(matcher.isMatch(value,parent)){found=true;break;}if(!found)return false;}return true;}
    @Override public String toString(){return "FuzzyClassContract[fields="+fields.size()+", methods="+methods.size()+", constructors="+constructors.size()+"]";}
    public static final class Builder { private final List<AbstractFuzzyMatcher<Field>> fields=new ArrayList<>(); private final List<AbstractFuzzyMatcher<MethodInfo>> methods=new ArrayList<>(),constructors=new ArrayList<>(); private final List<AbstractFuzzyMatcher<Class<?>>> bases=new ArrayList<>(),interfaces=new ArrayList<>(); public Builder(){} public Builder field(AbstractFuzzyMatcher<Field> m){fields.add(m);return this;} public Builder field(FuzzyFieldContract.Builder b){return field(b.build());} public Builder method(AbstractFuzzyMatcher<MethodInfo> m){methods.add(m);return this;} public Builder method(FuzzyMethodContract.Builder b){return method(b.build());} public Builder constructor(AbstractFuzzyMatcher<MethodInfo> m){constructors.add(m);return this;} public Builder constructor(FuzzyMethodContract.Builder b){return constructor(b.build());} public Builder baseclass(AbstractFuzzyMatcher<Class<?>> m){bases.add(m);return this;} public Builder baseclass(Builder b){return baseclass(b.build());} public Builder interfaces(AbstractFuzzyMatcher<Class<?>> m){interfaces.add(m);return this;} public Builder interfaces(Builder b){return interfaces(b.build());} public FuzzyClassContract build(){return new FuzzyClassContract(this);} }
}
