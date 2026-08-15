package com.comphenix.protocol.reflect.cloning;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializableCloner implements Cloner {
    @Override public boolean canClone(Object source){return source instanceof Serializable;}
    @Override public Object clone(Object source){return clone((Serializable)source);}
    public static <T extends Serializable> T clone(T value){try{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ObjectOutputStream out=new ObjectOutputStream(bytes)){out.writeObject(value);}try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){return (T)in.readObject();}}catch(Exception ex){throw new IllegalArgumentException("Unable to clone serializable value",ex);}}
}
