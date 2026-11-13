package net.minebo.cobalt.service.command.defaults.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletions;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.chat.ChatChannel;
import net.minebo.cobalt.service.chat.ChatService;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("channel|ch|chat")
@Description("Switch the chat channel you talk in.")
public final class ChannelCommand extends BaseCommand {

    public static void registerCompletions(BukkitCommandManager manager) {
        BukkitCommandCompletions completions = (BukkitCommandCompletions) manager.getCommandCompletions();

        completions.registerCompletion("chatchannels", c -> {
            ChatService service = CobaltAPI.getChatService();
            if (service == null) {
                return List.of();
            }
            return service.getChannels().stream()
                    .filter(channel -> !(c.getSender() instanceof Player player) || channel.canSpeak(player))
                    .map(ChatChannel::getId)
                    .toList();
        });
    }

    @Default
    @Syntax("[channel]")
    @CommandCompletion("@chatchannels")
    public void onChannel(Player player, @Optional String id) {
        ChatService service = CobaltAPI.getChatService();
        if (service == null || !service.isEnabled()) {
            player.sendMessage(Coloring.toComponent("<red>Chat channels are disabled."));
            return;
        }

        if (id == null) {
            player.sendMessage(Coloring.toComponent("<gray>You are talking in <aqua>" + service.getChannel(player).getDisplayName() + "<gray>."));
            return;
        }

        ChatChannel channel = service.getChannel(id);
        if (channel == null) {
            player.sendMessage(Coloring.toComponent("<red>Unknown channel: " + id));
            return;
        }
        if (!channel.canSpeak(player)) {
            player.sendMessage(Coloring.toComponent("<red>You can't speak in " + channel.getDisplayName() + " chat."));
            return;
        }

        service.setChannel(player, channel);
        player.sendMessage(Coloring.toComponent("<gray>You are now talking in <aqua>" + channel.getDisplayName() + "<gray>."));
    }
}
