package net.minebo.cobalt.menu.construct;

import net.minebo.cobalt.menu.MenuHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Menu {
    public String title;
    public Integer size;
    public final ConcurrentHashMap<Integer, Supplier<Button>> buttonSuppliers = new ConcurrentHashMap<>();
    public boolean autoUpdate = false;
    public boolean updateAfterClick = true;
    public boolean nonCancelling = false;
    private GameMode originalMode;

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

    public Menu fillEmpty(Material material, Boolean hideName) {
        for (int i = 0; i < size; i++) {
            if (!buttonSuppliers.containsKey(i)) {
                buttonSuppliers.put(i, () -> (hideName)
                        ? new Button().setMaterial(material).setName(" ")
                        : new Button().setMaterial(material));
            }
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

    public Menu setNoncancellingInventory(boolean nonCancelling) {
        this.nonCancelling = nonCancelling;
        return this;
    }

    public Menu clearButtons() {
        buttonSuppliers.clear();
        return this;
    }

    // Basalt-like hook: dynamic menus override this and repopulate buttons each update tick
    public void rebuild(Player player) {
        // default no-op for static menus
    }

    public void refresh(Player player) {
        MenuHandler.updateMenu(player, this);
    }

    public int getSlot(int x, int y) {
        return y * 9 + x;
    }

    public void openMenu(Player player) {
        int safeSize = normalizeChestSize(size);
        this.size = safeSize;

        Inventory inv = Bukkit.createInventory(null, safeSize, title);

        for (int i = 0; i < safeSize; i++) {
            Supplier<Button> supplier = buttonSuppliers.get(i);
            if (supplier != null) {
                inv.setItem(i, supplier.get().build());
            } else if (!nonCancelling) {
                inv.setItem(i, new Button().setMaterial(Material.AIR).setName(" ").build());
            }
        }

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

        if (nonCancelling && originalMode != null) {
            player.setGameMode(originalMode);
            originalMode = null;
        }
    }

    private int normalizeChestSize(int raw) {
        int clamped = Math.max(9, Math.min(54, raw));
        int rem = clamped % 9;
        return rem == 0 ? clamped : (clamped + (9 - rem));
    }
}