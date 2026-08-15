package com.comphenix.protocol.reflect.fuzzy;

@FunctionalInterface
public interface AbstractFuzzyMatcher<T> {
    boolean isMatch(T value, Object parent);
    default AbstractFuzzyMatcher<T> inverted() { return (value,parent) -> !isMatch(value,parent); }
    default AbstractFuzzyMatcher<T> and(AbstractFuzzyMatcher<T> other) { return (value,parent) -> isMatch(value,parent) && other.isMatch(value,parent); }
    default AbstractFuzzyMatcher<T> or(AbstractFuzzyMatcher<T> other) { return (value,parent) -> isMatch(value,parent) || other.isMatch(value,parent); }
}
