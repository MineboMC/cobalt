package net.minebo.cobalt.service.chat.event;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minebo.cobalt.service.chat.ChatChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called before a message is sent in a channel, from chat or from /channel. Async when it comes from chat.
 * Cancelling it drops the message, so tell the sender why.
 */
@Getter
public class ChannelMessageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player sender;
    private final ChatChannel channel;
    @Setter private Component message;
    @Setter private boolean cancelled;

    public ChannelMessageEvent(Player sender, ChatChannel channel, Component message) {
        super(!Bukkit.isPrimaryThread());
        this.sender = sender;
        this.channel = channel;
        this.message = message;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
