package net.minebo.cobalt.service.visibility.example;

import net.minebo.cobalt.service.visibility.Visibility;
import net.minebo.cobalt.service.visibility.VisibilityProvider;
import org.bukkit.entity.Player;

/** Hides players with cobalt.vanished from anyone without cobalt.vanished.see. */
public final class ExampleVisibilityProvider implements VisibilityProvider {

    @Override
    public Visibility getVisibility(Player viewer, Player target) {
        if (target.hasPermission("cobalt.vanished") && !viewer.hasPermission("cobalt.vanished.see")) {
            return Visibility.HIDE;
        }
        return Visibility.NEUTRAL;
    }
}
