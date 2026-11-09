package net.minebo.cobalt.service.menu.listener;

import net.minebo.cobalt.service.menu.MenuService;
import net.minebo.cobalt.service.menu.construct.Button;
import net.minebo.cobalt.service.menu.construct.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class ButtonListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String playerName = player.getName();
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        Inventory topInventory = event.getView().getTopInventory();
        if (!clickedInventory.equals(topInventory)) return;

        Menu menu = MenuService.getPlayerMenu(playerName);
        if (menu == null) return;

        int slot = event.getSlot();

        if (slot < 0 || slot >= topInventory.getSize()) {
            event.setCancelled(true);
            return;
        }

        if (!menu.buttonSuppliers.containsKey(slot)) {
            if (!menu.nonCancelling) {
                event.setCancelled(true);
            }
            return;
        }

        Button button = menu.buttonSuppliers.get(slot).get();
        if (button == null) {
            if (!menu.nonCancelling) {
                event.setCancelled(true);
            }
            return;
        }

        ClickType clickType = event.getClick();

        boolean shouldCancel = button.shouldCancel(player, clickType);
        event.setCancelled(shouldCancel);

        button.onClick(clickType, player);

        if (menu.updateAfterClick) {
            MenuService.updateMenu(player, menu);
        }
    }
}