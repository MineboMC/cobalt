package net.minebo.cobalt.service.hologram.registry;

import net.minebo.cobalt.service.hologram.Hologram;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class HologramRegistry {
    private static final int START_ID = 900_000;
    private static final int LINE_BUDGET = 32;
    private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(START_ID);

    public int nextBaseEntityId() { return nextId.getAndAdd(LINE_BUDGET); }
    public void register(Hologram hologram) { holograms.put(hologram.id().toLowerCase(), hologram); }
    public void remove(String id) { holograms.remove(id.toLowerCase()); }
    public Hologram get(String id) { return holograms.get(id.toLowerCase()); }
    public Collection<Hologram> all() { return holograms.values(); }
    public void clear() { holograms.clear(); }
}