package net.minebo.cobalt.service.chat.store;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Keeps selections until the player leaves the server. */
public final class MemoryChannelSelectionStore implements ChannelSelectionStore {

    private final Map<UUID, String> selected = new ConcurrentHashMap<>();

    @Override
    public String load(Player player) {
        return selected.get(player.getUniqueId());
    }

    @Override
    public void save(Player player, String channelId) {
        if (channelId == null) {
            selected.remove(player.getUniqueId());
        } else {
            selected.put(player.getUniqueId(), channelId);
        }
    }

    @Override
    public void forget(Player player) {
        selected.remove(player.getUniqueId());
    }
}
