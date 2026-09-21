package net.minebo.cobalt.service.npc;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcLookTracker {

    private final Map<UUID, Map<UUID, float[]>> angles = new ConcurrentHashMap<>();

    public float[] current(Player viewer, Npc npc, float fallbackYaw, float fallbackPitch) {
        Map<UUID, float[]> byNpc = angles.computeIfAbsent(viewer.getUniqueId(), key -> new ConcurrentHashMap<>());
        return byNpc.computeIfAbsent(npc.id(), key -> new float[]{fallbackYaw, fallbackPitch});
    }

    public void clear(Player viewer) {
        angles.remove(viewer.getUniqueId());
    }

    public void clearNpc(Npc npc) {
        for (Map<UUID, float[]> byNpc : angles.values()) {
            byNpc.remove(npc.id());
        }
    }
}