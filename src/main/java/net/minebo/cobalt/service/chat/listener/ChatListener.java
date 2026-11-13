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
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.regex.Pattern;

public final class ChatListener implements Listener {

    private final ChatService service;

    public ChatListener(ChatService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        Component message = event.message();

        ChatChannel channel = service.getChannel(sender);
        ChatChannel prefixed = service.getChannelByPrefix(PlainTextComponentSerializer.plainText().serialize(message));
        if (prefixed != null) {
            channel = prefixed;
            message = message.replaceText(TextReplacementConfig.builder()
                    .match(Pattern.compile(Pattern.quote(prefixed.getPrefix()) + "\\s*"))
                    .once()
                    .replacement("")
                    .build());
            if (PlainTextComponentSerializer.plainText().serialize(message).isBlank()) {
                event.setCancelled(true);
                return;
            }
            event.message(message);
        }

        if (channel == null) {
            return;
        }
        if (!channel.canSpeak(sender)) {
            event.setCancelled(true);
            sender.sendMessage(Coloring.toComponent("<red>You can't speak in " + channel.getDisplayName() + " chat."));
            return;
        }

        ChatChannel target = channel;
        event.viewers().removeIf(audience -> audience instanceof Player receiver
                && !receiver.equals(sender)
                && !target.canReceive(sender, receiver));
        event.renderer((source, displayName, rendered, viewer) ->
                target.format(source, viewer instanceof Player player ? player : null, rendered));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        service.handleQuit(event.getPlayer());
    }
}
