/* ProtocolLib2PacketEvents - clean-room block-data wrapper. */
package com.comphenix.protocol.wrappers;

import org.bukkit.Material;

import java.util.Objects;

/** Version-neutral block state view backed by Bukkit material/data. */
public class WrappedBlockData implements ClonableWrapper {
    private Material type;
    private int data;
    protected WrappedBlockData() { this(Material.AIR, 0); }
    private WrappedBlockData(Material type, int data) { this.type = type; this.data = data; }
    public WrappedBlockData(Object handle) {
        if (handle instanceof WrappedBlockData value) { this.type = value.type; this.data = value.data; }
        else { this.type = Material.AIR; this.data = 0; }
    }
    public static WrappedBlockData createData(Material type) { return createData(type, 0); }
    public static WrappedBlockData createData(Material type, int data) {
        return new WrappedBlockData(Objects.requireNonNull(type, "type"), data);
    }
    public static WrappedBlockData fromHandle(Object handle) { return handle == null ? null : new WrappedBlockData(handle); }
    public static WrappedBlockData createData(Object data) { return new WrappedBlockData(data); }
    public Material getType() { return type; }
    public int getData() { return data; }
    public void setType(Material type) { this.type = Objects.requireNonNull(type, "type"); }
    public void setData(int data) { this.data = data; }
    public void setTypeAndData(Material type, int data) { setType(type); setData(data); }
    public WrappedBlockData deepClone() { return createData(type, data); }
    public Object getHandle() { return this; }
    @Override public boolean equals(Object other) { return other instanceof WrappedBlockData value && type == value.type && data == value.data; }
    @Override public int hashCode() { return Objects.hash(type, data); }
    @Override public String toString() { return type + "[data=" + data + "]"; }

    static class NewBlockData extends WrappedBlockData {
        NewBlockData() { super(); }
        @Override public Material getType(){return super.getType();}
        @Override public int getData(){return super.getData();}
        @Override public void setType(Material type){super.setType(type);}
        @Override public void setData(int data){super.setData(data);}
        @Override public void setTypeAndData(Material type,int data){super.setTypeAndData(type,data);}
        public WrappedBlockData deepClone(){return WrappedBlockData.createData(getType(),getData());}
        public ClonableWrapper deepCloneAsInterface(){return deepClone();}
    }
    static class OldBlockData extends WrappedBlockData {
        OldBlockData() { super(); }
        @Override public Material getType(){return super.getType();}
        @Override public int getData(){return super.getData();}
        @Override public void setType(Material type){super.setType(type);}
        @Override public void setData(int data){super.setData(data);}
        @Override public void setTypeAndData(Material type,int data){super.setTypeAndData(type,data);}
        public WrappedBlockData deepClone(){return WrappedBlockData.createData(getType(),getData());}
        public ClonableWrapper deepCloneAsInterface(){return deepClone();}
    }
}
