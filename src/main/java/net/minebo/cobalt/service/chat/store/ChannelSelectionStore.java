package net.minebo.cobalt.service.chat.store;

import org.bukkit.entity.Player;

/** Where each player's selected channel is kept. Swap it to keep selections across servers. */
public interface ChannelSelectionStore {

    /** The id of the player's selected channel, or null for the default. Can be called off the main thread. */
    String load(Player player);

    /** {@code channelId} is null to go back to the default. */
    void save(Player player, String channelId);

    /** The player left this server. */
    default void forget(Player player) {
    }
}
