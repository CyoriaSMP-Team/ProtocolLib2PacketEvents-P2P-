/* ProtocolLib2PacketEvents - clean-room server-ping wrapper. */
package com.comphenix.protocol.wrappers;

import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class WrappedServerPing implements ClonableWrapper {
    private WrappedChatComponent motd = WrappedChatComponent.fromText("");
    private CompressedImage favicon;
    private boolean playersVisible = true;
    private int playersOnline;
    private int playersMaximum;
    private final List<WrappedGameProfile> players = new ArrayList<>();
    private String versionName = "1.9.4";
    private int versionProtocol = 340;
    private boolean chatPreviewEnabled;
    private boolean enforceSecureChat;

    public WrappedServerPing() { }
    private WrappedServerPing(WrappedServerPing other) {
        motd = other.motd == null ? null : other.motd.deepClone();
        favicon = other.favicon == null ? null : new CompressedImage(other.favicon.mime, other.favicon.data);
        playersVisible = other.playersVisible; playersOnline = other.playersOnline; playersMaximum = other.playersMaximum;
        players.addAll(other.players); versionName = other.versionName; versionProtocol = other.versionProtocol;
        chatPreviewEnabled = other.chatPreviewEnabled; enforceSecureChat = other.enforceSecureChat;
    }
    public static WrappedServerPing fromHandle(Object handle) { return handle instanceof WrappedServerPing value ? new WrappedServerPing(value) : new WrappedServerPing(); }
    public static WrappedServerPing fromJson(String json) {
        if (json == null) throw new IllegalArgumentException("json cannot be null");
        WrappedServerPing result = new WrappedServerPing();
        try {
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            if (root.has("description")) {
                String description = root.get("description").isJsonPrimitive()
                        ? root.get("description").getAsString() : root.get("description").toString();
                result.motd = WrappedChatComponent.fromJson(description.startsWith("\"")
                        ? description : root.get("description").toString());
            }
            if (root.has("players") && root.get("players").isJsonObject()) {
                com.google.gson.JsonObject players = root.getAsJsonObject("players");
                if (players.has("online")) result.playersOnline = players.get("online").getAsInt();
                if (players.has("max")) result.playersMaximum = players.get("max").getAsInt();
            }
            if (root.has("version") && root.get("version").isJsonObject()) {
                com.google.gson.JsonObject version = root.getAsJsonObject("version");
                if (version.has("name")) result.versionName = version.get("name").getAsString();
                if (version.has("protocol")) result.versionProtocol = version.get("protocol").getAsInt();
            }
            if (root.has("favicon") && root.get("favicon").isJsonPrimitive()) {
                result.favicon = CompressedImage.fromEncodedText(root.get("favicon").getAsString());
            }
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Invalid server ping JSON", error);
        }
        return result;
    }
    public WrappedChatComponent getMotD() { return motd; }
    public void setMotD(WrappedChatComponent value) { motd = value; }
    public void setMotD(String value) { setMotD(WrappedChatComponent.fromLegacyText(value)); }
    public CompressedImage getFavicon() { return favicon; }
    public void setFavicon(CompressedImage value) { favicon = value; }
    @Deprecated public boolean isChatPreviewEnabled() { return chatPreviewEnabled; }
    @Deprecated public void setChatPreviewEnabled(boolean value) { chatPreviewEnabled = value; }
    public boolean isEnforceSecureChat() { return enforceSecureChat; }
    public void setEnforceSecureChat(boolean value) { enforceSecureChat = value; }
    public int getPlayersOnline() { if (!playersVisible) throw new IllegalStateException("player counts are hidden"); return playersOnline; }
    public void setPlayersOnline(int value) { playersOnline = value; }
    public int getPlayersMaximum() { if (!playersVisible) throw new IllegalStateException("player counts are hidden"); return playersMaximum; }
    public void setPlayersMaximum(int value) { playersMaximum = value; }
    public void setPlayersVisible(boolean value) { playersVisible = value; }
    public boolean isPlayersVisible() { return playersVisible; }
    public ImmutableList<WrappedGameProfile> getPlayers() { return ImmutableList.copyOf(players); }
    public void setPlayers(Iterable<? extends WrappedGameProfile> values) { players.clear(); if (values != null) for (var value : values) players.add(value); }
    public void setBukkitPlayers(Iterable<? extends Player> values) { players.clear(); if (values != null) for (Player value : values) players.add(WrappedGameProfile.fromPlayer(value)); }
    public String getVersionName() { return versionName; }
    public void setVersionName(String value) { versionName = value; }
    public int getVersionProtocol() { return versionProtocol; }
    public void setVersionProtocol(int value) { versionProtocol = value; }
    public Object getHandle() { return this; }
    public WrappedServerPing deepClone() { return new WrappedServerPing(this); }
    public String toJson() {
        String escaped = motd == null ? "" : motd.getJson().replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"description\":" + escaped + ",\"players\":{\"max\":" + playersMaximum + ",\"online\":" + playersOnline + "},\"version\":{\"name\":\"" + versionName + "\",\"protocol\":" + versionProtocol + "}}";
    }
    @Override public String toString() { return "WrappedServerPing< " + toJson() + ">"; }
    @Override public boolean equals(Object other) { return other instanceof WrappedServerPing ping && toJson().equals(ping.toJson()); }
    @Override public int hashCode() { return toJson().hashCode(); }

    public static class CompressedImage {
        protected volatile String mime; protected volatile byte[] data; protected volatile String encoded;
        protected CompressedImage() { }
        public CompressedImage(String mime, byte[] data) { this.mime = mime; this.data = data == null ? new byte[0] : data.clone(); }
        public static CompressedImage fromPng(InputStream input) throws IOException { return new CompressedImage("image/png", input.readAllBytes()); }
        public static CompressedImage fromPng(byte[] data) { return new CompressedImage("image/png", data); }
        public static CompressedImage fromPng(java.awt.image.RenderedImage image) throws IOException {
            if (image == null) throw new IllegalArgumentException("image cannot be null");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!javax.imageio.ImageIO.write(image, "png", output)) {
                throw new IOException("No PNG writer is available");
            }
            return fromPng(output.toByteArray());
        }
        public static CompressedImage fromBase64Png(String base64) { return new CompressedImage("image/png", Base64.getDecoder().decode(base64)); }
        public static CompressedImage fromEncodedText(String text) { int comma = text.indexOf(','); String prefix = comma < 0 ? "image/png" : text.substring(5, comma).replace(";base64", ""); String value = comma < 0 ? text : text.substring(comma + 1); CompressedImage image = new CompressedImage(prefix, Base64.getDecoder().decode(value)); image.encoded = text; return image; }
        public String getMime() { return mime; }
        public byte[] getDataCopy() { return data.clone(); }
        public String toEncodedText() { if (encoded == null) encoded = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(data); return encoded; }
        public java.awt.image.BufferedImage getImage() throws IOException { return javax.imageio.ImageIO.read(new ByteArrayInputStream(data)); }
        @Override public boolean equals(Object other) { return other instanceof CompressedImage image && mime.equals(image.mime) && java.util.Arrays.equals(data, image.data); }
        @Override public int hashCode() { return 31 * mime.hashCode() + java.util.Arrays.hashCode(data); }
    }

    static class EncodedCompressedImage extends CompressedImage {
        public EncodedCompressedImage(String encoded) { super(); CompressedImage value = fromEncodedText(encoded); this.mime=value.mime; this.data=value.data; this.encoded=encoded; }
        @Override public String getMime() { return mime; }
        @Override public String toEncodedText() { return encoded == null ? super.toEncodedText() : encoded; }
    }
}
