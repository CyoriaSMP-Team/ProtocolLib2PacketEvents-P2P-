package com.comphenix.protocol.wrappers;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class WrappedSaltedSignature extends AbstractWrapper {
    private long salt; private byte[] signature;
    public WrappedSaltedSignature(Object handle){super(handle==null?Object.class:handle.getClass());if(handle!=null)setHandle(handle);}
    public WrappedSaltedSignature(long salt,byte[] signature){super(WrappedSaltedSignature.class);this.salt=salt;this.signature=signature==null?null:signature.clone();this.handle=this;}
    public boolean isSigned(){return signature!=null&&signature.length>0;} public long getSalt(){return salt;} public void setSalt(long v){salt=v;} public byte[] getSignature(){return signature==null?null:signature.clone();} public void setSignature(byte[] v){signature=v==null?null:v.clone();} public byte[] getSaltBytes(){return ByteBuffer.allocate(Long.BYTES).putLong(salt).array();}
}
