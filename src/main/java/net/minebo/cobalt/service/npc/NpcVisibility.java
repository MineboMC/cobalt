package net.minebo.cobalt.service.npc;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcVisibility {

    private final Map<UUID, Set<UUID>> shown = new ConcurrentHashMap<>();

    public boolean isShown(Player viewer, Npc npc) {
        Set<UUID> set = shown.get(viewer.getUniqueId());
        return set != null && set.contains(npc.id());
    }

    public void markShown(Player viewer, Npc npc) {
        shown.computeIfAbsent(viewer.getUniqueId(), key -> ConcurrentHashMap.newKeySet()).add(npc.id());
    }

    public void markHidden(Player viewer, Npc npc) {
        Set<UUID> set = shown.get(viewer.getUniqueId());
        if (set != null) {
            set.remove(npc.id());
        }
    }

    public void clear(Player viewer) {
        shown.remove(viewer.getUniqueId());
    }
}