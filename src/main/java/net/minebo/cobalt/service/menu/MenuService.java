package net.minebo.cobalt.service.menu;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.menu.construct.Button;
import net.minebo.cobalt.service.menu.construct.Menu;
import net.minebo.cobalt.service.menu.listener.ButtonListener;
import net.minebo.cobalt.service.menu.listener.MenuListener;
import net.minebo.cobalt.service.CService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MenuService extends CService {
    public static Map<String, Inventory> currentlyOpenedMenus = new HashMap<>();
    public static Map<String, BukkitRunnable> checkTasks = new HashMap<>();
    public static Map<String, Menu> playerMenus = new HashMap<>();

    @Override
    public String getName() {
        return "Menus";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new ButtonListener(), Cobalt.getInstance());
        Bukkit.getPluginManager().registerEvents(new MenuListener(), Cobalt.getInstance());
    }

    public static void startAutoUpdate(Player player, Menu menu) {
        if (!menu.autoUpdate || player == null) return;
        stopAutoUpdate(player);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                Menu current = playerMenus.get(player.getName());
                if (current != null) {
                    updateMenu(player, current);
                } else {
                    cancel();
                }
            }
        };

        task.runTaskTimer(Cobalt.getInstance(), 20L, 20L);
        checkTasks.put(player.getName(), task);
    }

    public static void stopAutoUpdate(Player player) {
        BukkitRunnable task = checkTasks.remove(player.getName());
        if (task != null) task.cancel();
    }

    public static void updateMenu(Player player, Menu menu) {
        if (player == null || menu == null) return;

        Inventory inv = currentlyOpenedMenus.get(player.getName());
        if (inv == null) return;

        Inventory topInv = player.getOpenInventory().getTopInventory();
        if (!inv.equals(topInv)) return;

        // Critical: rebuild dynamic buttons each update tick (Basalt-like behavior)
        menu.rebuild(player);

        int invSize = inv.getSize();

        // hard clear to avoid stale items
        for (int i = 0; i < invSize; i++) {
            inv.setItem(i, null);
        }

        // re-render current button suppliers
        for (Map.Entry<Integer, Supplier<Button>> entry : menu.buttonSuppliers.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= invSize) continue;

            Button button = entry.getValue().get();
            if (button != null) {
                inv.setItem(slot, button.build());
            }
        }

        player.updateInventory();
    }

    public static Menu getPlayerMenu(String playerName) {
        return playerMenus.get(playerName);
    }


}