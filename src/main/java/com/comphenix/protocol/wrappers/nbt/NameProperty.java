package com.comphenix.protocol.wrappers.nbt;

/** Name storage abstraction retained for ProtocolLib wrapper compatibility. */
public abstract class NameProperty {
    public abstract String getName();

    public abstract void setName(String name);

    public static boolean hasStringIndex(Class<?> baseClass, int index) {
        return false;
    }

    public static NameProperty fromStringIndex(Class<?> baseClass, Object target, int index) {
        return fromBean();
    }

    public static NameProperty fromBean() {
        return new NameProperty() {
            private String name = "";

            @Override
            public String getName() {
                return name;
            }

            @Override
            public void setName(String name) {
                this.name = name == null ? "" : name;
            }
        };
    }
}
