package net.minebo.cobalt.scoreboard.provider;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScoreboardPriority {
    HIGHEST(4),
    HIGH(3),
    MEDIUM(2),
    LOW(1),
    LOWEST(0);

    private final int priority;

}