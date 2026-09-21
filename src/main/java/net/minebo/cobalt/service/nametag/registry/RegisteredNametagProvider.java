package net.minebo.cobalt.service.nametag.registry;

import net.minebo.cobalt.service.nametag.NametagProvider;

public final class RegisteredNametagProvider {

    private final NametagProvider provider;
    private final int weight;

    public RegisteredNametagProvider(NametagProvider provider, int weight) {
        this.provider = provider;
        this.weight = weight;
    }

    public NametagProvider provider() {
        return provider;
    }

    public int weight() {
        return weight;
    }
}