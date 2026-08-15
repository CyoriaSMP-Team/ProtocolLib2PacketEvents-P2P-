package com.comphenix.protocol.scheduler;

import com.comphenix.protocol.reflect.accessors.MethodAccessor;

public class FoliaTask implements Task {
    private final MethodAccessor cancel;
    private final Object handle;
    public FoliaTask(MethodAccessor cancel, Object handle) { this.cancel=cancel;this.handle=handle; }
    public void cancel() { if (cancel != null) cancel.invoke(handle); }
}
