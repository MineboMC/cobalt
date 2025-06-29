package net.minebo.cobalt.menu;

import net.minebo.cobalt.menu.construct.Button;
import net.minebo.cobalt.menu.construct.Menu;
import net.minebo.cobalt.menu.listener.ButtonListener;
import net.minebo.cobalt.menu.listener.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class MenuHandler {

    public static JavaPlugin plugin;

    public static Map<String, Inventory> currentlyOpenedMenus;
    public static Map<String, BukkitRunnable> checkTasks;

    public static Map<String, Menu> playerMenus = new HashMap<>();

    public static void init(JavaPlugin plugin) {
        MenuHandler.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(new ButtonListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new MenuListener(), plugin);

        currentlyOpenedMenus = new HashMap();
        checkTasks = new HashMap();
    }

    public static void startAutoUpdate(Player player, Menu menu) {
        if (!menu.autoUpdate) return;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                updateMenu(player, menu);
            }
        };

        task.runTaskTimer(plugin, 20L, 20L); // runs every second
        checkTasks.put(player.getName(), task);
    }

    public static void stopAutoUpdate(Player player) {
        BukkitRunnable task = checkTasks.remove(player.getName());
        if (task != null) {
            task.cancel();
        }
    }

    public static void updateMenu(Player player, Menu menu) {
        Inventory inv = currentlyOpenedMenus.get(player.getName());

        if (inv == null) return;

        for (Map.Entry<Integer, Button> entry : menu.buttons.entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue().build());
        }

        player.updateInventory();
    }

    public static Menu getPlayerMenu(String playerName) {
        return playerMenus.get(playerName);
    }

}
