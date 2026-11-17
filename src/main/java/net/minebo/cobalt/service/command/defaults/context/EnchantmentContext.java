package net.minebo.cobalt.service.command.defaults.context;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.minebo.cobalt.service.command.CommonCommandContext;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class EnchantmentContext extends CommonCommandContext<Enchantment> {

    public EnchantmentContext() {
        super("enchants", Enchantment.class);
    }

    @Override
    public Enchantment getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        String supplied = context.popFirstArg();
        if (supplied == null) {
            throw new InvalidCommandArgument(Coloring.translateColors("<red>Name an enchantment."), false);
        }

        Enchantment enchantment = resolve(supplied);
        if (enchantment == null) {
            throw new InvalidCommandArgument(Coloring.translateColors(
                    "<red>There's no enchantment called <white>" + supplied + "<red>."), false);
        }
        return enchantment;
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) {
        String input = normalize(Objects.requireNonNullElse(context.getInput(), ""));
        return Arrays.stream(Enchantment.values())
                .map(enchantment -> enchantment.getKey().getKey())
                .filter(key -> key.startsWith(input))
                .sorted()
                .toList();
    }

    /** Vanilla lookup, tolerant of spaces and casing ({@code Fire Aspect} → {@code fire_aspect}). */
    public static Enchantment resolve(String input) {
        return Enchantment.getByKey(NamespacedKey.minecraft(normalize(input)));
    }

    private static String normalize(String input) {
        return input.toLowerCase().replace(' ', '_');
    }
}
