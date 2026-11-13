package net.minebo.cobalt.service.reboot;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.reboot.task.RebootTask;
import net.minebo.cobalt.service.scheduler.TimeFormat;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class RebootService extends CService {

    private static final List<Integer> DEFAULT_WARNINGS = List.of(1800, 900, 600, 300, 120, 60, 30, 15, 10, 5, 4, 3, 2, 1);

    private boolean scheduleEnabled;
    private ZoneId zone;
    private final List<LocalTime> times = new ArrayList<>();
    private final List<Integer> warnings = new ArrayList<>();
    private List<String> warningMessage;
    private List<String> kickMessage;
    private boolean useRestartScript;

    /** Manually scheduled reboot, overrides the configured times while set. */
    private long manualRebootAt = -1L;
    /** Scheduled reboots at or before this instant are skipped (set by cancelling one). */
    private long skipUntil = -1L;

    private long warnedFor = -1L;
    private final Set<Integer> announced = new HashSet<>();
    private boolean rebooting;

    private BukkitTask task;

    @Override
    public String getName() {
        return "Auto Reboot";
    }

    @Override
    public void onEnable() {
        loadConfig();
        task = new RebootTask(this).runTaskTimer(Cobalt.getInstance(), RebootTask.PERIOD_TICKS, RebootTask.PERIOD_TICKS);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void loadConfig() {
        ConfigurationSection config = Cobalt.getInstance().getConfig().getConfigurationSection("reboot");

        times.clear();
        warnings.clear();
        scheduleEnabled = config != null && config.getBoolean("enabled", false);

        String zoneId = config == null ? "" : config.getString("timezone", "");
        try {
            zone = zoneId == null || zoneId.isBlank() ? ZoneId.systemDefault() : ZoneId.of(zoneId);
        } catch (Exception exception) {
            Cobalt.getInstance().getLogger().warning("Invalid reboot timezone '" + zoneId + "', using the system default.");
            zone = ZoneId.systemDefault();
        }

        if (config != null) {
            for (String time : config.getStringList("times")) {
                try {
                    times.add(LocalTime.parse(time.trim()));
                } catch (DateTimeParseException exception) {
                    Cobalt.getInstance().getLogger().warning("Invalid reboot time '" + time + "', expected HH:mm.");
                }
            }
        }

        List<Integer> configured = config == null ? List.of() : config.getIntegerList("warnings");
        warnings.addAll(configured.isEmpty() ? DEFAULT_WARNINGS : configured);

        warningMessage = getLines(config, "warning-message", "&c&lREBOOT &7» &fThe server will reboot in &c<time>&f.");
        kickMessage = getLines(config, "kick-message", "&cThe server is rebooting. Please reconnect shortly.");
        useRestartScript = config != null && config.getBoolean("use-restart-script", false);
    }

    /** A list of lines, or a single string as one line for older configs. */
    private static List<String> getLines(ConfigurationSection config, String path, String fallback) {
        if (config == null || !config.contains(path)) {
            return List.of(fallback);
        }
        if (config.isList(path)) {
            return config.getStringList(path);
        }
        return List.of(config.getString(path, fallback));
    }

    /** Epoch millis of the next reboot, or -1 if none is scheduled. */
    public long getNextReboot() {
        if (manualRebootAt > 0L) {
            return manualRebootAt;
        }
        if (!scheduleEnabled || times.isEmpty()) {
            return -1L;
        }

        ZonedDateTime now = ZonedDateTime.now(zone);
        long next = -1L;
        for (LocalTime time : times) {
            ZonedDateTime candidate = now.with(time);
            if (!candidate.isAfter(now) || candidate.toInstant().toEpochMilli() <= skipUntil) {
                candidate = candidate.plusDays(1);
            }
            long millis = candidate.toInstant().toEpochMilli();
            if (next < 0L || millis < next) {
                next = millis;
            }
        }
        return next;
    }

    /** Millis until the next reboot, or -1 if none is scheduled. */
    public long getTimeUntilReboot() {
        long next = getNextReboot();
        return next < 0L ? -1L : Math.max(0L, next - System.currentTimeMillis());
    }

    public boolean isRebootScheduled() {
        return getNextReboot() > 0L;
    }

    /** Schedules a one-off reboot, replacing any manual reboot already scheduled. */
    public void scheduleReboot(long delay, TimeUnit unit) {
        manualRebootAt = System.currentTimeMillis() + unit.toMillis(delay);
    }

    /** Cancels a manual reboot if one is set, otherwise skips the next configured reboot time. Returns false if nothing was scheduled. */
    public boolean cancelReboot() {
        if (manualRebootAt > 0L) {
            manualRebootAt = -1L;
            return true;
        }
        long next = getNextReboot();
        if (next < 0L) {
            return false;
        }
        skipUntil = next;
        return true;
    }

    public void tick() {
        if (rebooting) {
            return;
        }

        long next = getNextReboot();
        if (next < 0L) {
            return;
        }
        if (next != warnedFor) {
            warnedFor = next;
            announced.clear();
        }

        long remaining = next - System.currentTimeMillis();
        if (remaining <= 0L) {
            reboot();
            return;
        }

        // Catch-up safe: if ticks lag or we start inside the window, every passed warning is
        // marked at once and only one message goes out.
        long secondsLeft = (remaining + 999L) / 1000L;
        boolean announce = false;
        for (int warning : warnings) {
            if (secondsLeft <= warning && announced.add(warning)) {
                announce = true;
            }
        }
        if (announce) {
            String time = TimeFormat.words(secondsLeft * 1000L);
            for (String line : warningMessage) {
                Bukkit.broadcast(Coloring.toComponent(line.replace("<time>", time)));
            }
        }
    }

    /** Kicks everyone and stops the server right now. */
    public void reboot() {
        rebooting = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kick(Coloring.toComponent(String.join("\n", kickMessage)));
        }
        if (useRestartScript) {
            Bukkit.spigot().restart();
        } else {
            Bukkit.shutdown();
        }
    }
}
