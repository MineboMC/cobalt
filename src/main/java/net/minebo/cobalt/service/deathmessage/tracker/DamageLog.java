package net.minebo.cobalt.service.deathmessage.tracker;

import net.minebo.cobalt.service.deathmessage.DamageRecord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DamageLog {

    private final List<DamageRecord> records = new CopyOnWriteArrayList<>();

    public void add(DamageRecord record) {
        if (record != null) {
            records.add(record);
        }
    }

    /** Drops any record older than {@code windowMillis} from now. */
    public void trim(long windowMillis) {
        long cutoff = System.currentTimeMillis() - windowMillis;
        records.removeIf(record -> record.timestamp() < cutoff);
    }

    public List<DamageRecord> records() {
        return List.copyOf(records);
    }

    public DamageRecord last() {
        return records.isEmpty() ? null : records.get(records.size() - 1);
    }

    /**
     * Total damage dealt by each attacking player, highest first.
     * Damage with no attacker (mobs, fall, fire, etc.) isn't counted here.
     */
    public Map<UUID, Double> contributions() {
        Map<UUID, Double> totals = new LinkedHashMap<>();
        for (DamageRecord record : records) {
            if (record.hasAttacker()) {
                totals.merge(record.attacker(), record.amount(), Double::sum);
            }
        }
        LinkedHashMap<UUID, Double> sorted = new LinkedHashMap<>();
        totals.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    public void clear() {
        records.clear();
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }
}