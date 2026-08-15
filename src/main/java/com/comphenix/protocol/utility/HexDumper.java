package com.comphenix.protocol.utility;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;

import java.io.IOException;

/** Deterministic byte dumper for packet diagnostics. */
public class HexDumper {
    private String lineDelimiter = System.lineSeparator();
    private int positionLength = 4;
    private String positionSuffix = ": ";
    private String delimiter = " ";
    private int groupLength = 1;
    private int groupCount = 16;

    public static HexDumper defaultDumper() { return new HexDumper(); }
    public HexDumper lineDelimiter(String value) { lineDelimiter = value; return this; }
    public HexDumper positionLength(int value) { positionLength = Math.max(0, value); return this; }
    public HexDumper positionSuffix(String value) { positionSuffix = value; return this; }
    public HexDumper delimiter(String value) { delimiter = value; return this; }
    public HexDumper groupLength(int value) { groupLength = Math.max(1, value); return this; }
    public HexDumper groupCount(int value) { groupCount = Math.max(1, value); return this; }

    public void appendTo(Appendable out, byte[] bytes) throws IOException { appendTo(out, bytes, 0, bytes.length); }

    public void appendTo(Appendable out, byte[] bytes, int offset, int length) throws IOException {
        if (bytes == null) return;
        int lineBytes = Math.max(1, groupLength * groupCount);
        for (int i = 0; i < length; i++) {
            if (i % lineBytes == 0) {
                if (i != 0) out.append(lineDelimiter);
                String pos = Integer.toHexString(offset + i);
                for (int p = pos.length(); p < positionLength; p++) out.append('0');
                out.append(pos).append(positionSuffix);
            } else if (i % groupLength == 0) {
                out.append(delimiter);
            }
            int value = bytes[offset + i] & 0xff;
            out.append(Character.forDigit(value >>> 4, 16));
            out.append(Character.forDigit(value & 0xf, 16));
        }
    }

    public void appendTo(StringBuilder out, byte[] bytes) { appendTo(out, bytes, 0, bytes.length); }
    public void appendTo(StringBuilder out, byte[] bytes, int offset, int length) {
        try { appendTo((Appendable) out, bytes, offset, length); } catch (IOException impossible) { throw new AssertionError(impossible); }
    }

    public int getLineLength(int bytes) {
        return positionLength + positionSuffix.length() + bytes * 2 + Math.max(0, bytes / groupLength - 1) * delimiter.length();
    }

    public static EquivalentConverter<Object> findConverter(Class<?> type) {
        return new EquivalentConverter<>() {
            public Object getSpecific(Object generic) { return generic; }
            public Object getGeneric(Object specific) { return specific; }
            public Class<Object> getSpecificType() { return (Class<Object>) type; }
            public Class<?> getGenericType() { return type; }
        };
    }

    public static String getPacketDescription(PacketContainer packet) throws IllegalAccessException {
        return packet == null ? "null" : packet.getType() + " " + packet.getHandle();
    }
}
