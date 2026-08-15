package com.comphenix.protocol.reflect.fuzzy;

import java.util.regex.Pattern;
final class ClassRegexMatcher implements AbstractFuzzyMatcher<Class<?>> {
    private final Pattern regex; public ClassRegexMatcher(Pattern regex){this.regex=regex;}
    @Override public boolean isMatch(Class<?> value,Object parent){return value!=null&&regex!=null&&regex.matcher(value.getCanonicalName()==null?value.getName():value.getCanonicalName()).matches();}
    @Override public String toString(){return "{ type matches \""+regex+"\" }";}
}
