package com.comphenix.protocol.reflect.fuzzy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class FuzzyMatchers {
    private FuzzyMatchers() { }
    public static AbstractFuzzyMatcher<Class<?>> matchArray(AbstractFuzzyMatcher<Class<?>> matcher){return (value,parent)->value!=null&&value.isArray()&&matcher.isMatch(value.getComponentType(),parent);}
    public static AbstractFuzzyMatcher<Class<?>> except(Class<?> clazz){return (value,parent)->value!=null&&!clazz.isAssignableFrom(value);}
    public static AbstractFuzzyMatcher<Class<?>> assignable(Class<?> clazz){return (value,parent)->value!=null&&clazz.isAssignableFrom(value);}
    @SafeVarargs public static AbstractFuzzyMatcher<Class<?>> and(AbstractFuzzyMatcher<Class<?>>... matchers){return (value,parent)->{for(AbstractFuzzyMatcher<Class<?>> matcher:matchers)if(!matcher.isMatch(value,parent))return false;return true;};}
    public static AbstractFuzzyMatcher<Class<?>> matchAll(){return (value,parent)->true;}
    public static AbstractFuzzyMatcher<Class<?>> matchExact(Class<?> clazz){return new ClassTypeMatcher(clazz,ClassTypeMatcher.MatchVariant.MATCH_EXACT);}
    public static AbstractFuzzyMatcher<Class<?>> matchAnyOf(Class<?>... classes){return matchAnyOf(new HashSet<>(Arrays.asList(classes)));}
    public static AbstractFuzzyMatcher<Class<?>> matchAnyOf(Set<Class<?>> classes){return new ClassSetMatcher(classes);}
    public static AbstractFuzzyMatcher<Class<?>> matchSuper(Class<?> clazz){return new ClassTypeMatcher(clazz,ClassTypeMatcher.MatchVariant.MATCH_SUPER);}
    public static AbstractFuzzyMatcher<Class<?>> matchDerived(Class<?> clazz){return new ClassTypeMatcher(clazz,ClassTypeMatcher.MatchVariant.MATCH_DERIVED);}
    public static AbstractFuzzyMatcher<Class<?>> matchRegex(Pattern regex){return new ClassRegexMatcher(regex);}
    public static AbstractFuzzyMatcher<Class<?>> matchRegex(String regex){return matchRegex(Pattern.compile(regex));}
    static boolean checkPattern(Pattern a,Pattern b){return a==null?b==null:b!=null&&a.pattern().equals(b.pattern());}
}
