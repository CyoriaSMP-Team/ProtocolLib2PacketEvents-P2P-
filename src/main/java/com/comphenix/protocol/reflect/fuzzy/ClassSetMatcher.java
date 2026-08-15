package com.comphenix.protocol.reflect.fuzzy;

import java.util.Set;
final class ClassSetMatcher implements AbstractFuzzyMatcher<Class<?>> { private final Set<Class<?>> classes; public ClassSetMatcher(Set<Class<?>> classes){this.classes=classes;} @Override public boolean isMatch(Class<?> value,Object parent){return classes.contains(value);} @Override public String toString(){return "ClassSetMatcher"+classes;} }
