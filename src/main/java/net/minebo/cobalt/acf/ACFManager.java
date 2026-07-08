package net.minebo.cobalt.acf;

import co.aikar.commands.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import java.util.UUID;

import net.minebo.cobalt.acf.completion.*;
import net.minebo.cobalt.acf.context.ChatColorContextResolver;
import net.minebo.cobalt.acf.context.EnchantmentContextResolver;
import net.minebo.cobalt.acf.context.OnlinePlayerContextResolver;
import net.minebo.cobalt.acf.context.UUIDContextResolver;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

public class ACFManager {
   public JavaPlugin plugin;

   public CommandHelpFormatter helpFormatter = (new CommandHelpFormatter(ACFCommandController.commandController) {
      @Override
      public void printHelpHeader(CommandHelp help, CommandIssuer issuer) {
         issuer.sendMessage(ColorUtil.translateColors("<gold>=== <white>Showing help for <yellow>/" + help.getCommandName() + " <gold>==="));
      }

      @Override
      public void printHelpCommand(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
         issuer.sendMessage(ColorUtil.translateColors("<yellow>" + entry.getCommand() + "<white> " + entry.getParameterSyntax() + " <dark_gray>- <gray>" + entry.getDescription()));
      }

      @Override
      public void printHelpFooter(CommandHelp help, CommandIssuer issuer) {
         if (help.isOnlyPage()) return;

         issuer.sendMessage(ColorUtil.translateColors("<white>- Showing page <yellow>" + help.getPage() + " <white>of <yellow>" + help.getTotalPages() + " <gray>(" + help.getTotalResults() + " entries)"));
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
      ACFCommandController.registerContext(Enchantment.class, new EnchantmentContextResolver());
   }

   public void registerCompletions() {
      ACFCommandController.registerCompletion("chatcolors", new ChatColorCompletionHandler());
      ACFCommandController.registerCompletion("materials", new MaterialCompletionHandler());
      ACFCommandController.registerCompletion("players", new PlayerCompletionHandler());
      ACFCommandController.registerCompletion("dyecolors", new DyeColorCompletionHandler());
      ACFCommandController.registerCompletion("enchants", new EnchantCompletionHandler());
   }

}
