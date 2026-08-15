package com.comphenix.protocol.reflect;

/**
 * Compatibility exception used by plugins compiled against ProtocolLib.
 */
public class FieldAccessException extends RuntimeException {
    private static final long serialVersionUID = 1911011681494034617L;

    public FieldAccessException() {
        super();
    }

    public FieldAccessException(String message) {
        super(message);
    }

    public FieldAccessException(Throwable cause) {
        super(cause);
    }

    public FieldAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public static FieldAccessException fromFormat(String message, Object... params) {
        return new FieldAccessException(String.format(message, params));
    }

    @Override
    public String toString() {
        String message = getMessage();
        return "FieldAccessException" + (message != null ? ": " + message : "");
    }
}
