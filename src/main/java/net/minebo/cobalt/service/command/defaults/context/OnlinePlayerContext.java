package net.minebo.cobalt.service.command.defaults.context;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.minebo.cobalt.service.command.CommonCommandContext;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

/** Vanished players count as offline. */
public class OnlinePlayerContext extends CommonCommandContext<OnlinePlayer> {

    public OnlinePlayerContext() {
        super("players", OnlinePlayer.class);
    }

    @Override
    public OnlinePlayer getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        String input = context.popFirstArg();
        Player target = Bukkit.getPlayerExact(input);
        if (target == null || target.hasMetadata("vanished")) {
            throw new InvalidCommandArgument(Coloring.translateColors("<red>" + input + " is not online."));
        }
        return new OnlinePlayer(target);
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> !player.hasMetadata("vanished"))
                .map(Player::getName)
                .toList();
    }
}
