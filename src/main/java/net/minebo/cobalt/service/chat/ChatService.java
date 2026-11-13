package net.minebo.cobalt.service.chat;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.chat.channel.GlobalChatChannel;
import net.minebo.cobalt.service.chat.listener.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatService extends CService {

    private final Map<String, ChatChannel> channels = new ConcurrentHashMap<>();
    private final Map<UUID, String> selected = new ConcurrentHashMap<>();

    private String defaultChannelId = GlobalChatChannel.ID;
    private ChatListener listener;

    @Override
    public String getName() {
        return "Chat";
    }

    @Override
    public void onEnable() {
        ConfigurationSection config = Cobalt.getInstance().getConfig().getConfigurationSection("chat");
        if (config != null && !config.getBoolean("enabled", true)) {
            return;
        }

        ConfigurationSection global = config == null ? null : config.getConfigurationSection("channels.global");
        registerChannel(new GlobalChatChannel(
                global == null ? "<player><gray>: <white><message>" : global.getString("format", "<player><gray>: <white><message>"),
                global == null ? null : global.getString("prefix"),
                global == null ? null : global.getString("permission")
        ));
        if (config != null) {
            defaultChannelId = config.getString("default-channel", GlobalChatChannel.ID).toLowerCase(Locale.ROOT);
        }

        listener = new ChatListener(this);
        Bukkit.getPluginManager().registerEvents(listener, Cobalt.getInstance());
    }

    @Override
    public void onDisable() {
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
        channels.clear();
        selected.clear();
    }

    public boolean isEnabled() {
        return listener != null;
    }

    public void registerChannel(ChatChannel channel) {
        channels.put(channel.getId().toLowerCase(Locale.ROOT), channel);
    }

    public void unregisterChannel(ChatChannel channel) {
        String id = channel.getId().toLowerCase(Locale.ROOT);
        channels.remove(id, channel);
        selected.values().removeIf(id::equals);
    }

    public ChatChannel getChannel(String id) {
        return id == null ? null : channels.get(id.toLowerCase(Locale.ROOT));
    }

    public List<ChatChannel> getChannels() {
        return List.copyOf(channels.values());
    }

    public ChatChannel getDefaultChannel() {
        ChatChannel channel = channels.get(defaultChannelId);
        return channel != null ? channel : channels.get(GlobalChatChannel.ID);
    }

    public void setDefaultChannel(ChatChannel channel) {
        defaultChannelId = channel.getId().toLowerCase(Locale.ROOT);
    }

    /** The player's selected channel, or the default if they haven't picked one (or it was removed). */
    public ChatChannel getChannel(Player player) {
        ChatChannel channel = getChannel(selected.get(player.getUniqueId()));
        return channel != null ? channel : getDefaultChannel();
    }

    public void setChannel(Player player, ChatChannel channel) {
        if (channel == null) {
            selected.remove(player.getUniqueId());
        } else {
            selected.put(player.getUniqueId(), channel.getId().toLowerCase(Locale.ROOT));
        }
    }

    /** The channel whose prefix starts this message, preferring the longest matching prefix. */
    public ChatChannel getChannelByPrefix(String message) {
        ChatChannel match = null;
        for (ChatChannel channel : channels.values()) {
            String prefix = channel.getPrefix();
            if (prefix == null || prefix.isEmpty() || !message.startsWith(prefix)) {
                continue;
            }
            if (match == null || prefix.length() > match.getPrefix().length()) {
                match = channel;
            }
        }
        return match;
    }

    public void handleQuit(Player player) {
        selected.remove(player.getUniqueId());
    }
}
