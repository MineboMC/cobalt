package net.minebo.cobalt.service.command.defaults.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.minebo.cobalt.service.ServiceManager;
import net.minebo.cobalt.service.menu.impl.menu.TestingMenu;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.entity.Player;

@CommandAlias("cobalt")
public class CobaltCommands extends BaseCommand {

   @CommandAlias("services")
   @Subcommand("services")
   public void ServicesCommand(CommandIssuer sender) {
      sender.sendMessage(Coloring.translateColors("&bRegistered Services:"));
      ServiceManager.services.forEach((p , c) -> {
         c.forEach(s -> sender.sendMessage(Coloring.translateColors("<gray>* " + "<gray>\"<white>" + s.getName() + "<gray>\" <dark_gray>- <aqua>" + p.getName())));
      });
   }

   @CommandAlias("test")
   @Description("Command for testing the acf module.")
   @CommandCompletion("@players")
   @Syntax("test <target>")
   public void TestCommand(Player sender, @Optional OnlinePlayer target) {
      if (target == null) {
         sender.sendMessage("Howdy!");
      } else {
         sender.sendMessage("Howdy, " + target.getPlayer().getDisplayName() + "!");
      }

      new TestingMenu().openMenu(sender);

   }
}
