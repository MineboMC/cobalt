package net.minebo.cobalt.service.scheduler.timer;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TimerRegistry {

    private final Map<String, ScheduledTimer> types = new ConcurrentHashMap<>();

    public void register(ScheduledTimer timer) {
        types.put(timer.getId(), timer);
    }

    public void unregister(String id) {
        types.remove(id);
    }

    public ScheduledTimer get(String id) {
        return types.get(id);
    }

    public Collection<ScheduledTimer> all() {
        return types.values();
    }
}