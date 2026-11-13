package net.minebo.cobalt.service.visibility;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.visibility.listener.VisibilityListener;
import net.minebo.cobalt.service.visibility.registry.VisibilityProviderRegistry;
import net.minebo.cobalt.service.visibility.state.VisibilityTracker;
import net.minebo.cobalt.service.visibility.task.VisibilityUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class VisibilityService extends CService {

    private VisibilityProviderRegistry registry;
    private VisibilityTracker tracker;
    private VisibilityListener listener;
    private BukkitTask task;

    @Override
    public String getName() {
        return "Visibility";
    }

    @Override
    public void onEnable() {
        registry = new VisibilityProviderRegistry();
        tracker = new VisibilityTracker();
        listener = new VisibilityListener(this);

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

        tracker.snapshot().forEach((viewerId, targets) -> {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer == null) {
                return;
            }
            for (UUID targetId : targets) {
                Player target = Bukkit.getPlayer(targetId);
                if (target != null) {
                    viewer.showPlayer(Cobalt.getInstance(), target);
                }
            }
        });
        tracker.clear();
    }

    /**
     * Providers are asked from highest weight to lowest; the first one that doesn't return
     * NEUTRAL decides. If every provider is NEUTRAL the target is shown.
     */
    public void registerProvider(VisibilityProvider provider, int weight) {
        registry.register(provider, weight);
        refreshAll();
    }

    public void unregisterProvider(VisibilityProvider provider) {
        registry.unregister(provider);
        refreshAll();
    }

    public List<VisibilityProvider> getProviders() {
        return registry.all();
    }

    /** Evaluates the providers without applying anything. */
    public boolean canSee(Player viewer, Player target) {
        if (viewer == null || target == null) {
            return false;
        }
        if (viewer.equals(target)) {
            return true;
        }
        for (VisibilityProvider provider : registry.all()) {
            Visibility visibility;
            try {
                visibility = provider.getVisibility(viewer, target);
            } catch (Exception exception) {
                continue;
            }
            if (visibility == Visibility.SHOW) {
                return true;
            }
            if (visibility == Visibility.HIDE) {
                return false;
            }
        }
        return true;
    }

    /** Whether this service currently has {@code target} hidden from {@code viewer}. */
    public boolean isHidden(Player viewer, Player target) {
        return tracker.isHidden(viewer.getUniqueId(), target.getUniqueId());
    }

    public Set<UUID> getHiddenFrom(Player viewer) {
        return tracker.hiddenFrom(viewer.getUniqueId());
    }

    public void refreshAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                refresh(viewer, target);
            }
        }
    }

    /** Refreshes the player both as a viewer and as a target. */
    public void refresh(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        for (Player other : Bukkit.getOnlinePlayers()) {
            refresh(player, other);
            refresh(other, player);
        }
    }

    public void refresh(Player viewer, Player target) {
        if (viewer == null || target == null || !viewer.isOnline() || !target.isOnline() || viewer.equals(target)) {
            return;
        }

        boolean hide = !canSee(viewer, target);
        if (hide == tracker.isHidden(viewer.getUniqueId(), target.getUniqueId())) {
            return;
        }

        if (hide) {
            viewer.hidePlayer(Cobalt.getInstance(), target);
        } else {
            viewer.showPlayer(Cobalt.getInstance(), target);
        }
        tracker.setHidden(viewer.getUniqueId(), target.getUniqueId(), hide);
    }

    public void handleQuit(Player player) {
        tracker.removeViewer(player.getUniqueId());
    }

    private void startTask() {
        if (task != null) {
            task.cancel();
        }
        task = new VisibilityUpdateTask(this).runTaskTimer(
                Cobalt.getInstance(),
                VisibilityUpdateTask.PERIOD_TICKS,
                VisibilityUpdateTask.PERIOD_TICKS
        );
    }
}
