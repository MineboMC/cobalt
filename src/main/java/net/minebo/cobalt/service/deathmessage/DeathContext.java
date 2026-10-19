package net.minebo.cobalt.service.deathmessage;

import net.minebo.cobalt.service.deathmessage.tracker.DamageLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DeathContext {

    private final UUID victim;
    private final DamageRecord killingBlow;
    private final DeathCauseCategory category;
    private final Map<UUID, Double> contributions;
    private final UUID killer;
    private final List<UUID> assists;

    private DeathContext(UUID victim, DamageRecord killingBlow, DeathCauseCategory category,
                          Map<UUID, Double> contributions, UUID killer, List<UUID> assists) {
        this.victim = victim;
        this.killingBlow = killingBlow;
        this.category = category;
        this.contributions = contributions;
        this.killer = killer;
        this.assists = assists;
    }

    public static DeathContext build(UUID victim, DamageLog log, DamageRecord killingBlow,
                                      DeathCauseCategory category, long assistWindowMillis,
                                      double minimumAssistDamage) {
        if (log != null) {
            log.trim(assistWindowMillis);
        }
        Map<UUID, Double> contributions = log == null ? Map.of() : log.contributions();
        UUID killer = killingBlow != null ? killingBlow.attacker() : null;

        List<UUID> assists = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : contributions.entrySet()) {
            if (entry.getKey().equals(killer)) {
                continue;
            }
            if (entry.getValue() >= minimumAssistDamage) {
                assists.add(entry.getKey());
            }
        }

        // Wrapped rather than Map.copyOf, which does not preserve the (highest-first) iteration order.
        Map<UUID, Double> immutableContributions = Collections.unmodifiableMap(new LinkedHashMap<>(contributions));
        return new DeathContext(victim, killingBlow, category, immutableContributions, killer, List.copyOf(assists));
    }

    public UUID victim() { return victim; }
    public DamageRecord killingBlow() { return killingBlow; }
    public DeathCauseCategory category() { return category; }

    /** Total damage dealt by each attacking player during the tracking window, highest first. */
    public Map<UUID, Double> contributions() { return contributions; }

    public UUID killer() { return killer; }
    public boolean hasKiller() { return killer != null; }

    /** Players who damaged the victim but aren't credited with the kill. */
    public List<UUID> assists() { return assists; }

    public double contributionOf(UUID player) {
        return contributions.getOrDefault(player, 0.0D);
    }

    /** This player's share of total tracked damage, as a percentage from 0-100. */
    public double contributionPercent(UUID player) {
        double total = contributions.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0.0D) {
            return 0.0D;
        }
        return (contributionOf(player) / total) * 100.0D;
    }

    /** The item, projectile, or mob type behind the killing blow, or null if purely environmental. */
    public String weapon() {
        return killingBlow == null ? null : killingBlow.weapon();
    }
}