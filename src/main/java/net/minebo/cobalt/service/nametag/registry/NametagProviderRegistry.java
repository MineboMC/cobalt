package net.minebo.cobalt.service.nametag.registry;

import net.minebo.cobalt.service.nametag.NametagProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class NametagProviderRegistry {

    private final List<RegisteredNametagProvider> providers = new ArrayList<>();

    public void register(NametagProvider provider, int weight) {
        Objects.requireNonNull(provider, "provider");
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
            providers.add(new RegisteredNametagProvider(provider, weight));
            providers.sort((a, b) -> Integer.compare(b.weight(), a.weight()));
        }
    }

    public void unregister(NametagProvider provider) {
        if (provider == null) {
            return;
        }
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
        }
    }

    public List<NametagProvider> all() {
        synchronized (providers) {
            return providers.stream().map(RegisteredNametagProvider::provider).toList();
        }
    }

    public NametagProvider select() {
        synchronized (providers) {
            if (providers.isEmpty()) {
                return null;
            }
            return providers.get(0).provider();
        }
    }
}