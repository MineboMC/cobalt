package net.minebo.cobalt.acf;

import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import java.util.UUID;
import lombok.Generated;
import net.minebo.cobalt.acf.completion.ChatColorCompletionHandler;
import net.minebo.cobalt.acf.completion.PlayerCompletionHandler;
import net.minebo.cobalt.acf.context.ChatColorContextResolver;
import net.minebo.cobalt.acf.context.OnlinePlayerContextResolver;
import net.minebo.cobalt.acf.context.UUIDContextResolver;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ACFManager {
   public JavaPlugin plugin;

   public ACFManager(JavaPlugin plugin) {
      this.plugin = plugin;
      ACFCommandController.commandController = new PaperCommandManager(plugin);
      ACFCommandController.commandController.enableUnstableAPI("help");
      this.registerContexts();
      this.registerCompletions();
      ACFCommandController.registerAll(plugin);
   }

   public void registerContexts() {
      ACFCommandController.registerContext(ChatColor.class, new ChatColorContextResolver());
      ACFCommandController.registerContext(OnlinePlayer.class, new OnlinePlayerContextResolver());
      ACFCommandController.registerContext(UUID.class, new UUIDContextResolver());
   }

   public void registerCompletions() {
      ACFCommandController.registerCompletion("chatcolors", new ChatColorCompletionHandler());
      ACFCommandController.registerCompletion("players", new PlayerCompletionHandler());
   }

}
