package com.comphenix.protocol.reflect.cloning;

public class NullableCloner implements Cloner {
    private final Cloner wrapped;
    public NullableCloner(Cloner wrapped){this.wrapped=wrapped;}
    @Override public boolean canClone(Object source){return source==null||wrapped.canClone(source);}
    @Override public Object clone(Object source){return source==null?null:wrapped.clone(source);}
    public Cloner getWrapped(){return wrapped;}
}
