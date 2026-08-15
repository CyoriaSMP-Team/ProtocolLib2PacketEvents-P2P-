package com.comphenix.protocol.reflect.instances;

public class NotConstructableException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    public NotConstructableException() { super("This object should never be constructed."); }
    public NotConstructableException(String message) { super(message); }
    public NotConstructableException(String message, Throwable cause) { super(message, cause); }
    public NotConstructableException(Throwable cause) { super(cause); }
}
