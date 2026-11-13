package net.minebo.cobalt.service.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

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

    /** Typing this before a message sends it to this channel once, e.g. "!" for global. Null for none. */
    default String getPrefix() {
        return null;
    }

    default boolean canSpeak(Player sender) {
        return true;
    }

    boolean canReceive(Player sender, Player receiver);

    /** Formats the message for one viewer. {@code viewer} is null for the console. */
    Component format(Player sender, Player viewer, Component message);
}
