package net.minebo.cobalt.service.actionbar.registry;

import net.minebo.cobalt.service.actionbar.ActionBarProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ActionBarProviderRegistry {

    private final List<RegisteredActionBarProvider> providers = new ArrayList<>();

    public void register(ActionBarProvider provider, int weight) {
        Objects.requireNonNull(provider, "provider");
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
            providers.add(new RegisteredActionBarProvider(provider, weight));
            providers.sort((a, b) -> Integer.compare(b.weight(), a.weight()));
        }
    }

    public void unregister(ActionBarProvider provider) {
        if (provider == null) {
            return;
        }
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
        }
    }

    public List<ActionBarProvider> all() {
        synchronized (providers) {
            return providers.stream().map(RegisteredActionBarProvider::provider).toList();
        }
    }

    public ActionBarProvider select() {
        synchronized (providers) {
            if (providers.isEmpty()) {
                return null;
            }
            return providers.get(0).provider();
        }
    }
}
