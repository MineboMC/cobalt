package net.minebo.cobalt.service.tablist;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface TabListProvider {

    TabLayout provide(Player player);

    default Component getHeader(Player player) {
        return Component.empty();
    }

    default Component getFooter(Player player) {
        return Component.empty();
    }
}