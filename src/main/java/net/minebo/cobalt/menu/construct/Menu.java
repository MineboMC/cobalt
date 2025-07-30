package net.minebo.cobalt.menu.construct;

import net.minebo.cobalt.menu.MenuHandler;
import org.bukkit.Bukkit;
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

    public void openMenu(Player player) {

        Inventory inv = Bukkit.createInventory(null, size, title);

        // Set every slot, so no ghosts!
        for (int i = 0; i < size; i++) {
            Supplier<Button> supplier = buttonSuppliers.get(i);
            if (supplier != null) {
                inv.setItem(i, supplier.get().build());
            } else {
                // Fill with glass pane, no name
                inv.setItem(i, new Button().setMaterial(Material.AIR).setName(" ").build());
            }
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
    }

}
