package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;

import java.util.Objects;

public class Vector3F {
    private float x; private float y; private float z;
    public Vector3F() { }
    public Vector3F(float x,float y,float z){this.x=x;this.y=y;this.z=z;}
    public float getX(){return x;} public Vector3F setX(float value){x=value;return this;}
    public float getY(){return y;} public Vector3F setY(float value){y=value;return this;}
    public float getZ(){return z;} public Vector3F setZ(float value){z=value;return this;}
    public int hashCode(){return Objects.hash(x,y,z);} public boolean equals(Object o){return o instanceof Vector3F v&&Float.compare(x,v.x)==0&&Float.compare(y,v.y)==0&&Float.compare(z,v.z)==0;}
    public static Class<?> getMinecraftClass(){return Vector3F.class;}
    public static EquivalentConverter<Vector3F> getConverter(){return new EquivalentConverter<>(){public Vector3F getSpecific(Object g){return g instanceof Vector3F?v:(v=new Vector3F());} private Vector3F v; public Object getGeneric(Vector3F s){return s;} public Class<Vector3F> getSpecificType(){return Vector3F.class;} public Class<?> getGenericType(){return Vector3F.class;}};}
}
