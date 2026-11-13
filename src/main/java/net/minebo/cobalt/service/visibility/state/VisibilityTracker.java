package net.minebo.cobalt.service.visibility.state;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Which targets each viewer currently has hidden by this service. */
public final class VisibilityTracker {

    private final Map<UUID, Set<UUID>> hidden = new ConcurrentHashMap<>();

    public boolean isHidden(UUID viewer, UUID target) {
        Set<UUID> targets = hidden.get(viewer);
        return targets != null && targets.contains(target);
    }

    public void setHidden(UUID viewer, UUID target, boolean hide) {
        if (hide) {
            hidden.computeIfAbsent(viewer, k -> ConcurrentHashMap.newKeySet()).add(target);
        } else {
            Set<UUID> targets = hidden.get(viewer);
            if (targets != null) {
                targets.remove(target);
            }
        }
    }

    public Set<UUID> hiddenFrom(UUID viewer) {
        Set<UUID> targets = hidden.get(viewer);
        return targets == null ? Set.of() : Set.copyOf(targets);
    }

    public Map<UUID, Set<UUID>> snapshot() {
        return Map.copyOf(hidden);
    }

    /**
     * Drops the player as a viewer only. Bukkit keeps a viewer's hidden entry for a target
     * across the target relogging, so those entries must stay to match.
     */
    public void removeViewer(UUID viewer) {
        hidden.remove(viewer);
    }

    public void clear() {
        hidden.clear();
    }
}
