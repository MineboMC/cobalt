package net.minebo.cobalt.menu.listener;

import net.minebo.cobalt.menu.MenuHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        String playerName = player.getName();

        // Check if the player had one of our menus open
        if (MenuHandler.currentlyOpenedMenus.containsKey(playerName)) {
            MenuHandler.getPlayerMenu(playerName).closeMenu(player);
        }
    }

}
