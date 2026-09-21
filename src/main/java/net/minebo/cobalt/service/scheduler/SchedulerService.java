package net.minebo.cobalt.service.scheduler;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.scheduler.cooldown.Cooldown;
import net.minebo.cobalt.service.scheduler.cooldown.CooldownEntry;
import net.minebo.cobalt.service.scheduler.cooldown.CooldownRegistry;
import net.minebo.cobalt.service.scheduler.listener.SchedulerListener;
import net.minebo.cobalt.service.scheduler.lunar.LunarCooldownBridge;
import net.minebo.cobalt.service.scheduler.task.SchedulerTickTask;
import net.minebo.cobalt.service.scheduler.timer.ScheduledTimer;
import net.minebo.cobalt.service.scheduler.timer.TimerEntry;
import net.minebo.cobalt.service.scheduler.timer.TimerRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class SchedulerService extends CService {

    private CooldownRegistry cooldowns;
    private TimerRegistry timers;
    private SchedulerListener bukkitListener;
    private BukkitTask task;

    private final Map<UUID, Map<String, CooldownEntry>> playerCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, TimerEntry>> playerTimers = new ConcurrentHashMap<>();
    private final Map<String, TimerEntry> globalTimers = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "Scheduler";
    }

    @Override
    public void onEnable() {
        cooldowns = new CooldownRegistry();
        timers = new TimerRegistry();
        bukkitListener = new SchedulerListener(this);
        Bukkit.getPluginManager().registerEvents(bukkitListener, Cobalt.getInstance());
        task = new SchedulerTickTask(this).runTaskTimer(
                Cobalt.getInstance(),
                SchedulerTickTask.PERIOD_TICKS,
                SchedulerTickTask.PERIOD_TICKS
        );
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        HandlerList.unregisterAll(bukkitListener);
        for (Cooldown cooldown : cooldowns.all()) {
            HandlerList.unregisterAll(cooldown);
        }
        for (ScheduledTimer timer : timers.all()) {
            HandlerList.unregisterAll(timer);
        }
        playerCooldowns.clear();
        playerTimers.clear();
        globalTimers.clear();
    }

    public void register(Cooldown cooldown) {
        cooldown.attach(this);
        cooldowns.register(cooldown);
        Bukkit.getPluginManager().registerEvents(cooldown, Cobalt.getInstance());
    }

    public void register(ScheduledTimer timer) {
        timer.attach(this);
        timers.register(timer);
        Bukkit.getPluginManager().registerEvents(timer, Cobalt.getInstance());
    }

    public Cooldown getCooldown(String id) {
        return cooldowns.get(id);
    }

    public ScheduledTimer getTimer(String id) {
        return timers.get(id);
    }

    public void applyCooldown(Player player, Cooldown cooldown, long time, TimeUnit unit) {
        if (player == null || cooldown == null || unit == null || time <= 0L) {
            return;
        }
        long duration = unit.toMillis(time);
        long expireAt = System.currentTimeMillis() + duration;
        playerCooldowns
                .computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>())
                .put(cooldown.getId(), new CooldownEntry(player.getUniqueId(), cooldown.getId(), expireAt));

        Material item = cooldown.getItem();
        if (item != null) {
            int ticks = (int) Math.max(1L, duration / 50L);
            player.setCooldown(item, ticks);
        }
        LunarCooldownBridge.send(player, cooldown.getId(), item, duration);
        cooldown.onApply(player);
    }

    public void applyCooldown(Player player, String cooldownId, long time, TimeUnit unit) {
        Cooldown cooldown = cooldowns.get(cooldownId);
        if (cooldown != null) {
            applyCooldown(player, cooldown, time, unit);
        }
    }

    public boolean onCooldown(Player player, Cooldown cooldown) {
        return cooldown != null && onCooldown(player, cooldown.getId());
    }

    public boolean onCooldown(Player player, String cooldownId) {
        if (player == null || cooldownId == null) {
            return false;
        }
        CooldownEntry entry = getCooldownEntry(player.getUniqueId(), cooldownId);
        return entry != null && !entry.expired();
    }

    public long getRemaining(Player player, Cooldown cooldown) {
        return cooldown == null ? 0L : getRemaining(player, cooldown.getId());
    }

    public long getRemaining(Player player, String cooldownId) {
        if (player == null || cooldownId == null) {
            return 0L;
        }
        CooldownEntry entry = getCooldownEntry(player.getUniqueId(), cooldownId);
        return entry == null ? 0L : entry.remainingMillis();
    }

    public void removeCooldown(Player player, Cooldown cooldown) {
        if (cooldown != null) {
            removeCooldown(player, cooldown.getId());
        }
    }

    public void removeCooldown(Player player, String cooldownId) {
        if (player == null || cooldownId == null) {
            return;
        }
        Map<String, CooldownEntry> map = playerCooldowns.get(player.getUniqueId());
        if (map == null) {
            return;
        }
        CooldownEntry removed = map.remove(cooldownId);
        if (removed == null) {
            return;
        }
        Cooldown cooldown = cooldowns.get(cooldownId);
        Material item = cooldown == null ? null : cooldown.getItem();
        if (item != null) {
            player.setCooldown(item, 0);
        }
        LunarCooldownBridge.clear(player, cooldownId);
    }

    public void startTimer(Player player, ScheduledTimer timer, long time, TimeUnit unit) {
        if (timer == null || unit == null || time <= 0L) {
            return;
        }
        if (timer.isGlobal()) {
            startGlobalTimer(timer, time, unit);
            return;
        }
        if (player == null) {
            return;
        }
        long expireAt = System.currentTimeMillis() + unit.toMillis(time);
        playerTimers
                .computeIfAbsent(player.getUniqueId(), ignored -> new ConcurrentHashMap<>())
                .put(timer.getId(), new TimerEntry(timer.getId(), player.getUniqueId(), expireAt, false));
        timer.onStart(player);
    }

    public void startTimer(Player player, String timerId, long time, TimeUnit unit) {
        ScheduledTimer timer = timers.get(timerId);
        if (timer != null) {
            startTimer(player, timer, time, unit);
        }
    }

    public void startGlobalTimer(ScheduledTimer timer, long time, TimeUnit unit) {
        if (timer == null || unit == null || time <= 0L) {
            return;
        }
        long expireAt = System.currentTimeMillis() + unit.toMillis(time);
        globalTimers.put(timer.getId(), new TimerEntry(timer.getId(), null, expireAt, true));
        timer.onStart(null);
    }

    public boolean isTimerActive(Player player, ScheduledTimer timer) {
        return timer != null && isTimerActive(player, timer.getId());
    }

    public boolean isTimerActive(Player player, String timerId) {
        TimerEntry entry = getTimerEntry(player, timerId);
        return entry != null && !entry.expired();
    }

    public boolean isCooldownActive(Player player, CooldownEntry cooldown) {
        return cooldown != null && isCooldownActive(player, cooldown.cooldownId());
    }

    public boolean isCooldownActive(Player player, String cooldownId) {
        CooldownEntry entry = getCooldownEntry(player.getUniqueId(), cooldownId);
        return entry != null && !entry.expired();
    }

    public long getTimerRemaining(Player player, ScheduledTimer timer) {
        return timer == null ? 0L : getTimerRemaining(player, timer.getId());
    }

    public long getTimerRemaining(Player player, String timerId) {
        TimerEntry entry = getTimerEntry(player, timerId);
        return entry == null ? 0L : entry.remainingMillis();
    }

    public void cancelTimer(Player player, ScheduledTimer timer) {
        if (timer != null) {
            cancelTimer(player, timer.getId());
        }
    }

    public void cancelTimer(Player player, String timerId) {
        ScheduledTimer timer = timers.get(timerId);
        if (timer != null && timer.isGlobal()) {
            TimerEntry removed = globalTimers.remove(timerId);
            if (removed != null) {
                timer.onCancel(null);
            }
            return;
        }
        if (player == null) {
            return;
        }
        Map<String, TimerEntry> map = playerTimers.get(player.getUniqueId());
        if (map == null) {
            return;
        }
        TimerEntry removed = map.remove(timerId);
        if (removed != null && timer != null) {
            timer.onCancel(player);
        }
    }

    public void handleQuit(Player player) {
        UUID id = player.getUniqueId();
        Map<String, CooldownEntry> cooldownMap = playerCooldowns.get(id);
        if (cooldownMap != null) {
            for (String cooldownId : cooldownMap.keySet()) {
                LunarCooldownBridge.clear(player, cooldownId);
            }
        }
        playerCooldowns.remove(id);

        Map<String, TimerEntry> timerMap = playerTimers.remove(id);
        if (timerMap != null) {
            for (String timerId : timerMap.keySet()) {
                ScheduledTimer timer = timers.get(timerId);
                if (timer != null) {
                    timer.onCancel(player);
                }
            }
        }
    }

    public void tick() {
        long now = System.currentTimeMillis();
        tickCooldowns(now);
        tickPlayerTimers(now);
        tickGlobalTimers(now);
    }

    private void tickCooldowns(long now) {
        for (Map.Entry<UUID, Map<String, CooldownEntry>> playerEntry : playerCooldowns.entrySet()) {
            Player player = Bukkit.getPlayer(playerEntry.getKey());
            Iterator<Map.Entry<String, CooldownEntry>> iterator = playerEntry.getValue().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CooldownEntry> entry = iterator.next();
                if (entry.getValue().expireAt() > now) {
                    continue;
                }
                iterator.remove();
                Cooldown cooldown = cooldowns.get(entry.getKey());
                if (player != null && player.isOnline()) {
                    if (cooldown != null && cooldown.getItem() != null) {
                        player.setCooldown(cooldown.getItem(), 0);
                    }
                    LunarCooldownBridge.clear(player, entry.getKey());
                    if (cooldown != null) {
                        cooldown.onExpire(player);
                    }
                }
            }
        }
    }

    private void tickPlayerTimers(long now) {
        for (Map.Entry<UUID, Map<String, TimerEntry>> playerEntry : playerTimers.entrySet()) {
            Player player = Bukkit.getPlayer(playerEntry.getKey());
            Iterator<Map.Entry<String, TimerEntry>> iterator = playerEntry.getValue().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, TimerEntry> entry = iterator.next();
                ScheduledTimer timer = timers.get(entry.getKey());
                TimerEntry value = entry.getValue();
                if (value.expireAt() > now) {
                    if (timer != null) {
                        timer.onTick(player, value.remainingMillis());
                    }
                    continue;
                }
                iterator.remove();
                if (timer != null) {
                    timer.onComplete(player);
                }
            }
        }
    }

    private void tickGlobalTimers(long now) {
        Iterator<Map.Entry<String, TimerEntry>> iterator = globalTimers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TimerEntry> entry = iterator.next();
            ScheduledTimer timer = timers.get(entry.getKey());
            TimerEntry value = entry.getValue();
            if (value.expireAt() > now) {
                if (timer != null) {
                    timer.onTick(null, value.remainingMillis());
                }
                continue;
            }
            iterator.remove();
            if (timer != null) {
                timer.onComplete(null);
            }
        }
    }

    private CooldownEntry getCooldownEntry(UUID playerId, String cooldownId) {
        Map<String, CooldownEntry> map = playerCooldowns.get(playerId);
        if (map == null) {
            return null;
        }
        CooldownEntry entry = map.get(cooldownId);
        if (entry == null || entry.expired()) {
            return null;
        }
        return entry;
    }

    private TimerEntry getTimerEntry(Player player, String timerId) {
        ScheduledTimer timer = timers.get(timerId);
        if (timer != null && timer.isGlobal()) {
            TimerEntry entry = globalTimers.get(timerId);
            return entry == null || entry.expired() ? null : entry;
        }
        if (player == null) {
            return null;
        }
        Map<String, TimerEntry> map = playerTimers.get(player.getUniqueId());
        if (map == null) {
            return null;
        }
        TimerEntry entry = map.get(timerId);
        return entry == null || entry.expired() ? null : entry;
    }
}