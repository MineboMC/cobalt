package net.minebo.cobalt.service.tablist.state;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TabListTracker {

    private final Map<UUID, PlayerTabState> states = new ConcurrentHashMap<>();

    public PlayerTabState getOrCreate(Player player) {
        return states.computeIfAbsent(player.getUniqueId(), PlayerTabState::new);
    }

    public Optional<PlayerTabState> get(Player player) {
        return Optional.ofNullable(states.get(player.getUniqueId()));
    }

    public Optional<PlayerTabState> get(UUID playerId) {
        return Optional.ofNullable(states.get(playerId));
    }

    public PlayerTabState remove(Player player) {
        return states.remove(player.getUniqueId());
    }

    public void clear() {
        states.clear();
    }
}