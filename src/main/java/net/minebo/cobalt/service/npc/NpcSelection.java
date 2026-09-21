package net.minebo.cobalt.service.npc;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcSelection {

    private final Map<UUID, UUID> selected = new ConcurrentHashMap<>();

    public void select(Player player, Npc npc) {
        if (player == null) return;
        if (npc == null) selected.remove(player.getUniqueId());
        else selected.put(player.getUniqueId(), npc.id());
    }

    public UUID get(Player player) {
        return player == null ? null : selected.get(player.getUniqueId());
    }

    public void clear(Player player) {
        if (player != null) selected.remove(player.getUniqueId());
    }
}