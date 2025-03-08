package net.minebo.cobalt.acf.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import co.aikar.commands.contexts.ContextResolver;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class OnlinePlayerContextResolver implements ContextResolver<OnlinePlayer, BukkitCommandExecutionContext> {
   public OnlinePlayer getContext(BukkitCommandExecutionContext c) {
      String input = c.popFirstArg();
      Player target = Bukkit.getPlayerExact(input);
      if (target != null) {
         if (!target.hasMetadata("vanished")) {
            return new OnlinePlayer(target);
         } else {
            String var4 = String.valueOf(ChatColor.RED);
            throw new InvalidCommandArgument(var4 + input + " is not online.");
         }
      } else {
         String var10002 = String.valueOf(ChatColor.RED);
         throw new InvalidCommandArgument(var10002 + input + " is not online.");
      }
   }
}
