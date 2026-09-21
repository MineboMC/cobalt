package net.minebo.cobalt.service.bossbar.registry;

import net.minebo.cobalt.service.bossbar.BossBarProvider;

public final class RegisteredBossBarProvider {

    private final BossBarProvider provider;
    private final int weight;

    public RegisteredBossBarProvider(BossBarProvider provider, int weight) {
        this.provider = provider;
        this.weight = weight;
    }

    public BossBarProvider provider() {
        return provider;
    }

    public int weight() {
        return weight;
    }
}