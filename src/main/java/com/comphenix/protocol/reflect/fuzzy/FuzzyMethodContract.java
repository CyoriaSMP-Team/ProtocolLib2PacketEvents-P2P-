package com.comphenix.protocol.reflect.fuzzy;

import com.comphenix.protocol.reflect.MethodInfo;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FuzzyMethodContract extends AbstractFuzzyMember<MethodInfo> {
    private AbstractFuzzyMatcher<Class<?>> returnMatcher=ClassTypeMatcher.MATCH_ALL;
    private List<ParameterClassMatcher> params=new ArrayList<>(), exceptions=new ArrayList<>();
    private Integer paramCount;
    private FuzzyMethodContract() { }
    private FuzzyMethodContract(FuzzyMethodContract other){super(other);returnMatcher=other.returnMatcher;params=ImmutableList.copyOf(other.params);exceptions=ImmutableList.copyOf(other.exceptions);paramCount=other.paramCount;}
    public static Builder newBuilder(){return new Builder();}
    public AbstractFuzzyMatcher<Class<?>> getReturnMatcher(){return returnMatcher;}
    public ImmutableList<ParameterClassMatcher> getParamMatchers(){return params instanceof ImmutableList?(ImmutableList<ParameterClassMatcher>)params:ImmutableList.copyOf(params);}
    public List<ParameterClassMatcher> getExceptionMatchers(){return exceptions;}
    public Integer getParamCount(){return paramCount;}
    @Override public boolean isMatch(MethodInfo value,Object parent){if(!super.isMatch(value,parent)||!returnMatcher.isMatch(value.getReturnType(),value))return false; if(paramCount!=null&&value.getParameterTypes().length!=paramCount)return false; return matches(value.getParameterTypes(),params,value)&&matches(value.getExceptionTypes(),exceptions,value);}
    private boolean matches(Class<?>[] types,List<ParameterClassMatcher> matchers,MethodInfo parent){for(ParameterClassMatcher matcher:matchers){boolean found=false;for(int i=0;i<types.length;i++)if(matcher.isParameterMatch(types[i],parent,i)){found=true;break;}if(!found)return false;}return true;}
    public static final class ParameterClassMatcher implements AbstractFuzzyMatcher<Class<?>[]> {
        private final AbstractFuzzyMatcher<Class<?>> matcher; private final Integer index;
        public ParameterClassMatcher(AbstractFuzzyMatcher<Class<?>> matcher){this(matcher,null);} public ParameterClassMatcher(AbstractFuzzyMatcher<Class<?>> matcher,Integer index){this.matcher=matcher;this.index=index;}
        public boolean isParameterMatch(Class<?> value,MethodInfo parent,int position){return (index==null||index==position)&&matcher.isMatch(value,parent);}
        @Override public boolean isMatch(Class<?>[] value,Object parent){throw new UnsupportedOperationException();}
        @Override public String toString(){return "ParameterClassMatcher["+matcher+","+index+"]";}
    }
    public static class Builder extends AbstractFuzzyMember.Builder<FuzzyMethodContract> {
        @Override protected FuzzyMethodContract initialMember(){return new FuzzyMethodContract();}
        @Override public Builder requireModifier(int v){super.requireModifier(v);return this;} @Override public Builder requirePublic(){super.requirePublic();return this;} @Override public Builder banModifier(int v){super.banModifier(v);return this;} @Override public Builder nameRegex(String v){super.nameRegex(v);return this;} @Override public Builder nameRegex(Pattern v){super.nameRegex(v);return this;} @Override public Builder nameExact(String v){super.nameExact(v);return this;} @Override public Builder declaringClassExactType(Class<?> v){super.declaringClassExactType(v);return this;} @Override public Builder declaringClassSuperOf(Class<?> v){super.declaringClassSuperOf(v);return this;} @Override public Builder declaringClassDerivedOf(Class<?> v){super.declaringClassDerivedOf(v);return this;} @Override public Builder declaringClassMatching(AbstractFuzzyMatcher<Class<?>> v){super.declaringClassMatching(v);return this;}
        public Builder parameterExactType(Class<?> type){return parameterMatches(FuzzyMatchers.matchExact(type));} public Builder parameterSuperOf(Class<?> type){return parameterMatches(FuzzyMatchers.matchSuper(type));} public Builder parameterDerivedOf(Class<?> type){return parameterMatches(FuzzyMatchers.matchDerived(type));} public Builder parameterMatches(AbstractFuzzyMatcher<Class<?>> matcher){member.params.add(new ParameterClassMatcher(matcher));return this;} public Builder parameterExactType(Class<?> type,int index){return parameterMatches(FuzzyMatchers.matchExact(type),index);} public Builder parameterSuperOf(Class<?> type,int index){return parameterMatches(FuzzyMatchers.matchSuper(type),index);} public Builder parameterDerivedOf(Class<?> type,int index){return parameterMatches(FuzzyMatchers.matchDerived(type),index);} public Builder parameterMatches(AbstractFuzzyMatcher<Class<?>> matcher,int index){member.params.add(new ParameterClassMatcher(matcher,index));return this;}
        public Builder parameterExactArray(Class<?>... types){for(int i=0;i<types.length;i++)parameterExactType(types[i],i);return this;} public Builder parameterCount(int count){member.paramCount=count;return this;}
        public Builder returnTypeVoid(){return returnTypeExact(void.class);} public Builder returnTypeExact(Class<?> type){member.returnMatcher=FuzzyMatchers.matchExact(type);return this;} public Builder returnDerivedOf(Class<?> type){member.returnMatcher=FuzzyMatchers.matchDerived(type);return this;} public Builder returnTypeMatches(AbstractFuzzyMatcher<Class<?>> matcher){member.returnMatcher=matcher;return this;}
        public Builder exceptionExactType(Class<?> type){return exceptionMatches(FuzzyMatchers.matchExact(type));} public Builder exceptionSuperOf(Class<?> type){return exceptionMatches(FuzzyMatchers.matchSuper(type));} public Builder exceptionMatches(AbstractFuzzyMatcher<Class<?>> matcher){member.exceptions.add(new ParameterClassMatcher(matcher));return this;} public Builder exceptionExactType(Class<?> type,int index){return exceptionMatches(FuzzyMatchers.matchExact(type),index);} public Builder exceptionSuperOf(Class<?> type,int index){return exceptionMatches(FuzzyMatchers.matchSuper(type),index);} public Builder exceptionMatches(AbstractFuzzyMatcher<Class<?>> matcher,int index){member.exceptions.add(new ParameterClassMatcher(matcher,index));return this;}
        @Override public FuzzyMethodContract build(){member.prepareBuild();return new FuzzyMethodContract(member);}
    }
}
