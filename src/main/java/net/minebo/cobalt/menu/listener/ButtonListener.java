package net.minebo.cobalt.menu.listener;

import net.minebo.cobalt.menu.MenuHandler;
import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.menu.construct.Menu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        String playerName = player.getName();

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        Inventory topInventory = event.getView().getTopInventory();

        if (!clickedInventory.equals(topInventory)) return;

        Menu menu = MenuHandler.getPlayerMenu(playerName);
        if (menu == null) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        Button button = menu.buttons.get(slot);

        if (button != null) {
            button.onClick(event.getClick(), player);

            if (menu.updateAfterClick) {
                MenuHandler.updateMenu(player, menu);
            }
        }
    }

}
