package net.minebo.cobalt.service.deathmessage;

import java.util.UUID;

public record DamageRecord(UUID attacker, double amount, DeathCauseCategory category, String weapon, long timestamp) {

    public boolean hasAttacker() {
        return attacker != null;
    }
}