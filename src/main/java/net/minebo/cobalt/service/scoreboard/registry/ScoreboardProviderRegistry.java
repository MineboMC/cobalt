package net.minebo.cobalt.service.scoreboard.registry;

import net.minebo.cobalt.service.scoreboard.ScoreboardProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ScoreboardProviderRegistry {

    private final List<RegisteredScoreboardProvider> providers = new ArrayList<>();

    public void register(ScoreboardProvider provider, int weight) {
        Objects.requireNonNull(provider, "provider");
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
            providers.add(new RegisteredScoreboardProvider(provider, weight));
            providers.sort((a, b) -> Integer.compare(b.weight(), a.weight()));
        }
    }

    public void unregister(ScoreboardProvider provider) {
        if (provider == null) {
            return;
        }
        synchronized (providers) {
            providers.removeIf(existing -> existing.provider() == provider);
        }
    }

    public List<ScoreboardProvider> all() {
        synchronized (providers) {
            return providers.stream().map(RegisteredScoreboardProvider::provider).toList();
        }
    }

    public ScoreboardProvider select() {
        synchronized (providers) {
            if (providers.isEmpty()) {
                return null;
            }
            return providers.get(0).provider();
        }
    }
}