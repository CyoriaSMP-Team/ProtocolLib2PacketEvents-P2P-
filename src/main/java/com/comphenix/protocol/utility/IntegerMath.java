package com.comphenix.protocol.utility;

/** Small integer helpers used by collection and packet registries. */
public final class IntegerMath {
    public static final int MAX_SIGNED_POWER_OF_TWO = 1 << 30;

    private IntegerMath() {
    }

    public static int nextPowerOfTwo(int value) {
        if (value <= 1) return 1;
        if (value >= MAX_SIGNED_POWER_OF_TWO) return MAX_SIGNED_POWER_OF_TWO;
        return Integer.highestOneBit(value - 1) << 1;
    }
}
