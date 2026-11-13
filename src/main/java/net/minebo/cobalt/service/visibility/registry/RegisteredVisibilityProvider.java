package net.minebo.cobalt.service.visibility.registry;

import net.minebo.cobalt.service.visibility.VisibilityProvider;

public final class RegisteredVisibilityProvider {

    private final VisibilityProvider provider;
    private final int weight;

    public RegisteredVisibilityProvider(VisibilityProvider provider, int weight) {
        this.provider = provider;
        this.weight = weight;
    }

    public VisibilityProvider provider() {
        return provider;
    }

    public int weight() {
        return weight;
    }
}
