package net.minebo.cobalt.service.command.defaults.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletions;
import co.aikar.commands.BukkitCommandManager;
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
import net.minebo.cobalt.service.hologram.menu.HologramLinesMenu;
import net.minebo.cobalt.service.npc.Npc;
import net.minebo.cobalt.service.npc.NpcAction;
import net.minebo.cobalt.service.npc.NpcActionType;
import net.minebo.cobalt.service.npc.NpcPose;
import net.minebo.cobalt.service.npc.NpcSkinMode;
import net.minebo.cobalt.service.npc.menu.NpcEditorMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("npc")
@CommandPermission("cobalt.npc")
@Description("Create and manage packet NPCs.")
public final class NpcCommand extends BaseCommand {

    public static void registerCompletions(BukkitCommandManager manager) {
        BukkitCommandCompletions completions = (BukkitCommandCompletions) manager.getCommandCompletions();

        // Every registered NPC name
        completions.registerCompletion("npcs", c ->
                CobaltAPI.getNpcService().registry().all().stream()
                        .map(Npc::name)
                        .collect(Collectors.toList()));

        // Entity types an NPC can be created as
        completions.registerCompletion("npctypes", c ->
                Arrays.stream(EntityType.values())
                        .filter(type -> type == EntityType.PLAYER || (type != EntityType.UNKNOWN && type.isSpawnable()))
                        .map(type -> type.name().toLowerCase())
                        .collect(Collectors.toList()));

        // Poses
        completions.registerCompletion("npcposes", c ->
                Arrays.stream(NpcPose.values())
                        .map(pose -> pose.name().toLowerCase())
                        .collect(Collectors.toList()));

        // Action types
        completions.registerCompletion("npcactions", c ->
                Arrays.stream(NpcActionType.values())
                        .map(type -> type.name().toLowerCase())
                        .collect(Collectors.toList()));

        // Skin targets: me, viewer, or any online player's name
        completions.registerCompletion("npcskins", c -> {
            List<String> values = new ArrayList<>();
            values.add("me");
            values.add("viewer");
            for (Player online : Bukkit.getOnlinePlayers()) {
                values.add(online.getName());
            }
            return values;
        });
    }

    @Default
    @CatchUnknown
    @HelpCommand
    @Description("Shows this help menu.")
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("create")
    @Syntax("[name] [type]")
    @Description("Create an NPC at your location.")
    @CommandCompletion("@nothing @npctypes")
    public void onCreate(Player player, @Optional String name, @Optional EntityType type) {
        EntityType resolved = type == null ? EntityType.PLAYER : type;
        String resolvedName = name == null || name.isBlank() ? player.getName() + "-NPC" : name;
        Npc npc = CobaltAPI.getNpcService().create(player, resolved, resolvedName);
        player.sendMessage(Component.text("Created " + npc.name() + ". /npc manage " + npc.name(), NamedTextColor.GREEN));
    }

    @Subcommand("list")
    @Description("List all NPCs.")
    public void onList(Player player) {
        if (CobaltAPI.getNpcService().registry().all().isEmpty()) {
            player.sendMessage(Component.text("No NPCs.", NamedTextColor.GRAY));
            return;
        }
        for (Npc npc : CobaltAPI.getNpcService().registry().all()) {
            player.sendMessage(Component.text("- " + npc.name() + " [" + npc.type().name() + "]", NamedTextColor.YELLOW));
        }
    }

