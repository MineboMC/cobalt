package net.minebo.cobalt.service.reboot;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.bossbar.BossBarService;
import net.minebo.cobalt.service.reboot.provider.RebootBossBarProvider;
import net.minebo.cobalt.service.reboot.task.RebootTask;
import net.minebo.cobalt.service.scheduler.TimeFormat;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
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

    /** Seconds before a reboot at which a chat announcement goes out. */
    private static final List<Integer> WARNINGS = List.of(1800, 900, 600, 300, 60, 30, 10);
    /** The bossbar countdown shows up once the reboot is this close. */
    public static final long BOSSBAR_WINDOW = TimeUnit.MINUTES.toMillis(30);

    private static final String TITLE_COLOR = "<#FF6347>";
    private static final String TEXT_COLOR = "<#E0E0E0>";

    private ZoneId zone;
    private final List<LocalTime> times = new ArrayList<>();
    private List<String> kickMessage;

    /** Next reboot from the configured times, locked in until it happens or is skipped. */
    private long scheduledAt = -1L;
    /** Manually scheduled reboot, overrides the configured times while set. */
    private long manualRebootAt = -1L;

    private long warnedFor = -1L;
    private final Set<Integer> announced = new HashSet<>();
    private boolean rebooting;

    private final RebootBossBarProvider bossBar = new RebootBossBarProvider(this);
    private boolean bossBarShown;

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
        setBossBarShown(false);
    }

    public void loadConfig() {
        ConfigurationSection config = Cobalt.getInstance().getConfig().getConfigurationSection("reboot");

        times.clear();

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

        kickMessage = getLines(config, "kick-message", "&cThe server is rebooting. Please reconnect shortly.");
        scheduledAt = nextConfiguredAfter(System.currentTimeMillis());
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

    /** Epoch millis of the first configured time strictly after the given instant, or -1 if there are no times. */
    private long nextConfiguredAfter(long after) {
        ZonedDateTime from = Instant.ofEpochMilli(after).atZone(zone);
        long next = -1L;
        for (LocalTime time : times) {
            ZonedDateTime candidate = from.with(time);
            if (!candidate.isAfter(from)) {
                candidate = candidate.plusDays(1);
            }
            long millis = candidate.toInstant().toEpochMilli();
            if (next < 0L || millis < next) {
                next = millis;
            }
        }
        return next;
    }

    /** Epoch millis of the next reboot, or -1 if none is scheduled. */
    public long getNextReboot() {
        return manualRebootAt > 0L ? manualRebootAt : scheduledAt;
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
        if (scheduledAt < 0L) {
            return false;
        }
        scheduledAt = nextConfiguredAfter(scheduledAt);
        return true;
    }

    public void tick() {
        if (rebooting) {
            return;
        }

        long next = getNextReboot();
        if (next < 0L) {
            setBossBarShown(false);
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

        setBossBarShown(remaining <= BOSSBAR_WINDOW);

        // Catch-up safe: if ticks lag or we start inside the window, every passed warning is
        // marked at once and only one message goes out.
        long secondsLeft = (remaining + 999L) / 1000L;
        boolean announce = false;
        for (int warning : WARNINGS) {
            if (secondsLeft <= warning && announced.add(warning)) {
                announce = true;
            }
        }
        if (announce) {
            announce(secondsLeft * 1000L);
        }
    }

    private void announce(long remaining) {
        Bukkit.broadcast(Coloring.toComponent(""));
        Bukkit.broadcast(Coloring.toComponent(TITLE_COLOR + "<bold>Server Reboot"));
        Bukkit.broadcast(Coloring.toComponent("<gray>The server will reboot in <underlined>" + TEXT_COLOR + TimeFormat.words(remaining) + "</underlined><gray>."));
        Bukkit.broadcast(Coloring.toComponent(""));
    }

    private void setBossBarShown(boolean shown) {
        BossBarService bossBars = CobaltAPI.getBossBarService();
        if (bossBars == null || shown == bossBarShown) {
            return;
        }
        bossBarShown = shown;
        if (shown) {
            bossBars.registerProvider(bossBar, Integer.MAX_VALUE);
        } else {
            bossBars.unregisterProvider(bossBar);
        }
    }

    /** Kicks everyone and restarts the server right now. */
    public void reboot() {
        if (rebooting) {
            return;
        }
        rebooting = true;
        setBossBarShown(false);

        Cobalt.getInstance().getLogger().info("Rebooting the server.");
        Bukkit.savePlayers();
        for (Player player : List.copyOf(Bukkit.getOnlinePlayers())) {
            player.kick(Coloring.toComponent(String.join("\n", kickMessage)));
        }

        // Runs settings.restart-script from spigot.yml so the server comes back up;
        // without that script it just stops, leaving it to the host to start it again.
        Bukkit.restart();
    }
}
