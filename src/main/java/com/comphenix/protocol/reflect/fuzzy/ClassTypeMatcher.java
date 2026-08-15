package com.comphenix.protocol.reflect.fuzzy;

final class ClassTypeMatcher implements AbstractFuzzyMatcher<Class<?>> {
    public static final ClassTypeMatcher MATCH_ALL = new ClassTypeMatcher(null, MatchVariant.MATCH_SUPER);
    private final Class<?> matcher; private final MatchVariant variant;
    ClassTypeMatcher(Class<?> matcher, MatchVariant variant){this.matcher=matcher;this.variant=variant;}
    @Override public boolean isMatch(Class<?> input,Object parent){ if(input==null)return false; if(matcher==null)return variant!=MatchVariant.MATCH_EXACT; return switch(variant){case MATCH_EXACT->matcher.equals(input);case MATCH_DERIVED->matcher.isAssignableFrom(input);case MATCH_SUPER->input.isAssignableFrom(matcher);}; }
    public Class<?> getMatcher(){return matcher;} public MatchVariant getMatchVariant(){return variant;}
    @Override public String toString(){return "ClassTypeMatcher["+matcher+", "+variant+"]";}
    enum MatchVariant { MATCH_EXACT, MATCH_SUPER, MATCH_DERIVED }
}
