package net.minebo.cobalt.service.prompt;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PromptService extends CService implements Listener {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final String CANCEL_WORD = "cancel";

    private final Map<UUID, ActivePrompt> active = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "Prompt Service";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, Cobalt.getInstance());
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        active.values().forEach(entry -> entry.timeout.cancel());
        active.clear();
    }

    public void ask(Player player, String prompt, Consumer<String> onInput, Runnable onCancel) {
        ask(player, prompt, DEFAULT_TIMEOUT_SECONDS, onInput, onCancel);
    }

    public void ask(Player player, String prompt, int timeoutSeconds, Consumer<String> onInput, Runnable onCancel) {
        UUID id = player.getUniqueId();

        // Starting a new prompt silently replaces any existing one (no callbacks).
        ActivePrompt previous = active.remove(id);
        if (previous != null) {
            previous.timeout.cancel();
        }

        ActivePrompt entry = new ActivePrompt(onInput, onCancel);
        entry.timeout = Bukkit.getScheduler().runTaskLater(Cobalt.getInstance(), () -> {
            if (active.remove(id, entry)) {
                entry.runCancel();
            }
        }, timeoutSeconds * 20L);
        active.put(id, entry);

        player.sendMessage(Coloring.translateColors(prompt));
    }

    public boolean isPrompting(Player player) {
        return active.containsKey(player.getUniqueId());
    }

    /** Cancels the player's active prompt and runs its cancel callback. */
    public boolean cancel(Player player) {
        ActivePrompt entry = active.remove(player.getUniqueId());
        if (entry == null) {
            return false;
        }
        entry.timeout.cancel();
        entry.runCancel();
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        handle(event.getPlayer(),
                PlainTextComponentSerializer.plainText().serialize(event.message()),
                () -> event.setCancelled(true));
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLegacyChat(AsyncPlayerChatEvent event) {
        handle(event.getPlayer(), event.getMessage(), () -> event.setCancelled(true));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ActivePrompt entry = active.remove(event.getPlayer().getUniqueId());
        if (entry != null) {
            entry.timeout.cancel();
        }
    }

    private void handle(Player player, String message, Runnable cancelEvent) {
        UUID id = player.getUniqueId();
        ActivePrompt entry = active.get(id);
        if (entry == null) {
            return;
        }

        cancelEvent.run();

        // If the other chat event for this same message already claimed it, just stay cancelled.
        if (!entry.consumed.compareAndSet(false, true)) {
            return;
        }

        String input = message.trim();

        // Chat events are async; callbacks (menus, NPC calls) need the main thread.
        Bukkit.getScheduler().runTask(Cobalt.getInstance(), () -> {
            if (!active.remove(id, entry)) {
                return;
            }
            entry.timeout.cancel();

            if (input.equalsIgnoreCase(CANCEL_WORD)) {
                entry.runCancel();
            } else {
                entry.onInput.accept(input);
            }
        });
    }

    private static final class ActivePrompt {
        final Consumer<String> onInput;
        final Runnable onCancel;
        final AtomicBoolean consumed = new AtomicBoolean(false);
        volatile BukkitTask timeout;

        ActivePrompt(Consumer<String> onInput, Runnable onCancel) {
            this.onInput = onInput;
            this.onCancel = onCancel;
        }

        void runCancel() {
            if (onCancel != null) {
                onCancel.run();
            }
        }
    }
}