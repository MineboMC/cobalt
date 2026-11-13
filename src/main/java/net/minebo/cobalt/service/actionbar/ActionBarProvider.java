package net.minebo.cobalt.service.actionbar;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface ActionBarProvider {

    /** Text to show in the player's action bar. Return null or an empty component to show nothing. */
    Component getText(Player player);
}
