package net.minebo.cobalt.menu.construct;

import net.minebo.cobalt.menu.MenuHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Menu {

    public String title;
    public Integer size;

    public final ConcurrentHashMap<Integer, Button> buttons = new ConcurrentHashMap<>();

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
        buttons.put(slot, button);
        return this;
    }

    public Menu fillEmpty(Material material, Boolean hideName){
        Integer i = 0;

        while (i <= size-1) {
            if(!buttons.containsKey(i)){
                buttons.put(i, (hideName) ? new Button().setMaterial(material).setName("") : new Button().setMaterial(material));
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

        for (Map.Entry<Integer, Button> entry : buttons.entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue().build());
        }

        player.openInventory(inv);

        MenuHandler.currentlyOpenedMenus.put(player.getName(), inv);
        MenuHandler.playerMenus.put(player.getName(), this);

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
