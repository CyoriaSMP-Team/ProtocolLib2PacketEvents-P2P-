package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;

import java.util.Objects;
import java.util.UUID;

public class PlayerInfoData {
    private UUID profileId; private int latency; private boolean listed=true; private EnumWrappers.NativeGameMode gameMode; private WrappedGameProfile profile; private WrappedChatComponent displayName; private boolean showHat=true; private int listOrder; private WrappedProfilePublicKey.WrappedProfileKeyData keyData; private WrappedRemoteChatSessionData remoteData;
    public PlayerInfoData(WrappedGameProfile profile,int latency,EnumWrappers.NativeGameMode mode,WrappedChatComponent displayName){this(profile==null?null:profile.getUUID(),latency,true,mode,profile,displayName,true,0,null);}
    public PlayerInfoData(WrappedGameProfile profile,int latency,EnumWrappers.NativeGameMode mode,WrappedChatComponent displayName,WrappedProfilePublicKey.WrappedProfileKeyData key){this(profile,latency,mode,displayName);this.keyData=key;}
    public PlayerInfoData(UUID id,int latency,boolean listed,EnumWrappers.NativeGameMode mode,WrappedGameProfile profile,WrappedChatComponent displayName){this(id,latency,listed,mode,profile,displayName,true,0,null);}
    public PlayerInfoData(UUID id,int latency,boolean listed,EnumWrappers.NativeGameMode mode,WrappedGameProfile profile,WrappedChatComponent displayName,WrappedRemoteChatSessionData remote){this(id,latency,listed,mode,profile,displayName,true,0,remote);}
    public PlayerInfoData(UUID id,int latency,boolean listed,EnumWrappers.NativeGameMode mode,WrappedGameProfile profile,WrappedChatComponent displayName,WrappedProfilePublicKey.WrappedProfileKeyData key){this(id,latency,listed,mode,profile,displayName,true,0,null);this.keyData=key;}
    public PlayerInfoData(UUID id,int latency,boolean listed,EnumWrappers.NativeGameMode mode,WrappedGameProfile profile,WrappedChatComponent displayName,int order,WrappedRemoteChatSessionData remote){this(id,latency,listed,mode,profile,displayName,true,order,remote);}
    public PlayerInfoData(UUID id,int latency,boolean listed,EnumWrappers.NativeGameMode mode,WrappedGameProfile profile,WrappedChatComponent displayName,boolean showHat,WrappedRemoteChatSessionData remote){this(id,latency,listed,mode,profile,displayName,showHat,0,remote);}
    public PlayerInfoData(UUID id,int latency,boolean listed,EnumWrappers.NativeGameMode mode,WrappedGameProfile profile,WrappedChatComponent displayName,boolean showHat,int order,WrappedRemoteChatSessionData remote){this.profileId=id;this.latency=latency;this.listed=listed;this.gameMode=mode;this.profile=profile;this.displayName=displayName;this.showHat=showHat;this.listOrder=order;this.remoteData=remote;}
    public UUID getProfileId(){return profileId;} public WrappedGameProfile getProfile(){return profile;} public int getPing(){return latency;} public int getLatency(){return latency;} public boolean isListed(){return listed;} public EnumWrappers.NativeGameMode getGameMode(){return gameMode;} public WrappedChatComponent getDisplayName(){return displayName;} public boolean isShowHat(){return showHat;} public int getListOrder(){return listOrder;} public WrappedProfilePublicKey.WrappedProfileKeyData getProfileKeyData(){return keyData;} public WrappedRemoteChatSessionData getRemoteChatSessionData(){return remoteData;}
    public static EquivalentConverter<PlayerInfoData> getConverter(){return new EquivalentConverter<>(){public PlayerInfoData getSpecific(Object g){return g instanceof PlayerInfoData v?v:null;}public Object getGeneric(PlayerInfoData s){return s;}public Class<PlayerInfoData> getSpecificType(){return PlayerInfoData.class;}public Class<?> getGenericType(){return PlayerInfoData.class;}};}
    public boolean equals(Object o){return o instanceof PlayerInfoData v&&Objects.equals(profileId,v.profileId)&&latency==v.latency&&Objects.equals(profile,v.profile);} public int hashCode(){return Objects.hash(profileId,latency,profile);} public String toString(){return "PlayerInfoData["+profileId+","+latency+"]";}
}
