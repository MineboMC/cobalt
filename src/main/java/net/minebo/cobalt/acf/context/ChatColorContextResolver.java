package net.minebo.cobalt.acf.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import org.bukkit.ChatColor;

public class ChatColorContextResolver implements ContextResolver<ChatColor, BukkitCommandExecutionContext> {

   public ChatColor getContext(BukkitCommandExecutionContext bukkitCommandExecutionContext) throws InvalidCommandArgument {
      return ChatColor.valueOf(bukkitCommandExecutionContext.getFirstArg());
   }

}
