package net.minebo.cobalt.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class WorldUtils {

    public static World loadWorld(JavaPlugin plugin, String worldName) {

        // Check if already loaded
        World existing = Bukkit.getWorld(worldName);
        if (existing != null) {
            return existing;
        }

        // Check if folder exists
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            plugin.getLogger().warning("World folder '" + worldName + "' not found. Generating a new one.");
            World world;
        }

        plugin.getLogger().info("Preparing to load world '" + worldName + "'...");

        try {
            World world = Bukkit.createWorld(new WorldCreator(worldName));
            plugin.getLogger().info("World '" + worldName + "' loaded successfully.");
            return world;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load world '" + worldName + "': " + e.getMessage());
            return null;
        }
    }

    public static CompletableFuture<World> loadWorldAsync(JavaPlugin plugin, String worldName) {
        CompletableFuture<World> future = new CompletableFuture<>();

        // Check if already loaded
        World existing = Bukkit.getWorld(worldName);
        if (existing != null) {
            future.complete(existing);
            return future;
        }

        // Check if folder exists
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            plugin.getLogger().warning("World folder '" + worldName + "' not found. Generating a new one.");
            World world = Bukkit.createWorld(new WorldCreator(worldName));
            future.complete(world);
            return future;
        }

        // Run file I/O off the main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Simulate any pre-loading checks or file operations here
            plugin.getLogger().info("Preparing to load world '" + worldName + "'...");

            // Switch back to main thread for actual world creation
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    World world = Bukkit.createWorld(new WorldCreator(worldName));
                    future.complete(world);
                    plugin.getLogger().info("World '" + worldName + "' loaded successfully.");
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to load world '" + worldName + "': " + e.getMessage());
                    future.completeExceptionally(e);
                }
            });
        });

        return future;
    }
}
