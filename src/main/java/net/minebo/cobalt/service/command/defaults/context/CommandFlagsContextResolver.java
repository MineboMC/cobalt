package net.minebo.cobalt.service.command.defaults.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import net.minebo.cobalt.service.command.flag.CommandFlags;

public class CommandFlagsContextResolver implements ContextResolver<CommandFlags, BukkitCommandExecutionContext> {
   public CommandFlags getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
      String raw = context.joinArgs();
      context.getArgs().clear();
      return CommandFlags.parse(raw, context.getFlags().keySet());
   }
}
