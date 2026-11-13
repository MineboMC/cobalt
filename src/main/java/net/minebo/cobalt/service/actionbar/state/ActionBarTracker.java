package net.minebo.cobalt.service.actionbar.state;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActionBarTracker {

    private final Map<UUID, PlayerActionBarState> states = new ConcurrentHashMap<>();

    public PlayerActionBarState getOrCreate(Player player) {
        return states.computeIfAbsent(player.getUniqueId(), PlayerActionBarState::new);
    }

    public Optional<PlayerActionBarState> get(Player player) {
        return Optional.ofNullable(states.get(player.getUniqueId()));
    }

    public PlayerActionBarState remove(Player player) {
        return states.remove(player.getUniqueId());
    }

    public void clear() {
        states.clear();
    }
}
