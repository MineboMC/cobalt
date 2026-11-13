package net.minebo.cobalt.service.visibility;

import org.bukkit.entity.Player;

public interface VisibilityProvider {

    /** Whether {@code viewer} should see {@code target}. Return NEUTRAL to defer to lower-weight providers. */
    Visibility getVisibility(Player viewer, Player target);
}
