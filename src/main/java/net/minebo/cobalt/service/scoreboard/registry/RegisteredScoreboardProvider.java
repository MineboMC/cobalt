package net.minebo.cobalt.service.scoreboard.registry;

import net.minebo.cobalt.service.scoreboard.ScoreboardProvider;

public final class RegisteredScoreboardProvider {

    private final ScoreboardProvider provider;
    private final int weight;

    public RegisteredScoreboardProvider(ScoreboardProvider provider, int weight) {
        this.provider = provider;
        this.weight = weight;
    }

    public ScoreboardProvider provider() {
        return provider;
    }

    public int weight() {
        return weight;
    }
}