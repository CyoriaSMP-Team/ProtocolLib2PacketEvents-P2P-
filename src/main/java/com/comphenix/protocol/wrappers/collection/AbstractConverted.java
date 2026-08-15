package com.comphenix.protocol.wrappers.collection;

import com.google.common.base.Function;

/** Base conversion hooks shared by the converted collection views. */
public abstract class AbstractConverted<VInner, VOuter> {
    private final Function<VOuter, VInner> innerConverter = this::toInner;
    private final Function<VInner, VOuter> outerConverter = this::toOuter;

    protected abstract VOuter toOuter(VInner inner);

    protected abstract VInner toInner(VOuter outer);

    protected Function<VOuter, VInner> getInnerConverter() {
        return innerConverter;
    }

    protected Function<VInner, VOuter> getOuterConverter() {
        return outerConverter;
    }
}
