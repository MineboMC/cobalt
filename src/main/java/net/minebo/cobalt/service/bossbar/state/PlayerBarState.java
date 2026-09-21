package net.minebo.cobalt.service.bossbar.state;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class PlayerBarState {

    private final UUID playerId;
    private final UUID barId;
    private ShownBar shown;

    public PlayerBarState(UUID playerId) {
        this.playerId = playerId;
        this.barId = UUID.nameUUIDFromBytes(("cobalt-bossbar:" + playerId).getBytes(StandardCharsets.UTF_8));
    }

    public UUID playerId() {
        return playerId;
    }

    public UUID barId() {
        return barId;
    }

    public ShownBar shown() {
        return shown;
    }

    public void setShown(ShownBar shown) {
        this.shown = shown;
    }

    public boolean isVisible() {
        return shown != null;
    }
}