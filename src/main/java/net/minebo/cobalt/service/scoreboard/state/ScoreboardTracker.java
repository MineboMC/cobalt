package net.minebo.cobalt.service.scoreboard.state;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ScoreboardTracker {

    private final Map<UUID, PlayerScoreboardState> states = new ConcurrentHashMap<>();

    public PlayerScoreboardState getOrCreate(Player player) {
        return states.computeIfAbsent(player.getUniqueId(), PlayerScoreboardState::new);
    }

    public Optional<PlayerScoreboardState> get(Player player) {
        return Optional.ofNullable(states.get(player.getUniqueId()));
    }

    public void remove(Player player) {
        states.remove(player.getUniqueId());
    }

    public void clear() {
        states.clear();
    }
}