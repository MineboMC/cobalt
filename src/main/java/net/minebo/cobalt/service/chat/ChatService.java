package net.minebo.cobalt.service.chat;

import net.kyori.adventure.text.Component;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.chat.channel.GlobalChatChannel;
import net.minebo.cobalt.service.chat.event.ChannelMessageEvent;
import net.minebo.cobalt.service.chat.listener.ChatListener;
import net.minebo.cobalt.service.chat.store.ChannelSelectionStore;
import net.minebo.cobalt.service.chat.store.MemoryChannelSelectionStore;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatService extends CService {

    private final Map<String, ChatChannel> channels = new ConcurrentHashMap<>();

    private String defaultChannelId = GlobalChatChannel.ID;
    private ChannelSelectionStore store = new MemoryChannelSelectionStore();
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

        registerChannel(new GlobalChatChannel("<player><gray>: <white><message>", "!", null));

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
    }

    public boolean isEnabled() {
        return listener != null;
    }

    /** Replaces any channel with the same id. */
    public void registerChannel(ChatChannel channel) {
        channels.put(channel.getId().toLowerCase(Locale.ROOT), channel);
    }

    public void unregisterChannel(ChatChannel channel) {
        channels.remove(channel.getId().toLowerCase(Locale.ROOT), channel);
    }

    public void setSelectionStore(ChannelSelectionStore store) {
        this.store = store == null ? new MemoryChannelSelectionStore() : store;
    }

    /** By id, or by one of a channel's aliases. */
    public ChatChannel getChannel(String name) {
        if (name == null) {
            return null;
        }
        String key = name.toLowerCase(Locale.ROOT);
        ChatChannel channel = channels.get(key);
        if (channel != null) {
            return channel;
        }
        for (ChatChannel candidate : channels.values()) {
            for (String alias : candidate.getAliases()) {
                if (alias.equalsIgnoreCase(key)) {
                    return candidate;
                }
            }
        }
        return null;
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

    /**
     * The player's selected channel, or the default if they haven't picked one, it was removed,
     * or they've lost the right to speak in it.
     */
    public ChatChannel getChannel(Player player) {
        String id = store.load(player);
        ChatChannel channel = id == null ? null : channels.get(id);
        return channel != null && channel.canSpeak(player) ? channel : getDefaultChannel();
    }

    /** Null, or the default channel, goes back to the default. */
    public void setChannel(Player player, ChatChannel channel) {
        boolean isDefault = channel == null || channel == getDefaultChannel();
        store.save(player, isDefault ? null : channel.getId().toLowerCase(Locale.ROOT));
    }

    /** Switches the player to the channel, or back to the default if they're already in it. Returns where they ended up. */
    public ChatChannel toggleChannel(Player player, ChatChannel channel) {
        setChannel(player, getChannel(player) == channel ? null : channel);
        return getChannel(player);
    }

    /**
     * What /channel and channel shortcuts like /sc do: with a message it's sent in the channel once,
     * without one the player toggles in and out of the channel.
     */
    public void toggleOrSend(Player player, ChatChannel channel, String message) {
        if (!isEnabled()) {
            player.sendMessage(Coloring.toComponent("<red>Chat channels are disabled."));
            return;
        }
        if (!channel.canSpeak(player)) {
            player.sendMessage(Coloring.toComponent("<red>You can't speak in " + channel.getDisplayName() + "<red> chat."));
            return;
        }

        if (message != null && !message.isBlank()) {
            send(player, channel, Component.text(message));
            return;
        }

        ChatChannel now = toggleChannel(player, channel);
        player.sendMessage(Coloring.toComponent("<gray>You are now talking in <reset>" + now.getDisplayName() + "<gray>."));
    }

    /** Sends a message in the channel outside of chat, e.g. from a command. Returns false if it wasn't sent. */
    public boolean send(Player sender, ChatChannel channel, Component message) {
        if (!channel.canSpeak(sender)) {
            return false;
        }
        Component approved = approve(sender, channel, message);
        if (approved == null) {
            return false;
        }
        if (!channel.dispatch(sender, approved)) {
            deliverLocally(sender, channel, approved);
        }
        return true;
    }

    /** Fires {@link ChannelMessageEvent}. The message to send, or null if a listener cancelled it. */
    public Component approve(Player sender, ChatChannel channel, Component message) {
        ChannelMessageEvent event = new ChannelMessageEvent(sender, channel, message);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled() ? null : Objects.requireNonNullElse(event.getMessage(), message);
    }

    /** Sends the message to everyone on this server who can receive it, and the console. */
    public void deliverLocally(Player sender, ChatChannel channel, Component message) {
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            if (receiver.equals(sender) || channel.canReceive(sender, receiver)) {
                receiver.sendMessage(channel.format(sender, receiver, message));
            }
        }
        Bukkit.getConsoleSender().sendMessage(channel.format(sender, null, message));
    }

    /**
     * The channel whose prefix starts this message and that the sender can speak in, preferring
     * the longest matching prefix.
     */
    public ChatChannel getChannelByPrefix(Player sender, String message) {
        ChatChannel match = null;
        for (ChatChannel channel : channels.values()) {
            String prefix = channel.getPrefix();
            if (prefix == null || prefix.isEmpty() || !message.startsWith(prefix) || !channel.canSpeak(sender)) {
                continue;
            }
            if (match == null || prefix.length() > match.getPrefix().length()) {
                match = channel;
            }
        }
        return match;
    }

    public void handleQuit(Player player) {
        store.forget(player);
    }
}
