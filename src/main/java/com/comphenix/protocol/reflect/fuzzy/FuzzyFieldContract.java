package com.comphenix.protocol.reflect.fuzzy;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class FuzzyFieldContract extends AbstractFuzzyMember<Field> {
    private AbstractFuzzyMatcher<Class<?>> typeMatcher=ClassTypeMatcher.MATCH_ALL;
    private FuzzyFieldContract() { }
    private FuzzyFieldContract(FuzzyFieldContract other){super(other);typeMatcher=other.typeMatcher;}
    public static FuzzyFieldContract matchType(AbstractFuzzyMatcher<Class<?>> matcher){return newBuilder().typeMatches(matcher).build();}
    public static Builder newBuilder(){return new Builder();}
    public AbstractFuzzyMatcher<Class<?>> getTypeMatcher(){return typeMatcher;}
    @Override public boolean isMatch(Field value,Object parent){return super.isMatch(value,parent)&&typeMatcher.isMatch(value.getType(),value);}
    public static class Builder extends AbstractFuzzyMember.Builder<FuzzyFieldContract> {
        @Override protected FuzzyFieldContract initialMember(){return new FuzzyFieldContract();}
        @Override public Builder requireModifier(int v){super.requireModifier(v);return this;} @Override public Builder requirePublic(){super.requirePublic();return this;} @Override public Builder banModifier(int v){super.banModifier(v);return this;} @Override public Builder nameRegex(String v){super.nameRegex(v);return this;} @Override public Builder nameRegex(Pattern v){super.nameRegex(v);return this;} @Override public Builder nameExact(String v){super.nameExact(v);return this;} @Override public Builder declaringClassExactType(Class<?> v){super.declaringClassExactType(v);return this;} @Override public Builder declaringClassSuperOf(Class<?> v){super.declaringClassSuperOf(v);return this;} @Override public Builder declaringClassDerivedOf(Class<?> v){super.declaringClassDerivedOf(v);return this;} @Override public Builder declaringClassMatching(AbstractFuzzyMatcher<Class<?>> v){super.declaringClassMatching(v);return this;}
        public Builder typeExact(Class<?> type){member.typeMatcher=FuzzyMatchers.matchExact(type);return this;} public Builder typeSuperOf(Class<?> type){member.typeMatcher=FuzzyMatchers.matchSuper(type);return this;} public Builder typeDerivedOf(Class<?> type){member.typeMatcher=FuzzyMatchers.matchDerived(type);return this;} public Builder typeMatches(AbstractFuzzyMatcher<Class<?>> matcher){member.typeMatcher=matcher;return this;}
        @Override public FuzzyFieldContract build(){member.prepareBuild();return new FuzzyFieldContract(member);}
    }
}
