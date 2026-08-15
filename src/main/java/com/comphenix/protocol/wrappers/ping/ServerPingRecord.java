package com.comphenix.protocol.wrappers.ping;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Record-style ping representation used by modern Minecraft status packets. */
public final class ServerPingRecord implements ServerPingImpl {
    public static final class PlayerSample { public int max; public int online; public Object sample; public PlayerSample(){this(0,0,null);} public PlayerSample(int max,int online,Object sample){this.max=max;this.online=online;this.sample=sample;} }
    public static final class ServerData { public String name; public int protocol; public ServerData(){this("",0);} public ServerData(String name,int protocol){this.name=name;this.protocol=protocol;} }
    public static final class Favicon { public byte[] iconBytes; public Favicon(){this(new byte[0]);} public Favicon(byte[] bytes){iconBytes=bytes==null?new byte[0]:bytes.clone();} }
    public static final class NameAndId { public UUID id; public String name; public NameAndId(){this(null,"");} public NameAndId(UUID id,String name){this.id=id;this.name=name;} }
    private WrappedChatComponent motd=WrappedChatComponent.fromLegacyText("A Minecraft Server");
    private final PlayerSample sample=new PlayerSample(); private final ServerData data=new ServerData(); private Favicon favicon=new Favicon(); private boolean secure; private boolean visible=true;
    public ServerPingRecord(){}
    public ServerPingRecord(Object handle){if(handle instanceof ServerPingRecord other){motd=other.motd;sample.max=other.sample.max;sample.online=other.sample.online;sample.sample=other.sample.sample;data.name=other.data.name;data.protocol=other.data.protocol;favicon=new Favicon(other.favicon.iconBytes);secure=other.secure;visible=other.visible;}else if(handle instanceof WrappedServerPing ping){copy(ping);}}
    public static ServerPingRecord fromJson(String json){WrappedServerPing ping=WrappedServerPing.fromJson(json);return new ServerPingRecord(ping);}
    private void copy(WrappedServerPing ping){motd=ping.getMotD();sample.max=ping.getPlayersMaximum();sample.online=ping.getPlayersOnline();data.name=ping.getVersionName();data.protocol=ping.getVersionProtocol();favicon=ping.getFavicon()==null?new Favicon():new Favicon(ping.getFavicon().getDataCopy());secure=ping.isEnforceSecureChat();visible=ping.isPlayersVisible();setPlayers(ping.getPlayers());}
    public WrappedChatComponent getMotD(){return motd;} public void setMotD(WrappedChatComponent value){motd=value;}
    public int getPlayersMaximum(){return sample.max;} public void setPlayersMaximum(int value){sample.max=value;}
    public int getPlayersOnline(){return sample.online;} public void setPlayersOnline(int value){sample.online=value;}
    public ImmutableList<WrappedGameProfile> getPlayers(){if(!(sample.sample instanceof Iterable<?> values))return ImmutableList.of();List<WrappedGameProfile> out=new ArrayList<>();for(Object v:values)if(v instanceof WrappedGameProfile p)out.add(p);return ImmutableList.copyOf(out);}
    public void setPlayers(Iterable<? extends WrappedGameProfile> values){if(values==null){sample.sample=null;return;}List<WrappedGameProfile> out=new ArrayList<>();for(WrappedGameProfile p:values)out.add(p);sample.sample=out;}
    public String getVersionName(){return data.name;} public void setVersionName(String value){data.name=value;}
    public int getVersionProtocol(){return data.protocol;} public void setVersionProtocol(int value){data.protocol=value;}
    public WrappedServerPing.CompressedImage getFavicon(){return new WrappedServerPing.CompressedImage("image/png",favicon.iconBytes);} public void setFavicon(WrappedServerPing.CompressedImage value){favicon=value==null?new Favicon():new Favicon(value.getDataCopy());}
    public boolean isEnforceSecureChat(){return secure;} public void setEnforceSecureChat(boolean value){secure=value;}
    public void resetPlayers(){sample.max=0;sample.online=0;sample.sample=null;} public void resetVersion(){data.name="";data.protocol=0;}
    public boolean arePlayersVisible(){return visible;} public void setPlayersVisible(boolean value){visible=value;}
    public String getJson(){WrappedServerPing ping=new WrappedServerPing();ping.setMotD(motd);ping.setPlayersMaximum(sample.max);ping.setPlayersOnline(sample.online);ping.setPlayers(getPlayers());ping.setVersionName(data.name);ping.setVersionProtocol(data.protocol);ping.setFavicon(getFavicon());ping.setEnforceSecureChat(secure);ping.setPlayersVisible(visible);return ping.toJson();}
    public Object getHandle(){return this;}
    @Override public boolean equals(Object o){if(!(o instanceof ServerPingRecord other))return false;return java.util.Objects.equals(motd,other.motd)&&sample.max==other.sample.max&&sample.online==other.sample.online&&data.protocol==other.data.protocol&&java.util.Objects.equals(data.name,other.data.name)&&Arrays.equals(favicon.iconBytes,other.favicon.iconBytes)&&secure==other.secure;}
    @Override public int hashCode(){return java.util.Objects.hash(motd,sample.max,sample.online,data.name,data.protocol,Arrays.hashCode(favicon.iconBytes),secure);}
}
