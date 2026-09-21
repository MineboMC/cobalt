package net.minebo.cobalt.service.tablist.registry;

import net.minebo.cobalt.service.tablist.TabListProvider;

public final class RegisteredTabListProvider {

    private final TabListProvider provider;
    private final int weight;

    public RegisteredTabListProvider(TabListProvider provider, int weight) {
        this.provider = provider;
        this.weight = weight;
    }

    public TabListProvider provider() {
        return provider;
    }

    public int weight() {
        return weight;
    }
}