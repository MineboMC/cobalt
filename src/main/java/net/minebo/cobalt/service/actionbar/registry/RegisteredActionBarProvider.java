package net.minebo.cobalt.service.actionbar.registry;

import net.minebo.cobalt.service.actionbar.ActionBarProvider;

public final class RegisteredActionBarProvider {

    private final ActionBarProvider provider;
    private final int weight;

    public RegisteredActionBarProvider(ActionBarProvider provider, int weight) {
        this.provider = provider;
        this.weight = weight;
    }

    public ActionBarProvider provider() {
        return provider;
    }

    public int weight() {
        return weight;
    }
}
