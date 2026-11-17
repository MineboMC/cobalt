package net.minebo.cobalt.service.command.defaults.context;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.minebo.cobalt.service.command.CommonCommandContext;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.Collection;

public class ChatColorContext extends CommonCommandContext<ChatColor> {

    public ChatColorContext() {
        super("chatcolors", ChatColor.class);
    }

    @Override
    public ChatColor getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        return ChatColor.valueOf(context.getFirstArg());
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) {
        return Arrays.stream(ChatColor.values()).map(Enum::name).toList();
    }
}
