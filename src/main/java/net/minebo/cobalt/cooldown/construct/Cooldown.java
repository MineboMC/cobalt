package net.minebo.cobalt.cooldown.construct;

import lombok.Getter;
import net.minebo.cobalt.cooldown.CooldownHandler;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Cooldown implements Listener {

    @Getter
    private final Map<UUID, Long> cooldownMap = new HashMap<>();
    private final Map<UUID, Runnable> onExpireMap = new HashMap<>();

    /**
     * Applies a cooldown to the player.
     *
     * @param player    The player.
     * @param cooldown  The duration of the cooldown.
     * @param unit      The time unit of the cooldown.
     * @param plugin    The plugin to schedule the task with.
     */
    public void applyCooldown(Player player, long cooldown, TimeUnit unit, JavaPlugin plugin) {
        UUID uuid = player.getUniqueId();

        long millis = unit.toMillis(cooldown);
        long expireTime = System.currentTimeMillis() + millis;

        cooldownMap.put(uuid, expireTime);

        onApply(player);

        // Schedule expiration using proper tick conversion
        long ticks = Math.max(1, millis / 50); // 50ms per tick (1000ms / 20 ticks)

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Long storedExpire = cooldownMap.get(uuid);
            if (storedExpire != null && storedExpire == expireTime) {
                removeCooldown(player);
                onExpire(player);
            }
        }, ticks);
    }

    public boolean onCooldown(Player player) {
        Long expireTime = cooldownMap.get(player.getUniqueId());
        return expireTime != null && expireTime >= System.currentTimeMillis();
    }

    public String getRemaining(Player player) {
        Long expireTime = cooldownMap.get(player.getUniqueId());
        if (expireTime == null) return "0";

        long millisLeft = expireTime - System.currentTimeMillis();
        if (millisLeft <= 0) return "0";

        if (millisLeft >= TimeUnit.MINUTES.toMillis(1)) {
            return DurationFormatUtils.formatDuration(millisLeft, "mm:ss");
        }

        // Show seconds with one decimal for short cooldowns
        double seconds = millisLeft / 1000.0;
        return String.format("%.1fs", seconds);
    }

    public void removeCooldown(Player player) {
        cooldownMap.remove(player.getUniqueId());
        onExpireMap.remove(player.getUniqueId());
    }

    /**
     * Hook: called when a cooldown is applied.
     */
    public void onApply(Player player) {
        // To be overridden
    }

    /**
     * Hook: called when a cooldown expires.
     */
    public void onExpire(Player player) {
        // To be overridden
    }
}