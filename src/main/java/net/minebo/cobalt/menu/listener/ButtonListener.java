package net.minebo.cobalt.menu.listener;

import net.minebo.cobalt.menu.MenuHandler;
import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.menu.construct.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class ButtonListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getInventory();

        String playerName = player.getName();

        if (!MenuHandler.currentlyOpenedMenus.containsKey(playerName)) return;

        Inventory menuInventory = MenuHandler.currentlyOpenedMenus.get(playerName);

        if (!clickedInventory.equals(menuInventory)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        Menu menu = MenuHandler.getPlayerMenu(playerName);
        if (menu == null) return;

        Button button = menu.buttons.get(slot);

        if (button != null) {
            button.onClick(player);

            if (menu.updateAfterClick) {
                MenuHandler.updateMenu(player, menu);
            }
        }
    }
}
