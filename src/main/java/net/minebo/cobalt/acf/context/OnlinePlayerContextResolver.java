package net.minebo.cobalt.acf.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import co.aikar.commands.contexts.ContextResolver;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class OnlinePlayerContextResolver implements ContextResolver<OnlinePlayer, BukkitCommandExecutionContext> {

   public OnlinePlayer getContext(BukkitCommandExecutionContext c) throws InvalidCommandArgument {
      String input = c.popFirstArg();
      Player target = Bukkit.getPlayerExact(input);

      if (target != null) {
         if (!target.hasMetadata("vanished")) {
            return new OnlinePlayer(target);
         } else {
            throw new InvalidCommandArgument(ColorUtil.translateColors("<red>" + input + " is not online."));
         }
      } else {
         throw new InvalidCommandArgument(ColorUtil.translateColors("<red>" + input + " is not online."));
      }
   }
}