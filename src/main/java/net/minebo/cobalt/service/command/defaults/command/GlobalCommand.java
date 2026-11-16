package net.minebo.cobalt.service.command.defaults.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.text.Component;
import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.chat.ChatChannel;
import net.minebo.cobalt.service.chat.ChatService;
import net.minebo.cobalt.service.chat.channel.GlobalChatChannel;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.entity.Player;

@CommandAlias("global|g")
@Description("Switch to global chat, or send one message in it.")
public final class GlobalCommand extends BaseCommand {

    @Default
    @Syntax("[message]")
    public void onGlobal(Player player, @Optional String message) {
        ChatService service = CobaltAPI.getChatService();
        ChatChannel global = service == null ? null : service.getChannel(GlobalChatChannel.ID);
        if (global == null || !service.isEnabled()) {
            player.sendMessage(Coloring.toComponent("<red>Chat channels are disabled."));
            return;
        }
        if (!global.canSpeak(player)) {
            player.sendMessage(Coloring.toComponent("<red>You can't speak in " + global.getDisplayName() + "<red> chat."));
            return;
        }

        if (message != null && !message.isBlank()) {
            service.send(player, global, Component.text(message));
            return;
        }

        service.setChannel(player, global);
        player.sendMessage(Coloring.toComponent("<gray>You are now talking in <reset>" + global.getDisplayName() + "<gray>."));
    }
}
