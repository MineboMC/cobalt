package net.minebo.cobalt.service.visibility.registry;

import net.minebo.cobalt.service.visibility.VisibilityProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class VisibilityProviderRegistry {

    private final List<RegisteredVisibilityProvider> providers = new ArrayList<>();

    public void register(VisibilityProvider provider, int weight) {
        Objects.requireNonNull(provider, "provider");
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
            providers.add(new RegisteredVisibilityProvider(provider, weight));
            providers.sort((a, b) -> Integer.compare(b.weight(), a.weight()));
        }
    }

    public void unregister(VisibilityProvider provider) {
        if (provider == null) {
            return;
        }
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
        }
    }

    /** All providers, highest weight first. */
    public List<VisibilityProvider> all() {
        synchronized (providers) {
            return providers.stream().map(RegisteredVisibilityProvider::provider).toList();
        }
    }
}
