package net.minebo.cobalt.service.hologram.state;

import net.minebo.cobalt.service.hologram.Hologram;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HologramTracker {
    private final Map<UUID, Set<String>> shown = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, List<String>>> lastLines = new ConcurrentHashMap<>();

    public boolean isShown(Player player, Hologram hologram) {
        Set<String> ids = shown.get(player.getUniqueId());
        return ids != null && ids.contains(hologram.id().toLowerCase());
    }

    public void markShown(Player player, Hologram hologram) {
        shown.computeIfAbsent(player.getUniqueId(), key -> ConcurrentHashMap.newKeySet())
                .add(hologram.id().toLowerCase());
    }

    public void markHidden(Player player, Hologram hologram) {
        Set<String> ids = shown.get(player.getUniqueId());
        if (ids != null) ids.remove(hologram.id().toLowerCase());
        Map<String, List<String>> lines = lastLines.get(player.getUniqueId());
        if (lines != null) lines.remove(hologram.id().toLowerCase());
    }

    public List<String> lastLines(Player player, Hologram hologram) {
        Map<String, List<String>> lines = lastLines.get(player.getUniqueId());
        return lines == null ? List.of() : lines.getOrDefault(hologram.id().toLowerCase(), List.of());
    }

    public void setLastLines(Player player, Hologram hologram, List<String> lines) {
        lastLines.computeIfAbsent(player.getUniqueId(), key -> new ConcurrentHashMap<>())
                .put(hologram.id().toLowerCase(), List.copyOf(lines));
    }

    public void clear(Player player) {
        shown.remove(player.getUniqueId());
        lastLines.remove(player.getUniqueId());
    }

    public void clear() {
        shown.clear();
        lastLines.clear();
    }
}