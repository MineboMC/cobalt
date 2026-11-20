package net.minebo.cobalt.service.command.defaults.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.hologram.Hologram;
import net.minebo.cobalt.service.hologram.HologramService;
import net.minebo.cobalt.service.hologram.menu.HologramEditorMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


@CommandAlias("holo|hologram|hd")
@CommandPermission("cobalt.hologram")
@Description("Create and manage packet holograms.")
public final class HologramCommand extends BaseCommand {

    @Default
    @CatchUnknown
    @HelpCommand
    @Description("Shows this help menu.")
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("create")
    @Syntax("[id]")
    @Description("Create a hologram at your location.")
    public void onCreate(Player player, @Optional String id) {
        String resolvedId = id == null || id.isBlank() ? player.getName() + "-Holo" : id;
        if (CobaltAPI.getHologramService().byId(resolvedId) != null) {
            player.sendMessage(Component.text("A hologram named " + resolvedId + " already exists.", NamedTextColor.RED));
            return;
        }
        Hologram hologram = CobaltAPI.getHologramService().create(player, resolvedId);
        player.sendMessage(Component.text("Created hologram " + hologram.id() + ". /holo manage " + hologram.id(), NamedTextColor.GREEN));
    }

    @Subcommand("list")
    @Description("List all holograms.")
    public void onList(Player player) {
        boolean any = false;
        for (Hologram hologram : CobaltAPI.getHologramService().registry().all()) {
            if (hologram.internal()) {
                continue;
            }
            any = true;
            player.sendMessage(Component.text("- " + hologram.id() + " (" + hologram.lines().size() + " lines)", NamedTextColor.YELLOW));
        }
        if (!any) {
            player.sendMessage(Component.text("No holograms.", NamedTextColor.GRAY));
        }
    }

    @Subcommand("manage|menu|edit")
    @Syntax("[id]")
    @Description("Open the hologram editor menu.")
    @CommandCompletion("@holograms")
    public void onManage(Player player, @Optional Hologram hologram) {
        new HologramEditorMenu(CobaltAPI.getHologramService(), hologram).openMenu(player);
    }

    @Subcommand("here|bring|s|movehere")
    @Syntax("<id>")
    @Description("Move a hologram to your location.")
    @CommandCompletion("@holograms")
    public void onHere(Player player, Hologram hologram) {
        CobaltAPI.getHologramService().teleportHere(hologram, player);
        player.sendMessage(Component.text("Moved " + hologram.id() + " to you.", NamedTextColor.GREEN));
    }

    @Subcommand("line add")
    @Syntax("<id> <text>")
    @Description("Add a line to a hologram.")
    @CommandCompletion("@holograms @nothing")
    public void onLineAdd(Player player, Hologram hologram, String text) {
        CobaltAPI.getHologramService().addLine(hologram, text);
        player.sendMessage(Component.text("Added line #" + (hologram.lines().size() - 1) + ".", NamedTextColor.GREEN));
    }

    @Subcommand("line set")
    @Syntax("<id> <index> <text>")
    @Description("Change a line on a hologram.")
    @CommandCompletion("@holograms @range:0-9 @nothing")
    public void onLineSet(Player player, Hologram hologram, int index, String text) {
        if (!CobaltAPI.getHologramService().setLine(hologram, index, text)) {
            player.sendMessage(Component.text("No line at " + index, NamedTextColor.RED));
            return;
        }
        player.sendMessage(Component.text("Updated line #" + index + ".", NamedTextColor.GREEN));
    }

    @Subcommand("line remove")
    @Syntax("<id> <index>")
    @Description("Remove a line from a hologram.")
    @CommandCompletion("@holograms @range:0-9")
    public void onLineRemove(Player player, Hologram hologram, int index) {
        if (!CobaltAPI.getHologramService().removeLine(hologram, index)) {
            player.sendMessage(Component.text("No line at " + index, NamedTextColor.RED));
            return;
        }
        player.sendMessage(Component.text("Removed line #" + index + ".", NamedTextColor.GREEN));
    }

    @Subcommand("viewdistance|range")
    @Syntax("<id> <blocks>")
    @Description("Set how far away players can see a hologram.")
    @CommandCompletion("@holograms 16|32|48|64")
    public void onViewDistance(Player player, Hologram hologram, double blocks) {
        CobaltAPI.getHologramService().setViewDistance(hologram, blocks);
        player.sendMessage(Component.text("View distance set to " + hologram.viewDistance(), NamedTextColor.GREEN));
    }

    @Subcommand("delete")
    @Syntax("<id>")
    @Description("Delete a hologram.")
    @CommandCompletion("@holograms")
    public void onDelete(Player player, Hologram hologram) {
        CobaltAPI.getHologramService().delete(hologram);
        player.sendMessage(Component.text("Deleted " + hologram.id(), NamedTextColor.RED));
    }

}