package net.minebo.cobalt.menu.construct;

import net.minebo.cobalt.menu.MenuHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;  // New import
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Menu {

    public String title;
    public Integer size;

    public final ConcurrentHashMap<Integer, Supplier<Button>> buttonSuppliers = new ConcurrentHashMap<>();

    public boolean autoUpdate = false;
    public boolean updateAfterClick = true;
    public boolean nonCancelling = false;  // New field for noncancelling inventory mode
    private GameMode originalMode;  // New field to store original game mode for noncancelling menus

    public Menu() {
        this.title = "Default Title";
        this.size = 9;
    }

    public Menu setTitle(String title) {
        this.title = title;
        return this;
    }

    public Menu setSize(Integer size) {
        this.size = size;
        return this;
    }

    public Menu setButton(Integer slot, Button button) {
        buttonSuppliers.put(slot, () -> button);
        return this;
    }

    public Menu fillEmpty(Material material, Boolean hideName){
        Integer i = 0;

        while (i <= size-1) {
            if(!buttonSuppliers.containsKey(i)){
                buttonSuppliers.put(i, () ->  (hideName) ? new Button().setMaterial(material).setName(" ") : new Button().setMaterial(material));
            }

            i++;
        }

        return this;
    }

    public Menu setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        return this;
    }

    public Menu setUpdateAfterClick(boolean updateAfterClick) {
        this.updateAfterClick = updateAfterClick;
        return this;
    }

    public Menu setNoncancellingInventory(boolean nonCancelling) {  // New setter
        this.nonCancelling = nonCancelling;
        return this;
    }

    public int getSlot(int x, int y) {
        return y * 9 + x;
    }

    public void openMenu(Player player) {

        Inventory inv = Bukkit.createInventory(null, size, title);

        // Set every slot, so no ghosts!
        for (int i = 0; i < size; i++) {
            Supplier<Button> supplier = buttonSuppliers.get(i);
            if (supplier != null) {
                inv.setItem(i, supplier.get().build());
            } else if (!nonCancelling) {
                // Only fill empty slots with AIR if the menu is not noncancelling (to prevent ghosts in regular menus)
                // For noncancelling menus, leave empty slots unset to allow free item placement/manipulation
                inv.setItem(i, new Button().setMaterial(Material.AIR).setName(" ").build());
            }
            // If nonCancelling and no supplier, do nothing (leave slot empty for free manipulation)
        }

        // Temporarily switch to Survival mode for noncancelling menus to allow item manipulation in any restrictive mode
        if (nonCancelling && player.getGameMode() != GameMode.SURVIVAL) {
            originalMode = player.getGameMode();
            player.setGameMode(GameMode.SURVIVAL);
        }

        player.openInventory(inv);

        MenuHandler.currentlyOpenedMenus.put(player.getName(), inv);
        MenuHandler.playerMenus.put(player.getName(), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                MenuHandler.updateMenu(player, Menu.this);
            }
        }.runTaskLater(MenuHandler.plugin, 1L);

        if (autoUpdate) {
            MenuHandler.startAutoUpdate(player, this);
        }

    }

    public void closeMenu(Player player) {
        MenuHandler.stopAutoUpdate(player);
        MenuHandler.currentlyOpenedMenus.remove(player.getName());
        MenuHandler.playerMenus.remove(player.getName());

        // Restore original game mode if it was changed for noncancelling menu
        if (nonCancelling && originalMode != null) {
            player.setGameMode(originalMode);
            originalMode = null;  // Reset for next open
        }
    }

}