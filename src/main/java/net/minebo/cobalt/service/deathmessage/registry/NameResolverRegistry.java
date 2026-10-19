package net.minebo.cobalt.service.deathmessage.registry;

import net.minebo.cobalt.service.deathmessage.PlayerNameResolver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class NameResolverRegistry {

    private record Entry(PlayerNameResolver resolver, int weight) {}

    private final List<Entry> entries = new CopyOnWriteArrayList<>();
    private final PlayerNameResolver fallback;

    public NameResolverRegistry(PlayerNameResolver fallback) {
        this.fallback = fallback;
    }

    public void register(PlayerNameResolver resolver, int weight) {
        if (resolver == null) {
            return;
        }
        entries.removeIf(entry -> entry.resolver() == resolver);
        entries.add(new Entry(resolver, weight));
    }

    public void unregister(PlayerNameResolver resolver) {
        entries.removeIf(entry -> entry.resolver() == resolver);
    }

    /** Highest-weight registered resolver, falling back to the default if none is registered. */
    public PlayerNameResolver select() {
        Entry best = null;
        for (Entry entry : entries) {
            if (best == null || entry.weight() > best.weight()) {
                best = entry;
            }
        }
        return best != null ? best.resolver() : fallback;
    }
}