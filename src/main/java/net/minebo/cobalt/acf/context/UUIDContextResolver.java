package net.minebo.cobalt.acf.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import java.util.UUID;

public class UUIDContextResolver implements ContextResolver<UUID, BukkitCommandExecutionContext> {
   public UUID getContext(BukkitCommandExecutionContext bukkitCommandExecutionContext) throws InvalidCommandArgument {
      return UUID.fromString(bukkitCommandExecutionContext.getFirstArg());
   }
}
