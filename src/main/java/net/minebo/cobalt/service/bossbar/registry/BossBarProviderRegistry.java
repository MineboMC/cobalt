package net.minebo.cobalt.service.bossbar.registry;

import net.minebo.cobalt.service.bossbar.BossBarProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BossBarProviderRegistry {

    private final List<RegisteredBossBarProvider> providers = new ArrayList<>();

    public void register(BossBarProvider provider, int weight) {
        Objects.requireNonNull(provider, "provider");
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
            providers.add(new RegisteredBossBarProvider(provider, weight));
            providers.sort((a, b) -> Integer.compare(b.weight(), a.weight()));
        }
    }

    public void unregister(BossBarProvider provider) {
        if (provider == null) {
            return;
        }
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
        }
    }

    public List<BossBarProvider> all() {
        synchronized (providers) {
            return providers.stream().map(RegisteredBossBarProvider::provider).toList();
        }
    }

    public BossBarProvider select() {
        synchronized (providers) {
            if (providers.isEmpty()) {
                return null;
            }
            return providers.get(0).provider();
        }
    }
}