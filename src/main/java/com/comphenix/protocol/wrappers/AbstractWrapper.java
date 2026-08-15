package com.comphenix.protocol.wrappers;

/** Common handle/equality contract for ProtocolLib wrapper objects. */
public abstract class AbstractWrapper {
    protected Object handle;
    protected Class<?> handleType;
    public AbstractWrapper(Class<?> handleType) { if (handleType == null) throw new NullPointerException("handleType cannot be null"); this.handleType=handleType; }
    protected void setHandle(Object handle) { if (handle == null) throw new IllegalArgumentException("handle cannot be null"); if (!handleType.isAssignableFrom(handle.getClass())) throw new IllegalArgumentException("handle is not a "+handleType.getName()); this.handle=handle; }
    public Object getHandle(){return handle;}
    public Class<?> getHandleType(){return handleType;}
    @Override public boolean equals(Object other){return other instanceof AbstractWrapper && java.util.Objects.equals(handle,((AbstractWrapper)other).handle);}
    @Override public int hashCode(){return java.util.Objects.hashCode(handle);}
    @Override public String toString(){return getClass().getName()+"[handle="+handle+"]";}
}
