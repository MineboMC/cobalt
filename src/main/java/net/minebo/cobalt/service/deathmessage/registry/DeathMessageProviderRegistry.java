package net.minebo.cobalt.service.deathmessage.registry;

import net.minebo.cobalt.service.deathmessage.DeathCauseCategory;
import net.minebo.cobalt.service.deathmessage.DeathMessageProvider;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DeathMessageProviderRegistry {

    private record Entry(DeathMessageProvider provider, int weight) {}

    private final Map<DeathCauseCategory, List<Entry>> byCategory = new EnumMap<>(DeathCauseCategory.class);

    public void register(DeathCauseCategory category, DeathMessageProvider provider, int weight) {
        if (category == null || provider == null) {
            return;
        }
        List<Entry> entries = byCategory.computeIfAbsent(category, key -> new CopyOnWriteArrayList<>());
        entries.removeIf(entry -> entry.provider() == provider);
        entries.add(new Entry(provider, weight));
    }

    public void unregister(DeathMessageProvider provider) {
        for (List<Entry> entries : byCategory.values()) {
            entries.removeIf(entry -> entry.provider() == provider);
        }
    }

    /** Highest-weight provider for the category, falling back to GENERIC's provider if none is registered. */
    public DeathMessageProvider select(DeathCauseCategory category) {
        DeathMessageProvider provider = highest(byCategory.get(category));
        if (provider != null) {
            return provider;
        }
        if (category != DeathCauseCategory.GENERIC) {
            return highest(byCategory.get(DeathCauseCategory.GENERIC));
        }
        return null;
    }

    private static DeathMessageProvider highest(List<Entry> entries) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        Entry best = entries.get(0);
        for (Entry entry : entries) {
            if (entry.weight() > best.weight()) {
                best = entry;
            }
        }
        return best.provider();
    }
}