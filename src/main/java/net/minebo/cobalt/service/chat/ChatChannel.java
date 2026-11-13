package net.minebo.cobalt.service.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * A chat channel. Chat is handled on Paper's async chat thread, so implementations
 * must be safe to call off the main thread.
 */
public interface ChatChannel {

    /** Unique, lowercase id used for /channel and storage. */
    String getId();

    default String getDisplayName() {
        return getId();
    }

    /** Other names /channel accepts for this channel, e.g. "sc" for staff. */
    default List<String> getAliases() {
        return List.of();
    }

    /**
     * Typing this before a message sends it to this channel once, e.g. "!" for global. Null for none.
     * Players who can't speak in the channel just send the message as normal.
     */
    default String getPrefix() {
        return null;
    }

    default boolean canSpeak(Player sender) {
        return true;
    }

    boolean canReceive(Player sender, Player receiver);

    /** Formats the message for one viewer. {@code viewer} is null for the console. */
    Component format(Player sender, Player viewer, Component message);

    /**
     * Delivers the message yourself instead of to the players on this server that {@link #canReceive}
     * it, e.g. to send it to other servers too. Return false (the default) to have it delivered locally.
     * Can be called off the main thread.
     */
    default boolean dispatch(Player sender, Component message) {
        return false;
    }
}
