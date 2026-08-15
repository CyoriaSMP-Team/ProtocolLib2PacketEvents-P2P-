package com.comphenix.protocol.reflect.instances;

import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import javax.annotation.Nullable;

public class BannedGenerator implements InstanceProvider {
    private final AbstractFuzzyMatcher<Class<?>> matcher;
    public BannedGenerator(AbstractFuzzyMatcher<Class<?>> matcher) { this.matcher=matcher; }
    public BannedGenerator(Class<?>... classes) { this(FuzzyMatchers.matchAnyOf(classes)); }
    @Override public Object create(@Nullable Class<?> type) { if(matcher != null && matcher.isMatch(type,null)) throw new NotConstructableException(); return null; }
}
