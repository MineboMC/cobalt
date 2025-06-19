package net.minebo.cobalt.acf;

import co.aikar.commands.BukkitMessageFormatter;
import co.aikar.commands.MessageType;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import java.util.UUID;
import lombok.Generated;
import net.minebo.cobalt.acf.completion.ChatColorCompletionHandler;
import net.minebo.cobalt.acf.completion.MaterialCompletionHandler;
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

      ACFCommandController.commandController.setFormat(
              MessageType.SYNTAX,
              new BukkitMessageFormatter(ChatColor.YELLOW, ChatColor.GOLD, ChatColor.WHITE)
      );

      ACFCommandController.commandController.setFormat(MessageType.HELP, new BukkitMessageFormatter(ChatColor.YELLOW, ChatColor.GOLD, ChatColor.GOLD));

      this.registerContexts();
      this.registerCompletions();
   }

   public void registerContexts() {
      ACFCommandController.registerContext(ChatColor.class, new ChatColorContextResolver());
      ACFCommandController.registerContext(OnlinePlayer.class, new OnlinePlayerContextResolver());
      ACFCommandController.registerContext(UUID.class, new UUIDContextResolver());
   }

   public void registerCompletions() {
      ACFCommandController.registerCompletion("chatcolors", new ChatColorCompletionHandler());
      ACFCommandController.registerCompletion("materials", new MaterialCompletionHandler());
      ACFCommandController.registerCompletion("players", new PlayerCompletionHandler());
   }

}
