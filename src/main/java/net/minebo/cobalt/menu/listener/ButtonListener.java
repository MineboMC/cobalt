package net.minebo.cobalt.menu.listener;

import net.minebo.cobalt.menu.MenuHandler;
import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.menu.construct.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
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

        int slot = event.getSlot();

        if (!menu.buttonSuppliers.containsKey(slot)) {
            // No button in this slot, cancel by default
            event.setCancelled(true);
            return;
        }

        Button button = menu.buttonSuppliers.get(slot).get();

        if (button == null) {
            // Null button, cancel by default
            event.setCancelled(true);
            return;
        }

        ClickType clickType = event.getClick();

        // Check if the button wants to cancel this click event
        boolean shouldCancel = button.shouldCancel(player, clickType);
        event.setCancelled(shouldCancel);

        // Execute the button's click action
        button.onClick(clickType, player);

        // Update menu if configured to do so
        if (menu.updateAfterClick) {
            MenuHandler.updateMenu(player, menu);
        }
    }

}