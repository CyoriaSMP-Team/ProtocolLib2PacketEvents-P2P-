package com.comphenix.protocol.reflect.cloning;

import java.util.Optional;
public class JavaOptionalCloner implements Cloner {
    private final Cloner wrapped; public JavaOptionalCloner(Cloner wrapped){this.wrapped=wrapped;}
    @Override public boolean canClone(Object source){return source instanceof Optional;}
    @Override public Object clone(Object source){Optional<?> value=(Optional<?>)source;return value.isEmpty()?Optional.empty():Optional.ofNullable(wrapped.clone(value.get()));}
    public Cloner getWrapped(){return wrapped;}
}
