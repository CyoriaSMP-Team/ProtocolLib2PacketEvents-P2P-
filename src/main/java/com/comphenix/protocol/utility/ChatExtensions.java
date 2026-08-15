package com.comphenix.protocol.utility;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/** Chat convenience methods retained without depending on an NMS chat packet. */
public final class ChatExtensions {
    private final ProtocolManager manager;
    public ChatExtensions(ProtocolManager manager) { this.manager = manager; }
    /**
     * Build the server chat packet for the currently active PacketEvents protocol.
     * The old implementation returned an empty list, which made temporary-player
     * sendMessage() silently succeed without putting anything on the wire.
     */
    public static List<PacketContainer> createChatPackets(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }
        if (PacketEvents.getAPI() == null) {
            throw new IllegalStateException("PacketEvents is not initialized; cannot create a chat packet");
        }
        PacketType type = firstSupported(
                "SYSTEM_CHAT", "SYSTEM_CHAT_MESSAGE", "CHAT_MESSAGE", "CHAT");
        if (type == null) {
            throw new UnsupportedOperationException("No server chat packet is available for "
                    + PacketEvents.getAPI().getServerManager().getVersion());
        }

        PacketContainer packet = new PacketContainer(type);
        StructureModifier<WrappedChatComponent> components = packet.getChatComponents();
        if (components.size() == 0) {
            throw new UnsupportedOperationException("PacketEvents chat wrapper has no component field for " + type);
        }
        components.write(0, WrappedChatComponent.fromText(message));
        // System-chat packets carry an overlay flag.  A false value means the normal chat HUD.
        packet.getBooleans().writeSafely(0, false);
        return List.of(packet);
    }

    private static PacketType firstSupported(String... names) {
        for (String name : names) {
            PacketType type = PacketType.fromKey(PacketType.Protocol.PLAY, PacketType.Sender.SERVER, name);
            if (type != null && type.isSupported()) return type;
        }
        return null;
    }
    public static String[] toFlowerBox(String[] lines, String border, int margin, int width) {
        if (lines == null) return new String[0];
        String prefix = border == null ? "" : border;
        String[] result = new String[lines.length + 2];
        result[0] = prefix.repeat(Math.max(0, width));
        for (int i = 0; i < lines.length; i++) result[i + 1] = prefix + " ".repeat(Math.max(0, margin)) + lines[i];
        result[result.length - 1] = result[0];
        return result;
    }
    public void sendMessageSilently(CommandSender sender, String message) { if (sender != null) sender.sendMessage(message); }
    public void broadcastMessageSilently(String permission, String message) {
        for (CommandSender sender : Bukkit.getOnlinePlayers()) if (permission == null || sender.hasPermission(permission)) sender.sendMessage(message);
    }
}
