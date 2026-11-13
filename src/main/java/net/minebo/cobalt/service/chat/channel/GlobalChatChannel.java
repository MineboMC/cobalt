package net.minebo.cobalt.service.chat.channel;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minebo.cobalt.service.chat.ChatChannel;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class GlobalChatChannel implements ChatChannel {

    public static final String ID = "global";

    private final String format;
    private final String prefix;
    private final String permission;

    /**
     * @param format     MiniMessage/legacy format with {@code <player>} and {@code <message>} tags. PlaceholderAPI placeholders are applied for the sender.
     * @param prefix     quick-send prefix, or null/blank for none
     * @param permission permission required to speak, or null/blank for none
     */
    public GlobalChatChannel(String format, String prefix, String permission) {
        this.format = format;
        this.prefix = prefix == null || prefix.isBlank() ? null : prefix;
        this.permission = permission == null || permission.isBlank() ? null : permission;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "<green>Global";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean canSpeak(Player sender) {
        return permission == null || sender.hasPermission(permission);
    }

    @Override
    public boolean canReceive(Player sender, Player receiver) {
        return true;
    }

    @Override
    public Component format(Player sender, Player viewer, Component message) {
        String raw = format;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            raw = PlaceholderAPI.setPlaceholders(sender, raw);
        }
        // The message goes in as a component so players can't inject MiniMessage tags.
        return MiniMessage.miniMessage().deserialize(Coloring.legacyToMiniMessage(raw),
                Placeholder.component("player", sender.displayName()),
                Placeholder.component("message", message));
    }
}
