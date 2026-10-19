package net.minebo.cobalt.service.deathmessage.tracker;

import net.minebo.cobalt.service.deathmessage.DamageRecord;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds a rolling per-victim damage log used to compute death contributions.
 */
public final class DeathTracker {

    private final Map<UUID, DamageLog> logs = new ConcurrentHashMap<>();

    public void record(UUID victim, DamageRecord record, long windowMillis) {
        DamageLog log = logs.computeIfAbsent(victim, id -> new DamageLog());
        log.add(record);
        log.trim(windowMillis);
    }

    public DamageLog log(UUID victim) {
        DamageLog log = logs.get(victim);
        return log != null ? log : new DamageLog();
    }

    public void clear(UUID victim) {
        logs.remove(victim);
    }

    public void clearAll() {
        logs.clear();
    }
}