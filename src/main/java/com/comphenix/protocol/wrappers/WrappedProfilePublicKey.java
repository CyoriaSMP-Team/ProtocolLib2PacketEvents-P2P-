package com.comphenix.protocol.wrappers;

import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;

public class WrappedProfilePublicKey extends AbstractWrapper {
    private WrappedProfileKeyData keyData;
    public WrappedProfilePublicKey(Object handle){super(handle==null?Object.class:handle.getClass());if(handle!=null)setHandle(handle);}
    public WrappedProfilePublicKey(WrappedProfileKeyData data){super(WrappedProfileKeyData.class);this.keyData=data;this.handle=data;}
    public static WrappedProfilePublicKey ofPlayer(org.bukkit.entity.Player player){return new WrappedProfilePublicKey((Object)null);}
    public WrappedProfileKeyData getKeyData(){return keyData;} public void setKeyData(WrappedProfileKeyData v){keyData=v;handle=v;}
    public static class WrappedProfileKeyData extends AbstractWrapper {
        private Instant expireTime; private PublicKey key; private byte[] signature;
        public WrappedProfileKeyData(Object handle){super(handle==null?Object.class:handle.getClass());if(handle!=null)setHandle(handle);}
        public WrappedProfileKeyData(Instant expireTime,PublicKey key,byte[] signature){super(WrappedProfileKeyData.class);this.expireTime=expireTime;this.key=key;this.signature=signature==null?null:signature.clone();this.handle=this;}
        public Instant getExpireTime(){return expireTime;} public void setExpireTime(Instant v){expireTime=v;} public boolean isExpired(){return expireTime!=null&&expireTime.isBefore(Instant.now());}
        public String getSignedPayload(){return signature==null?null:Base64.getEncoder().encodeToString(signature);} public PublicKey getKey(){return key;} public void setKey(PublicKey v){key=v;} public byte[] getSignature(){return signature==null?null:signature.clone();} public void setSignature(byte[] v){signature=v==null?null:v.clone();}
    }
}
