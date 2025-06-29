package net.minebo.cobalt.acf.defaults;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.minebo.cobalt.menu.impl.menu.TestingMenu;
import org.bukkit.entity.Player;

public class TestCommand extends BaseCommand {
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
