package com.comphenix.protocol.wrappers.ping;

import com.comphenix.protocol.wrappers.AbstractWrapper;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.google.common.collect.ImmutableList;

/** Legacy object-shaped ping facade backed by the same logical ping model. */
public final class LegacyServerPing extends AbstractWrapper implements ServerPingImpl {
    private final ServerPingRecord delegate;
    public LegacyServerPing(){super(Object.class);delegate=new ServerPingRecord();setHandle(delegate);}
    public LegacyServerPing(Object handle){super(Object.class);delegate=handle instanceof ServerPingRecord p?p:new ServerPingRecord(handle);setHandle(delegate);}
    public static LegacyServerPing fromHandle(Object handle){return new LegacyServerPing(handle);}
    public static LegacyServerPing fromJson(String json){return new LegacyServerPing(ServerPingRecord.fromJson(json));}
    public void resetPlayers(){delegate.resetPlayers();} public void resetVersion(){delegate.resetVersion();}
    public WrappedChatComponent getMotD(){return delegate.getMotD();} public void setMotD(WrappedChatComponent v){delegate.setMotD(v);}
    public int getPlayersOnline(){return delegate.getPlayersOnline();} public void setPlayersOnline(int v){delegate.setPlayersOnline(v);}
    public int getPlayersMaximum(){return delegate.getPlayersMaximum();} public void setPlayersMaximum(int v){delegate.setPlayersMaximum(v);}
    public ImmutableList<WrappedGameProfile> getPlayers(){return delegate.getPlayers();} public void setPlayers(Iterable<? extends WrappedGameProfile> v){delegate.setPlayers(v);}
    public String getVersionName(){return delegate.getVersionName();} public void setVersionName(String v){delegate.setVersionName(v);}
    public int getVersionProtocol(){return delegate.getVersionProtocol();} public void setVersionProtocol(int v){delegate.setVersionProtocol(v);}
    public WrappedServerPing.CompressedImage getFavicon(){return delegate.getFavicon();} public void setFavicon(WrappedServerPing.CompressedImage v){delegate.setFavicon(v);}
    public boolean isEnforceSecureChat(){return delegate.isEnforceSecureChat();} public void setEnforceSecureChat(boolean v){delegate.setEnforceSecureChat(v);}
    public boolean arePlayersVisible(){return delegate.arePlayersVisible();} public void setPlayersVisible(boolean v){delegate.setPlayersVisible(v);}
    public String getJson(){return delegate.getJson();} public String toJson(){return getJson();}
    public boolean isChatPreviewEnabled(){return delegate.isChatPreviewEnabled();} public void setChatPreviewEnabled(boolean v){delegate.setChatPreviewEnabled(v);}
    public LegacyServerPing deepClone(){return new LegacyServerPing(delegate.getHandle());}
    @Override public String toString(){return "LegacyServerPing< "+getJson()+">";}
}
