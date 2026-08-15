package com.comphenix.protocol.reflect.cloning;

import com.google.common.base.Optional;
public class GuavaOptionalCloner implements Cloner {
    private final Cloner wrapped; public GuavaOptionalCloner(Cloner wrapped){this.wrapped=wrapped;}
    @Override public boolean canClone(Object source){return source instanceof Optional;}
    @Override public Object clone(Object source){Optional<?> value=(Optional<?>)source;return value.isPresent()?Optional.fromNullable(wrapped.clone(value.get())):Optional.absent();}
    public Cloner getWrapped(){return wrapped;}
}
