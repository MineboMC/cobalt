package net.minebo.cobalt.service.actionbar;

import net.kyori.adventure.text.Component;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.actionbar.listener.ActionBarListener;
import net.minebo.cobalt.service.actionbar.registry.ActionBarProviderRegistry;
import net.minebo.cobalt.service.actionbar.state.ActionBarTracker;
import net.minebo.cobalt.service.actionbar.state.PlayerActionBarState;
import net.minebo.cobalt.service.actionbar.task.ActionBarUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class ActionBarService extends CService {

    /** The client fades the action bar out after ~3 seconds, so unchanged text is resent before then. */
    public static final long RESEND_MILLIS = 2_000L;
    public static final long DEFAULT_TEMPORARY_SECONDS = 5L;

    private ActionBarProviderRegistry registry;
    private ActionBarTracker tracker;
    private ActionBarListener listener;
    private BukkitTask task;

    @Override
    public String getName() {
        return "Action Bars";
    }

    @Override
    public void onEnable() {
        registry = new ActionBarProviderRegistry();
        tracker = new ActionBarTracker();
        listener = new ActionBarListener(this);

        Bukkit.getPluginManager().registerEvents(listener, Cobalt.getInstance());
        startTask();
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        HandlerList.unregisterAll(listener);

        for (Player player : Bukkit.getOnlinePlayers()) {
            hide(player);
        }
        tracker.clear();
    }

    public void registerProvider(ActionBarProvider provider, int weight) {
        registry.register(provider, weight);
        refreshAll();
    }

    public void unregisterProvider(ActionBarProvider provider) {
        registry.unregister(provider);
        refreshAll();
    }

    public List<ActionBarProvider> getProviders() {
        return registry.all();
    }

    public Optional<ActionBarProvider> getActiveProvider() {
        return Optional.ofNullable(registry.select());
    }

    /** Shows a message for the default duration, then falls back to the active provider's text. */
    public void sendTemporary(Player player, Component message) {
        sendTemporary(player, message, DEFAULT_TEMPORARY_SECONDS, TimeUnit.SECONDS);
    }

    /** Shows a message for the given duration, then falls back to the active provider's text. Replaces any current temporary message. */
    public void sendTemporary(Player player, Component message, long duration, TimeUnit unit) {
        if (player == null || !player.isOnline() || message == null) {
            return;
        }
        tracker.getOrCreate(player).setTemporary(message, System.currentTimeMillis() + unit.toMillis(duration));
        refresh(player);
    }

    public void clearTemporary(Player player) {
        if (player == null) {
            return;
        }
        tracker.get(player).ifPresent(state -> {
            state.clearTemporary();
            refresh(player);
        });
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

        long now = System.currentTimeMillis();
        PlayerActionBarState state = tracker.getOrCreate(player);

        Component next = state.activeTemporary(now);
        if (next == null) {
            next = providerText(player);
        }

        if (next == null || next.equals(Component.empty())) {
            if (state.isShowing()) {
                player.sendActionBar(Component.empty());
                state.markSent(null, now);
            }
            return;
        }

        if (!next.equals(state.lastSent()) || now - state.lastSentAt() >= RESEND_MILLIS) {
            player.sendActionBar(next);
            state.markSent(next, now);
        }
    }

    public void hide(Player player) {
        if (player == null) {
            return;
        }

        PlayerActionBarState state = tracker.remove(player);
        if (state != null && state.isShowing() && player.isOnline()) {
            player.sendActionBar(Component.empty());
        }
    }

    public void handleQuit(Player player) {
        tracker.remove(player);
    }

    private Component providerText(Player player) {
        ActionBarProvider provider = registry.select();
        if (provider == null) {
            return null;
        }
        try {
            return provider.getText(player);
        } catch (Exception exception) {
            return null;
        }
    }

    private void startTask() {
        if (task != null) {
            task.cancel();
        }
        task = new ActionBarUpdateTask(this).runTaskTimer(
                Cobalt.getInstance(),
                ActionBarUpdateTask.PERIOD_TICKS,
                ActionBarUpdateTask.PERIOD_TICKS
        );
    }
}
