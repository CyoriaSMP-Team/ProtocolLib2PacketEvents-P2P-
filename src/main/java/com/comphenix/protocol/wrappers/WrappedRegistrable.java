package com.comphenix.protocol.wrappers;

public final class WrappedRegistrable extends AbstractWrapper implements ClonableWrapper {
    private Class<?> type; private MinecraftKey key;
    private WrappedRegistrable(Class<?> type,MinecraftKey key){super(WrappedRegistrable.class);this.type=type;this.key=key;handle=this;}
    public static WrappedRegistrable fromHandle(Factory factory,Object handle){return factory==null?null:new WrappedRegistrable(factory.type,MinecraftKey.fromHandle(handle));}
    public static WrappedRegistrable fromHandle(Class<?> type,Object handle){return new WrappedRegistrable(type,MinecraftKey.fromHandle(handle));}
    public static WrappedRegistrable fromClassAndKey(Class<?> type,MinecraftKey key){return new WrappedRegistrable(type,key);} public static WrappedRegistrable fromClassAndKey(Class<?> type,String key){return fromClassAndKey(type,new MinecraftKey(key));} public static WrappedRegistrable blockEntityType(MinecraftKey key){return fromClassAndKey(Object.class,key);} public static WrappedRegistrable blockEntityType(String key){return blockEntityType(new MinecraftKey(key));}
    public MinecraftKey getKey(){return key;} public void setKey(MinecraftKey v){key=v;} public WrappedRegistrable deepClone(){return new WrappedRegistrable(type,key);} public String toString(){return String.valueOf(key);} public int hashCode(){return java.util.Objects.hash(type,key);} public boolean equals(Object o){return o instanceof WrappedRegistrable v&&java.util.Objects.equals(type,v.type)&&java.util.Objects.equals(key,v.key);}
    public ClonableWrapper deepCloneAsInterface(){return deepClone();}
    public static class Factory {
        private final Class<?> type;
        public Factory(Class<?> type){this.type=type;}
        public static Factory getOrCreate(Class<?> type){return new Factory(type == null ? Object.class : type);}
        public MinecraftKey getKey(Object handle){
            if (handle instanceof WrappedRegistrable value) return value.getKey();
            return MinecraftKey.fromHandle(handle);
        }
        public Object getHandle(MinecraftKey key){return key;}
    }
}
