package net.minebo.cobalt.cooldown.construct;

import lombok.Getter;
import net.minebo.cobalt.cooldown.CooldownHandler;
import org.apache.commons.lang.time.DurationFormatUtils;
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
     * @param cooldown  Time in seconds.
     */
    public void applyCooldown(Player player, long cooldown, TimeUnit unit, JavaPlugin plugin) {
        UUID uuid = player.getUniqueId();
        long cooldownMillis = unit.toMillis(cooldown);
        long expireTime = System.currentTimeMillis() + (cooldownMillis * 1000);
        cooldownMap.put(uuid, expireTime);

        // Call overridable hook
        onApply(player);

        // Schedule expiration exactly when this cooldown ends
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Check that this is still the same cooldown (hasn't been reapplied with a new time)
            Long storedExpire = cooldownMap.get(uuid);
            if (storedExpire != null && storedExpire == expireTime) {
                removeCooldown(player);
                onExpire(player);
            }
        }, cooldown * 20L); // 20 ticks = 1 second

    }

    public boolean onCooldown(Player player) {
        return cooldownMap.containsKey(player.getUniqueId()) &&
                cooldownMap.get(player.getUniqueId()) >= System.currentTimeMillis();
    }

    public String getRemaining(Player player) {
        long l = cooldownMap.get(player.getUniqueId()) - System.currentTimeMillis();
        if (l > TimeUnit.SECONDS.toMillis(60)) {
            return DurationFormatUtils.formatDuration(l, "H:mm:ss");
        }
        String formatted = DurationFormatUtils.formatDuration(l, "s.S");
        return formatted.substring(0, formatted.length() - 2);
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
