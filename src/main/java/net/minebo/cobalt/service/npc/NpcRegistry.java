package net.minebo.cobalt.service.npc;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class NpcRegistry {

    private final Map<UUID, Npc> byId = new ConcurrentHashMap<>();
    private final Map<Integer, Npc> byEntityId = new ConcurrentHashMap<>();
    private final AtomicInteger entityIds = new AtomicInteger(800_000);

    public Npc register(Npc npc) {
        byId.put(npc.id(), npc);
        byEntityId.put(npc.entityId(), npc);
        return npc;
    }

    public void remove(UUID id) {
        Npc npc = byId.remove(id);
        if (npc != null) byEntityId.remove(npc.entityId());
    }

    public Npc get(UUID id) { return byId.get(id); }
    public Npc byEntityId(int entityId) { return byEntityId.get(entityId); }
    public Collection<Npc> all() { return byId.values(); }
    public int nextEntityId() { return entityIds.getAndIncrement(); }
}