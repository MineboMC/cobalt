package net.minebo.cobalt.service.nametag.state;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NametagTracker {

    private final Map<UUID, ViewerNametagState> states = new ConcurrentHashMap<>();

    public ViewerNametagState getOrCreate(Player viewer) {
        return states.computeIfAbsent(viewer.getUniqueId(), ViewerNametagState::new);
    }

    public Optional<ViewerNametagState> get(Player viewer) {
        return Optional.ofNullable(states.get(viewer.getUniqueId()));
    }

    public void remove(Player viewer) {
        states.remove(viewer.getUniqueId());
    }

    public void clear() {
        states.clear();
    }
}