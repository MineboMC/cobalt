package net.minebo.cobalt.acf.completion;

import co.aikar.commands.CommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.InvalidCommandArgument;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;

public class PlayerCompletionHandler implements CommandCompletions.CommandCompletionHandler {
   public Collection getCompletions(CommandCompletionContext context) throws InvalidCommandArgument {
      List<String> completions = new ArrayList();
      Bukkit.getOnlinePlayers().forEach((a) -> {
         if (!a.hasMetadata("vanished")) {
            completions.add(a.getName());
         }

      });
      return completions;
   }
}
