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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("channel|ch|chat")
@Description("Switch the chat channel you talk in, or send one message in it.")
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
    @Syntax("[channel] [message]")
    @CommandCompletion("@chatchannels")
    public void onChannel(Player player, @Optional String name, @Optional String message) {
        ChatService service = CobaltAPI.getChatService();
        if (service == null || !service.isEnabled()) {
            player.sendMessage(Coloring.toComponent("<red>Chat channels are disabled."));
            return;
        }

        if (name == null) {
            ChatChannel current = service.getChannel(player);
            String available = service.getChannels().stream()
                    .filter(channel -> channel.canSpeak(player))
                    .sorted(Comparator.comparing(ChatChannel::getId))
                    .map(channel -> (channel == current ? "<white>> " : "") + channel.getDisplayName())
                    .collect(Collectors.joining("<gray>, "));
            player.sendMessage(Coloring.toComponent("<gray>You are talking in <reset>" + current.getDisplayName() + "<gray>."));
            player.sendMessage(Coloring.toComponent("<gray>Channels: " + available));
            return;
        }

        ChatChannel channel = service.getChannel(name);
        if (channel == null) {
            player.sendMessage(Coloring.toComponent("<red>Unknown channel: " + name));
            return;
        }
        service.toggleOrSend(player, channel, message);
    }
}
