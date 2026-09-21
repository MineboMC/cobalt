package net.minebo.cobalt.service.bossbar.state;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BossBarTracker {

    private final Map<UUID, PlayerBarState> states = new ConcurrentHashMap<>();

    public PlayerBarState getOrCreate(Player player) {
        return states.computeIfAbsent(player.getUniqueId(), PlayerBarState::new);
    }

    public Optional<PlayerBarState> get(Player player) {
        return Optional.ofNullable(states.get(player.getUniqueId()));
    }

    public Optional<PlayerBarState> get(UUID playerId) {
        return Optional.ofNullable(states.get(playerId));
    }

    public PlayerBarState remove(Player player) {
        return states.remove(player.getUniqueId());
    }

    public PlayerBarState remove(UUID playerId) {
        return states.remove(playerId);
    }

    public void clear() {
        states.clear();
    }
}