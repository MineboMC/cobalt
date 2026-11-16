package net.minebo.cobalt.service.menu.construct;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.menu.MenuService;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Menu {
    public String title;
    public Integer size;
    public final ConcurrentHashMap<Integer, Supplier<Button>> buttonSuppliers = new ConcurrentHashMap<>();
    public boolean autoUpdate = false;
    public boolean updateAfterClick = true;
    public boolean nonCancelling = false;
    private InventoryType type = InventoryType.CHEST;
    private GameMode originalMode;

    public Menu() {
        this.title = "Default Title";
        this.size = 9;
    }

    public Menu setTitle(String title) {
        this.title = Coloring.translateColors(title);
        return this;
    }

    public Menu setSize(Integer size) {
        this.size = type == InventoryType.CHEST ? size : type.getDefaultSize();
        return this;
    }

    public InventoryType getType() {
        return type;
    }

    public Menu setType(InventoryType type) {
        Objects.requireNonNull(type, "Inventory type cannot be null");
        if (!type.isCreatable()) {
            throw new IllegalArgumentException("Cannot create a menu of type " + type);
        }
        this.type = type;
        this.size = type.getDefaultSize();
        return this;
    }

    public int getSize() {
        return type == InventoryType.CHEST ? normalizeChestSize(size) : type.getDefaultSize();
    }

    public Menu setButton(Integer slot, Button button) {
        buttonSuppliers.put(slot, () -> button);
        return this;
    }

    public Menu fillEmpty(Material material, Boolean hideName) {
        for (int i = 0; i < getSize(); i++) {
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

    // Dynamic menus override this to repopulate their buttons on every update.
    public void rebuild(Player player) {
    }

    public void refresh(Player player) {
        MenuService.updateMenu(player, this);
    }

    public int getSlot(int x, int y) {
        int width = switch (type) {
            case DISPENSER, DROPPER, CRAFTER -> 3;
            case HOPPER -> 5;
            default -> 9;
        };
        return y * width + x;
    }

    public void openMenu(Player player) {
        Inventory inv = type == InventoryType.CHEST
                ? Bukkit.createInventory(null, getSize(), title)
                : Bukkit.createInventory(null, type, title);
        this.size = inv.getSize();

        for (int i = 0; i < inv.getSize(); i++) {
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
        MenuService.currentlyOpenedMenus.put(player.getName(), inv);
        MenuService.playerMenus.put(player.getName(), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                MenuService.updateMenu(player, Menu.this);
            }
        }.runTaskLater(Cobalt.getInstance(), 1L);

        if (autoUpdate) {
            MenuService.startAutoUpdate(player, this);
        }
    }

    public void closeMenu(Player player) {
        MenuService.stopAutoUpdate(player);
        MenuService.currentlyOpenedMenus.remove(player.getName());
        MenuService.playerMenus.remove(player.getName());

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
