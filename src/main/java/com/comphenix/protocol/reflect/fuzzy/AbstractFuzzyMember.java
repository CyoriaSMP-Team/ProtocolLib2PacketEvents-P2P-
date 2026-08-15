package com.comphenix.protocol.reflect.fuzzy;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class AbstractFuzzyMember<T extends Member> implements AbstractFuzzyMatcher<T> {
    protected int modifiersRequired;
    protected int modifiersBanned;
    protected Pattern nameRegex;
    protected AbstractFuzzyMatcher<Class<?>> declaringMatcher = ClassTypeMatcher.MATCH_ALL;
    protected transient boolean sealed;
    protected AbstractFuzzyMember() { }
    protected AbstractFuzzyMember(AbstractFuzzyMember<T> other) { modifiersRequired=other.modifiersRequired; modifiersBanned=other.modifiersBanned; nameRegex=other.nameRegex; declaringMatcher=other.declaringMatcher; sealed=true; }
    protected void prepareBuild() { }
    public int getModifiersRequired() { return modifiersRequired; }
    public int getModifiersBanned() { return modifiersBanned; }
    public Pattern getNameRegex() { return nameRegex; }
    public AbstractFuzzyMatcher<Class<?>> getDeclaringMatcher() { return declaringMatcher; }
    @Override public boolean isMatch(T value,Object parent) { if(value==null)return false; int mods=value.getModifiers(); return (mods&modifiersRequired)==modifiersRequired && (mods&modifiersBanned)==0 && declaringMatcher.isMatch(value.getDeclaringClass(),value) && (nameRegex==null||nameRegex.matcher(value.getName()).matches()); }
    @Override public String toString() { return "FuzzyMember[required="+modifiersRequired+", banned="+modifiersBanned+", name="+nameRegex+"]"; }
    @Override public boolean equals(Object o) { if(this==o)return true; if(!(o instanceof AbstractFuzzyMember<?> other))return false; return modifiersRequired==other.modifiersRequired&&modifiersBanned==other.modifiersBanned&&Objects.equals(nameRegex==null?null:nameRegex.pattern(),other.nameRegex==null?null:other.nameRegex.pattern())&&Objects.equals(declaringMatcher,other.declaringMatcher); }
    @Override public int hashCode() { return Objects.hash(modifiersRequired,modifiersBanned,nameRegex==null?null:nameRegex.pattern(),declaringMatcher); }

    public static abstract class Builder<T extends AbstractFuzzyMember<?>> {
        protected T member = initialMember();
        protected abstract T initialMember();
        public Builder<T> requireModifier(int modifier){member.modifiersRequired|=modifier;return this;}
        public Builder<T> requirePublic(){return requireModifier(Modifier.PUBLIC);}
        public Builder<T> banModifier(int modifier){member.modifiersBanned|=modifier;return this;}
        public Builder<T> nameRegex(String regex){return nameRegex(Pattern.compile(regex));}
        public Builder<T> nameRegex(Pattern regex){member.nameRegex=regex;return this;}
        public Builder<T> nameExact(String name){return nameRegex(Pattern.quote(name));}
        public Builder<T> declaringClassExactType(Class<?> clazz){member.declaringMatcher=FuzzyMatchers.matchExact(clazz);return this;}
        public Builder<T> declaringClassSuperOf(Class<?> clazz){member.declaringMatcher=FuzzyMatchers.matchSuper(clazz);return this;}
        public Builder<T> declaringClassDerivedOf(Class<?> clazz){member.declaringMatcher=FuzzyMatchers.matchDerived(clazz);return this;}
        public Builder<T> declaringClassMatching(AbstractFuzzyMatcher<Class<?>> matcher){member.declaringMatcher=matcher;return this;}
        public abstract T build();
    }
}
