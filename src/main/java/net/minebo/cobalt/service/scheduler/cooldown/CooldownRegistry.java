package net.minebo.cobalt.service.scheduler.cooldown;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownRegistry {

    private final Map<String, Cooldown> types = new ConcurrentHashMap<>();

    public void register(Cooldown cooldown) {
        types.put(cooldown.getId(), cooldown);
    }

    public void unregister(String id) {
        types.remove(id);
    }

    public Cooldown get(String id) {
        return types.get(id);
    }

    public Collection<Cooldown> all() {
        return types.values();
    }
}