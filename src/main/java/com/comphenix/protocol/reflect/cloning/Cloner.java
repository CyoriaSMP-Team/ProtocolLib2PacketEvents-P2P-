package com.comphenix.protocol.reflect.cloning;

public interface Cloner {
    boolean canClone(Object source);
    Object clone(Object source);
}
