package net.minebo.cobalt.service.command.defaults.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.scheduler.TimeFormat;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.command.CommandSender;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandAlias("autoreboot")
@CommandPermission("cobalt.reboot")
@Description("Manage the automatic server reboot.")
public final class RebootCommand extends BaseCommand {

    private static final Pattern DURATION = Pattern.compile("(\\d+)([hms])");

    @Default
    @CatchUnknown
    @HelpCommand
    @Description("Shows this help menu.")
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("status")
    @Description("Shows when the next reboot is.")
    public void onStatus(CommandSender sender) {
        long remaining = CobaltAPI.getRebootService().getTimeUntilReboot();
        if (remaining < 0L) {
            sender.sendMessage(Coloring.toComponent("<gray>No reboot is scheduled."));
            return;
        }
        sender.sendMessage(Coloring.toComponent("<gray>The server reboots in <aqua>" + TimeFormat.words(remaining) + "<gray>."));
    }

    @Subcommand("schedule")
    @Syntax("<duration, e.g. 10m or 1h30m>")
    @Description("Schedules a one-off reboot.")
    public void onSchedule(CommandSender sender, String duration) {
        long millis = parse(duration);
        if (millis <= 0L) {
            sender.sendMessage(Coloring.toComponent("<red>Invalid duration: " + duration + " (use e.g. 30s, 10m, 1h30m)"));
            return;
        }
        CobaltAPI.getRebootService().scheduleReboot(millis, TimeUnit.MILLISECONDS);
        sender.sendMessage(Coloring.toComponent("<green>Reboot scheduled in " + TimeFormat.words(millis) + "."));
    }

    @Subcommand("cancel")
    @Description("Cancels a manual reboot, or skips the next scheduled one.")
    public void onCancel(CommandSender sender) {
        if (CobaltAPI.getRebootService().cancelReboot()) {
            sender.sendMessage(Coloring.toComponent("<green>Upcoming reboot cancelled."));
        } else {
            sender.sendMessage(Coloring.toComponent("<red>No reboot is scheduled."));
        }
    }

    @Subcommand("reload")
    @Description("Reloads the reboot times from config.yml.")
    public void onReload(CommandSender sender) {
        Cobalt.getInstance().reloadConfig();
        CobaltAPI.getRebootService().loadConfig();
        sender.sendMessage(Coloring.toComponent("<green>Reboot settings reloaded."));
    }

    @Subcommand("now")
    @Description("Reboots the server immediately.")
    public void onNow(CommandSender sender) {
        CobaltAPI.getRebootService().reboot();
    }

    private static long parse(String input) {
        String text = input.toLowerCase();
        Matcher matcher = DURATION.matcher(text);
        long total = 0L;
        int end = 0;
        while (matcher.find()) {
            if (matcher.start() != end) {
                return -1L;
            }
            long amount = Long.parseLong(matcher.group(1));
            total += switch (matcher.group(2)) {
                case "h" -> TimeUnit.HOURS.toMillis(amount);
                case "m" -> TimeUnit.MINUTES.toMillis(amount);
                default -> TimeUnit.SECONDS.toMillis(amount);
            };
            end = matcher.end();
        }
        return end == text.length() ? total : -1L;
    }
}
