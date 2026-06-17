package net.minebo.cobalt.acf.completion;

import co.aikar.commands.CommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import org.bukkit.enchantments.Enchantment;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class EnchantCompletionHandler implements CommandCompletions.CommandCompletionHandler {

    @Override
    public Collection<String> getCompletions(CommandCompletionContext context) {
        String input = context.getInput() != null ? context.getInput().toLowerCase() : "";

        return Arrays.stream(Enchantment.values())
                .filter(enchantment -> {
                    String key = enchantment.getKey().getKey(); // e.g. "sharpness"
                    return key.startsWith(input);
                })
                .map(enchantment -> enchantment.getKey().getKey().toUpperCase()) // UPPERCASE with underscores
                .collect(Collectors.toList());
    }
}