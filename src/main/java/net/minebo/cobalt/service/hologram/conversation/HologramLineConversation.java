package net.minebo.cobalt.service.hologram.conversation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.hologram.Hologram;
import net.minebo.cobalt.service.hologram.HologramService;
import net.minebo.cobalt.service.menu.construct.Menu;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public final class HologramLineConversation {

    private HologramLineConversation() {}

    public static void addLine(HologramService service, Hologram hologram, Player player, Menu reopen) {
        addLine(service, hologram, player, reopen, null);
    }

    public static void addLine(HologramService service, Hologram hologram, Player player, Menu reopen, Runnable onEdited) {
        start(player, "<yellow>Enter the new line. Type <red>cancel</red> to abort.", input -> {
            service.addLine(hologram, input);
            if (onEdited != null) onEdited.run();
            player.sendMessage(Component.text("Added line.", NamedTextColor.GREEN));
            if (reopen != null) reopen.openMenu(player);
        });
    }

    public static void editLine(HologramService service, Hologram hologram, int index, Player player, Menu reopen) {
        editLine(service, hologram, index, player, reopen, null);
    }

    public static void editLine(HologramService service, Hologram hologram, int index, Player player, Menu reopen, Runnable onEdited) {
        start(player, "<yellow>Enter the new text for line #" + index + ". Type <red>cancel</red> to abort.", input -> {
            if (!service.setLine(hologram, index, input)) {
                player.sendMessage(Component.text("That line no longer exists.", NamedTextColor.RED));
                return;
            }
            if (onEdited != null) onEdited.run();
            player.sendMessage(Component.text("Updated line #" + index + ".", NamedTextColor.GREEN));
            if (reopen != null) reopen.openMenu(player);
        });
    }

    private static void start(Player player, String prompt, Consumer<String> accept) {
        player.closeInventory();

        CobaltAPI.getPromptService().ask(player, prompt,
                input -> {
                    if (input.isBlank()) {
                        sendCancelled(player);
                        return;
                    }
                    accept.accept(input);
                },
                () -> sendCancelled(player));
    }

    private static void sendCancelled(Player player) {
        player.sendMessage(Component.text("Cancelled.", NamedTextColor.GRAY));
    }
}