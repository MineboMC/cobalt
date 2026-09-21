package net.minebo.cobalt.service.tablist.registry;

import net.minebo.cobalt.service.tablist.TabListProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TabListProviderRegistry {

    private final List<RegisteredTabListProvider> providers = new ArrayList<>();

    public void register(TabListProvider provider, int weight) {
        Objects.requireNonNull(provider, "provider");
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
            providers.add(new RegisteredTabListProvider(provider, weight));
            providers.sort((a, b) -> Integer.compare(b.weight(), a.weight()));
        }
    }

    public void unregister(TabListProvider provider) {
        if (provider == null) {
            return;
        }
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
        }
    }

    public List<TabListProvider> all() {
        synchronized (providers) {
            return providers.stream().map(RegisteredTabListProvider::provider).toList();
        }
    }

    public TabListProvider select() {
        synchronized (providers) {
            if (providers.isEmpty()) {
                return null;
            }
            return providers.get(0).provider();
        }
    }
}