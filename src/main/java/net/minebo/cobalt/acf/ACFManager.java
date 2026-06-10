package net.minebo.cobalt.acf;

import co.aikar.commands.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import java.util.UUID;
import net.minebo.cobalt.acf.completion.ChatColorCompletionHandler;
import net.minebo.cobalt.acf.completion.MaterialCompletionHandler;
import net.minebo.cobalt.acf.completion.PlayerCompletionHandler;
import net.minebo.cobalt.acf.context.ChatColorContextResolver;
import net.minebo.cobalt.acf.context.OnlinePlayerContextResolver;
import net.minebo.cobalt.acf.context.UUIDContextResolver;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ACFManager {
   public JavaPlugin plugin;

   public CommandHelpFormatter helpFormatter = (new CommandHelpFormatter(ACFCommandController.commandController) {
      @Override
      public void printHelpHeader(CommandHelp help, CommandIssuer issuer) {
         issuer.sendMessage(ColorUtil.translateColors("&6=== &fShowing help for &e/" + help.getCommandName() + " &6==="));
      }

      @Override
      public void printHelpCommand(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
         issuer.sendMessage(ColorUtil.translateColors("&e" + entry.getCommand() + "&f " + entry.getParameterSyntax() + " &8- &7" + entry.getDescription()));
      }

      @Override
      public void printHelpFooter(CommandHelp help, CommandIssuer issuer) {
         if (help.isOnlyPage()) return;

         issuer.sendMessage(ColorUtil.translateColors("&f- Showing page &e" + help.getPage() + " of &e" + help.getTotalPages() + " &7(" + help.getTotalResults() + " entries)"));
      }
   });

   public ACFManager(JavaPlugin plugin) {
      this.plugin = plugin;

      ACFCommandController.commandController = new PaperCommandManager(plugin);
      ACFCommandController.commandController.enableUnstableAPI("help");
      ACFCommandController.commandController.setFormat(MessageType.HELP, new BukkitMessageFormatter(ChatColor.YELLOW, ChatColor.WHITE, ChatColor.GRAY));
      ACFCommandController.commandController.setHelpFormatter(helpFormatter);

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
