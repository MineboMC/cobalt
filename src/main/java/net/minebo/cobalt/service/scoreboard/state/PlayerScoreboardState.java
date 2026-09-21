package net.minebo.cobalt.service.scoreboard.state;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class PlayerScoreboardState {

    public static final int MAX_LINES = 15;
    public static final String OBJECTIVE = "cobalt_sb";

    private final UUID playerId;
    private boolean created;
    private Component title = Component.empty();
    private final List<Component> lines = new ArrayList<>();

    public PlayerScoreboardState(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID playerId() {
        return playerId;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public Component title() {
        return title;
    }

    public void setTitle(Component title) {
        this.title = title == null ? Component.empty() : title;
    }

    public List<Component> lines() {
        return Collections.unmodifiableList(lines);
    }

    public void setLines(List<Component> next) {
        lines.clear();
        if (next != null) {
            lines.addAll(next);
        }
    }

    public boolean titleEquals(Component other) {
        return title.equals(other == null ? Component.empty() : other);
    }

    public boolean linesEqual(List<Component> other) {
        if (other == null) {
            return lines.isEmpty();
        }
        return lines.equals(other);
    }
}