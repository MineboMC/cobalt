package net.minebo.cobalt.service.scoreboard;

import net.kyori.adventure.text.Component;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.scoreboard.listener.ScoreboardListener;
import net.minebo.cobalt.service.scoreboard.packet.ScoreboardPacketSender;
import net.minebo.cobalt.service.scoreboard.registry.ScoreboardProviderRegistry;
import net.minebo.cobalt.service.scoreboard.state.PlayerScoreboardState;
import net.minebo.cobalt.service.scoreboard.state.ScoreboardTracker;
import net.minebo.cobalt.service.scoreboard.task.ScoreboardUpdateTask;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ScoreboardService extends CService {

    private ScoreboardProviderRegistry registry;
    private ScoreboardTracker tracker;
    private ScoreboardPacketSender packets;
    private ScoreboardListener bukkitListener;
    private BukkitTask task;

    @Override
    public String getName() {
        return "Scoreboards";
    }

    @Override
    public void onEnable() {
        registry = new ScoreboardProviderRegistry();
        tracker = new ScoreboardTracker();
        packets = new ScoreboardPacketSender();
        bukkitListener = new ScoreboardListener(this);

        Bukkit.getPluginManager().registerEvents(bukkitListener, Cobalt.getInstance());
        startTask();

        for (Player player : Bukkit.getOnlinePlayers()) {
            refresh(player);
        }
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        HandlerList.unregisterAll(bukkitListener);
        for (Player player : Bukkit.getOnlinePlayers()) {
            clear(player);
        }
        tracker.clear();
    }

    public void registerProvider(ScoreboardProvider provider, int weight) {
        registry.register(provider, weight);
        refreshAll();
    }

    public void unregisterProvider(ScoreboardProvider provider) {
        registry.unregister(provider);
        refreshAll();
    }

    public List<ScoreboardProvider> getProviders() {
        return registry.all();
    }

    public Optional<ScoreboardProvider> getActiveProvider() {
        return Optional.ofNullable(registry.select());
    }

    public void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refresh(player);
        }
    }

    public void refresh(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        ScoreboardProvider provider = registry.select();
        if (provider == null) {
            clear(player);
            return;
        }

        String rawTitle;
        List<String> rawLines;
        try {
            rawTitle = provider.getTitle(player);
            rawLines = provider.getLines(player);
        } catch (Exception exception) {
            Cobalt.getInstance().getLogger().warning("[Scoreboard] Provider " + provider.getClass().getSimpleName()
                    + " failed for " + player.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
            clear(player);
            return;
        }

        if (rawLines == null || rawLines.isEmpty()) {
            clear(player);
            return;
        }

        Component title = parse(rawTitle);
        List<Component> lines = parseLines(rawLines);

        PlayerScoreboardState state = tracker.getOrCreate(player);
        if (!state.isCreated()) {
            packets.create(player, title);
            state.setCreated(true);
            state.setTitle(title);
            packets.setLines(player, state, lines);
            state.setLines(lines);
            return;
        }

        if (!state.titleEquals(title)) {
            packets.updateTitle(player, title);
            state.setTitle(title);
        }
        if (!state.linesEqual(lines)) {
            packets.setLines(player, state, lines);
            state.setLines(lines);
        }
    }

    public void clear(Player player) {
        PlayerScoreboardState state = tracker.get(player).orElse(null);
        if (state == null) {
            return;
        }
        if (player.isOnline() && state.isCreated()) {
            packets.destroy(player, state);
        }
        state.setCreated(false);
        state.setTitle(Component.empty());
        state.setLines(List.of());
    }

    public void handleQuit(Player player) {
        tracker.remove(player);
    }

    private static List<Component> parseLines(List<String> raw) {
        int count = Math.min(raw.size(), PlayerScoreboardState.MAX_LINES);
        List<Component> lines = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            lines.add(parse(raw.get(index)));
        }
        return lines;
    }

    /**
     * Supports legacy (&) codes and MiniMessage tags in the same string.
     */
    private static Component parse(String text) {
        return Coloring.toComponent(text);
    }

    private void startTask() {
        if (task != null) {
            task.cancel();
        }
        task = new ScoreboardUpdateTask(this).runTaskTimer(
                Cobalt.getInstance(),
                ScoreboardUpdateTask.PERIOD_TICKS,
                ScoreboardUpdateTask.PERIOD_TICKS
        );
    }
}