    @Subcommand("manage|menu|edit")
    @Syntax("[npc]")
    @Description("Open the NPC editor menu.")
    @CommandCompletion("@npcs")
    public void onManage(Player player, @Optional String npcName) {
        if (npcName == null) {
            new NpcEditorMenu(CobaltAPI.getNpcService(), null).openMenu(player);
            return;
        }
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return;
        }
        new NpcEditorMenu(CobaltAPI.getNpcService(), npc).openMenu(player);
    }

    @Subcommand("here|bring")
    @Syntax("<npc>")
    @Description("Move an NPC to your location.")
    @CommandCompletion("@npcs")
    public void onHere(Player player, String npcName) {
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return;
        }
        CobaltAPI.getNpcService().teleportHere(npc, player);
        player.sendMessage(Component.text("Moved " + npc.name() + " to you.", NamedTextColor.GREEN));
    }

    @Subcommand("holo|hologram|lines")
    @Syntax("<npc>")
    @Description("Open the hologram line editor for an NPC.")
    @CommandCompletion("@npcs")
    public void onHolo(Player player, String npcName) {
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return;
        }
        if (CobaltAPI.getNpcService().holograms() == null) {
            player.sendMessage(Component.text("Hologram service is not hooked.", NamedTextColor.RED));
            return;
        }
        Hologram hologram = CobaltAPI.getNpcService().ensureNameHologram(npc);
        if (hologram == null) {
            player.sendMessage(Component.text("Could not create this NPC hologram.", NamedTextColor.RED));
            return;
        }
        new HologramLinesMenu(CobaltAPI.getNpcService().holograms(), hologram, new NpcEditorMenu(CobaltAPI.getNpcService(), npc), () -> CobaltAPI.getNpcService().captureHologram(npc)).openMenu(player);
    }

    @Subcommand("line add")
    @Syntax("<npc> <text>")
    @Description("Add a hologram line to an NPC.")
    @CommandCompletion("@npcs @nothing")
    public void onLineAdd(Player player, String npcName, String text) {
        Hologram hologram = requireHologram(player, npcName);
        if (hologram == null) {
            return;
        }
        CobaltAPI.getNpcService().holograms().addLine(hologram, text);
        CobaltAPI.getNpcService().captureHologram(CobaltAPI.getNpcService().byName(npcName));
        player.sendMessage(Component.text("Added hologram line.", NamedTextColor.GREEN));
    }

    @Subcommand("line set")
    @Syntax("<npc> <index> <text>")
    @Description("Change a hologram line on an NPC.")
    @CommandCompletion("@npcs @range:0-9 @nothing")
    public void onLineSet(Player player, String npcName, int index, String text) {
        Hologram hologram = requireHologram(player, npcName);
        if (hologram == null) {
            return;
        }
        if (!CobaltAPI.getNpcService().holograms().setLine(hologram, index, text)) {
            player.sendMessage(Component.text("No line at " + index, NamedTextColor.RED));
            return;
        }
        CobaltAPI.getNpcService().captureHologram(CobaltAPI.getNpcService().byName(npcName));
        player.sendMessage(Component.text("Updated line #" + index + ".", NamedTextColor.GREEN));
    }

    @Subcommand("line remove")
    @Syntax("<npc> <index>")
    @Description("Remove a hologram line from an NPC.")
    @CommandCompletion("@npcs @range:0-9")
    public void onLineRemove(Player player, String npcName, int index) {
        Hologram hologram = requireHologram(player, npcName);
        if (hologram == null) {
            return;
        }
        if (!CobaltAPI.getNpcService().holograms().removeLine(hologram, index)) {
            player.sendMessage(Component.text("No line at " + index, NamedTextColor.RED));
            return;
        }
        CobaltAPI.getNpcService().captureHologram(CobaltAPI.getNpcService().byName(npcName));
        player.sendMessage(Component.text("Removed line #" + index + ".", NamedTextColor.GREEN));
    }

    @Subcommand("skin")
    @Syntax("<npc> <username|me|viewer>")
    @Description("Set a player NPC's skin.")
    @CommandCompletion("@npcs @npcskins")
    public void onSkin(Player player, String npcName, String target) {
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return;
        }
        if (!npc.isPlayerNpc()) {
            player.sendMessage(Component.text("Skins only work on player NPCs.", NamedTextColor.RED));
            return;
        }
        if (target.equalsIgnoreCase("me")) {
            CobaltAPI.getNpcService().setSkinFromPlayer(npc, player);
            player.sendMessage(Component.text("Copied your skin.", NamedTextColor.GREEN));
            return;
        }
        if (target.equalsIgnoreCase("viewer")) {
            CobaltAPI.getNpcService().setSkinMode(npc, NpcSkinMode.VIEWER);
            player.sendMessage(Component.text("Skin now follows each viewer.", NamedTextColor.GREEN));
            return;
        }
        CobaltAPI.getNpcService().setSkinFromUsername(npc, target, player);
        player.sendMessage(Component.text("Fetching skin for " + target + "...", NamedTextColor.GRAY));
    }

    @Subcommand("pose")
    @Syntax("<npc> <pose>")
    @Description("Change an NPC's pose.")
    @CommandCompletion("@npcs @npcposes")
    public void onPose(Player player, String npcName, NpcPose pose) {
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return;
        }
        CobaltAPI.getNpcService().setPose(npc, pose);
        player.sendMessage(Component.text("Pose set.", NamedTextColor.GREEN));
    }

    @Subcommand("look")
    @Syntax("<npc> <on|off> [distance]")
    @Description("Toggle whether an NPC looks at nearby players.")
    @CommandCompletion("@npcs on|off 4|8|16|32")
    public void onLook(Player player, String npcName, String toggle, @Optional Double distance) {
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return;
        }
        boolean enabled = toggle.equalsIgnoreCase("on") || toggle.equalsIgnoreCase("true");
        npc.setLookAtPlayers(enabled);
        if (distance != null) {
            npc.setLookDistance(distance);
        }
        CobaltAPI.getNpcService().save(npc);
        player.sendMessage(Component.text("Look-at " + (enabled ? "enabled" : "disabled") + " (" + npc.lookDistance() + " blocks).", NamedTextColor.GREEN));
    }

    @Subcommand("viewdistance|range")
    @Syntax("<npc> <blocks>")
    @Description("Set how far away players can see an NPC.")
    @CommandCompletion("@npcs 16|32|48|64")
    public void onViewDistance(Player player, String npcName, double blocks) {
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return;
        }
        npc.setViewDistance(blocks);
        CobaltAPI.getNpcService().ensureNameHologram(npc);
        CobaltAPI.getNpcService().save(npc);
        player.sendMessage(Component.text("View distance set to " + npc.viewDistance(), NamedTextColor.GREEN));
    }

    @Subcommand("action")
    @Syntax("<npc> <message|player_command|console_command> <value>")
    @Description("Add a click action to an NPC.")
    @CommandCompletion("@npcs @npcactions @nothing")
    public void onAction(Player player, String npcName, NpcActionType type, String value) {
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return;
        }
        npc.actions().add(new NpcAction(type, value));
        CobaltAPI.getNpcService().save(npc);
        player.sendMessage(Component.text("Added action #" + (npc.actions().size() - 1) + ".", NamedTextColor.GREEN));
    }

    @Subcommand("actionremove")
    @Syntax("<npc> <index>")
    @Description("Remove a click action from an NPC.")
    @CommandCompletion("@npcs @range:0-9")
    public void onActionRemove(Player player, String npcName, int index) {
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return;
        }
        if (!npc.removeAction(index)) {
            player.sendMessage(Component.text("No action at " + index, NamedTextColor.RED));
            return;
        }
        CobaltAPI.getNpcService().save(npc);
        player.sendMessage(Component.text("Removed action #" + index + ".", NamedTextColor.GREEN));
    }

    @Subcommand("actionclear")
    @Syntax("<npc>")
    @Description("Clear all click actions from an NPC.")
    @CommandCompletion("@npcs")
    public void onActionClear(Player player, String npcName) {
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return;
        }
        npc.actions().clear();
        CobaltAPI.getNpcService().save(npc);
        player.sendMessage(Component.text("Cleared actions.", NamedTextColor.GREEN));
    }

    @Subcommand("delete")
    @Syntax("<npc>")
    @Description("Delete an NPC.")
    @CommandCompletion("@npcs")
    public void onDelete(Player player, String npcName) {
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return;
        }
        CobaltAPI.getNpcService().delete(npc);
        player.sendMessage(Component.text("Deleted " + npc.name(), NamedTextColor.RED));
    }

    private Npc requireNpc(Player player, String name) {
        Npc npc = CobaltAPI.getNpcService().byName(name);
        if (npc == null) {
            player.sendMessage(Component.text("No NPC named " + name, NamedTextColor.RED));
        }
        return npc;
    }

    private Hologram requireHologram(Player player, String npcName) {
        Npc npc = requireNpc(player, npcName);
        if (npc == null) {
            return null;
        }
        if (CobaltAPI.getNpcService().holograms() == null) {
            player.sendMessage(Component.text("Hologram service is not hooked.", NamedTextColor.RED));
            return null;
        }
        return CobaltAPI.getNpcService().ensureNameHologram(npc);
    }
}