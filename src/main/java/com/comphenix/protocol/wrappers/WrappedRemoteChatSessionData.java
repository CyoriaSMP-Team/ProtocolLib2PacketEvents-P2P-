package com.comphenix.protocol.wrappers;

import java.util.Objects;
import java.util.UUID;

public class WrappedRemoteChatSessionData extends AbstractWrapper {
    private UUID sessionId; private WrappedProfilePublicKey.WrappedProfileKeyData profilePublicKey;
    public WrappedRemoteChatSessionData(Object handle){super(handle==null?Object.class:handle.getClass());if(handle!=null)setHandle(handle);}
    public WrappedRemoteChatSessionData(UUID id,WrappedProfilePublicKey.WrappedProfileKeyData key){super(WrappedRemoteChatSessionData.class);this.sessionId=id;this.profilePublicKey=key;this.handle=this;}
    public UUID getSessionID(){return sessionId;} public void setSessionID(UUID v){sessionId=v;} public WrappedProfilePublicKey.WrappedProfileKeyData getProfilePublicKey(){return profilePublicKey;} public void setProfilePublicKey(WrappedProfilePublicKey.WrappedProfileKeyData v){profilePublicKey=v;}
    public boolean equals(Object o){return o instanceof WrappedRemoteChatSessionData v&&Objects.equals(sessionId,v.sessionId)&&Objects.equals(profilePublicKey,v.profilePublicKey);} public int hashCode(){return Objects.hash(sessionId,profilePublicKey);} public String toString(){return "WrappedRemoteChatSessionData["+sessionId+"]";}
    public static WrappedRemoteChatSessionData fromPlayer(org.bukkit.entity.Player player){return player==null?null:new WrappedRemoteChatSessionData(player.getUniqueId(),null);}
}
