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
import net.minebo.cobalt.service.projectile.ProjectileService;
import net.minebo.cobalt.service.projectile.ProjectileType;
import net.minebo.cobalt.service.projectile.trail.Trail;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("projectile|proj")
@CommandPermission("cobalt.projectile")
@Description("Test registered projectiles and trails.")
public final class ProjectileCommand extends BaseCommand {

    public static void registerCompletions(BukkitCommandManager manager) {
        BukkitCommandCompletions completions = (BukkitCommandCompletions) manager.getCommandCompletions();

        completions.registerCompletion("projectiles", c -> {
            ProjectileService service = CobaltAPI.getProjectileService();
            return service == null ? List.of() : List.copyOf(service.types().keySet());
        });
        completions.registerCompletion("projectiletrails", c -> {
            ProjectileService service = CobaltAPI.getProjectileService();
            return service == null ? List.of() : List.copyOf(service.trails().keySet());
        });
    }

    @Default
    @CatchUnknown
    @HelpCommand
    @Description("Shows this help menu.")
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("shoot")
    @Syntax("<type> [speed] [trail]")
    @CommandCompletion("@projectiles @nothing @projectiletrails")
    @Description("Shoot a registered projectile, optionally overriding its speed and trail.")
    public void onShoot(Player player, String typeId, @Optional Double speed, @Optional String trailId) {
        ProjectileService service = CobaltAPI.getProjectileService();
        ProjectileType type = service.getType(typeId);
        if (type == null) {
            player.sendMessage(Component.text("Unknown projectile type: " + typeId, NamedTextColor.RED));
            return;
        }

        if (speed != null || trailId != null) {
            ProjectileType.Builder builder = type.toBuilder();
            if (speed != null) {
                builder.speed(speed);
            }
            if (trailId != null) {
                Trail trail = service.getTrail(trailId);
                if (trail == null) {
                    player.sendMessage(Component.text("Unknown trail: " + trailId, NamedTextColor.RED));
                    return;
                }
                builder.clearTrails().trail(trail);
            }
            type = builder.build();
        }

        service.launch(type, player);
    }

    @Subcommand("list")
    @Description("List registered projectile types and trails.")
    public void onList(CommandSender sender) {
        ProjectileService service = CobaltAPI.getProjectileService();
        sender.sendMessage(Component.text("Projectiles: ", NamedTextColor.AQUA)
                .append(Component.text(service.types().isEmpty() ? "none" : String.join(", ", service.types().keySet()), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Trails: ", NamedTextColor.AQUA)
                .append(Component.text(service.trails().isEmpty() ? "none" : String.join(", ", service.trails().keySet()), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Active: ", NamedTextColor.AQUA)
                .append(Component.text(String.valueOf(service.active().size()), NamedTextColor.WHITE)));
    }

    @Subcommand("clear")
    @Description("Remove every projectile currently in flight.")
    public void onClear(CommandSender sender) {
        ProjectileService service = CobaltAPI.getProjectileService();
        int count = service.active().size();
        List.copyOf(service.active()).forEach(projectile -> projectile.remove());
        sender.sendMessage(Component.text("Removed " + count + " projectile(s).", NamedTextColor.GREEN));
    }
}
