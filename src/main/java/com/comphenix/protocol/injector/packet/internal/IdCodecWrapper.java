package com.comphenix.protocol.injector.packet.internal;

import com.comphenix.protocol.wrappers.AbstractWrapper;
import com.comphenix.protocol.wrappers.WrappedStreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IdCodecWrapper extends AbstractWrapper {
    public IdCodecWrapper(Object handle) { super(handle == null ? Object.class : handle.getClass()); if (handle != null) setHandle(handle); }
    public List<Entry> getById() { return Collections.emptyList(); }
    public class Entry extends AbstractWrapper {
        public Entry(Object handle) { super(handle == null ? Object.class : handle.getClass()); if (handle != null) setHandle(handle); }
        public Object type() { return handle; }
        public WrappedStreamCodec serializer() { return handle instanceof WrappedStreamCodec codec ? codec : null; }
    }
}
