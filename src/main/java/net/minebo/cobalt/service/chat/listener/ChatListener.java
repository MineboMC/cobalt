package net.minebo.cobalt.service.chat.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minebo.cobalt.service.chat.ChatChannel;
import net.minebo.cobalt.service.chat.ChatService;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Paper fires the legacy chat event before the modern one, so the channel is picked there: a channel that
 * delivers its own messages (to other servers, say) gets them before any other plugin's legacy listener can
 * take the message over. Local channels are delivered on the modern event, which keeps signed chat working.
 */
public final class ChatListener implements Listener {

    private final ChatService service;
    /** Local messages picked on the legacy event, waiting for the modern one. */
    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();

    public ChatListener(ChatService service) {
        this.service = service;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onLegacyChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        pending.remove(sender.getUniqueId());

        String text = event.getMessage();
        ChatChannel channel = service.getChannel(sender);
        ChatChannel prefixed = service.getChannelByPrefix(sender, text);
        if (prefixed != null) {
            channel = prefixed;
            text = text.substring(prefixed.getPrefix().length()).stripLeading();
            if (text.isBlank()) {
                event.setCancelled(true);
                return;
            }
            event.setMessage(text);
        }
        if (channel == null) {
            return;
        }
        if (!canSpeak(sender, channel)) {
            event.setCancelled(true);
            return;
        }

        Component original = Component.text(text);
        Component message = service.approve(sender, channel, original);
        if (message == null) {
            event.setCancelled(true);
            return;
        }

        if (channel.dispatch(sender, message)) {
            event.setCancelled(true);
            return;
        }
        pending.put(sender.getUniqueId(), new Pending(channel, message == original ? null : message));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        Pending picked = pending.remove(sender.getUniqueId());

        ChatChannel channel;
        if (picked != null) {
            channel = picked.channel();
            if (picked.message() != null) {
                event.message(picked.message());
            }
        } else {
            // the legacy event didn't run for this message, so pick the channel here
            channel = pickWithoutLegacy(event, sender);
            if (channel == null) {
                return;
            }
        }

        ChatChannel target = channel;
        event.viewers().removeIf(audience -> audience instanceof Player receiver
                && !receiver.equals(sender)
                && !target.canReceive(sender, receiver));
        event.renderer((source, displayName, rendered, viewer) ->
                target.format(source, viewer instanceof Player player ? player : null, rendered));
    }

    private ChatChannel pickWithoutLegacy(AsyncChatEvent event, Player sender) {
        Component message = event.message();
        ChatChannel channel = service.getChannel(sender);
        String plain = PlainTextComponentSerializer.plainText().serialize(message);
        ChatChannel prefixed = service.getChannelByPrefix(sender, plain);
        if (prefixed != null) {
            channel = prefixed;
            message = message.replaceText(TextReplacementConfig.builder()
                    .match(Pattern.compile(Pattern.quote(prefixed.getPrefix()) + "\\s*"))
                    .once()
                    .replacement("")
                    .build());
        }
        if (channel == null) {
            return null;
        }
        if (PlainTextComponentSerializer.plainText().serialize(message).isBlank() || !canSpeak(sender, channel)) {
            event.setCancelled(true);
            return null;
        }

        Component approved = service.approve(sender, channel, message);
        if (approved == null) {
            event.setCancelled(true);
            return null;
        }
        if (channel.dispatch(sender, approved)) {
            event.setCancelled(true);
            return null;
        }
        event.message(approved);
        return channel;
    }

    /** Only the default channel can get here without the sender being allowed to speak in it. */
    private boolean canSpeak(Player sender, ChatChannel channel) {
        if (channel.canSpeak(sender)) {
            return true;
        }
        sender.sendMessage(Coloring.toComponent("<red>You can't speak in " + channel.getDisplayName() + "<red> chat."));
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
        service.handleQuit(event.getPlayer());
    }

    private record Pending(ChatChannel channel, Component message) {
    }
}
