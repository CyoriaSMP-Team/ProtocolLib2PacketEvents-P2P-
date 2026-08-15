package com.comphenix.protocol.injector;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.AbstractWrapper;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

/** Converts P2P wrapper values to their backend handles before packet construction. */
public class BukkitUnwrapper implements PacketConstructor.Unwrapper {
    public static final ReportType REPORT_ILLEGAL_ARGUMENT = new ReportType("Illegal argument while unwrapping %s");
    public static final ReportType REPORT_SECURITY_LIMITATION = new ReportType("Security limitation while unwrapping %s");
    public static final ReportType REPORT_CANNOT_FIND_UNWRAP_METHOD = new ReportType("Cannot find unwrap method for %s");
    public static final ReportType REPORT_CANNOT_READ_FIELD_HANDLE = new ReportType("Cannot read wrapper handle for %s");
    private static final BukkitUnwrapper INSTANCE = new BukkitUnwrapper();
    private final ErrorReporter reporter;
    public BukkitUnwrapper() { this(null); }
    public BukkitUnwrapper(ErrorReporter reporter) { this.reporter = reporter; }
    public static BukkitUnwrapper getInstance() { return INSTANCE; }
    @Override public Object unwrapItem(Object value) {
        if (value instanceof AbstractWrapper wrapper) return wrapper.getHandle();
        if (value instanceof WrappedGameProfile profile) return profile.getUserProfile();
        if (value instanceof PacketContainer packet) return packet.getHandle();
        return value;
    }
}
