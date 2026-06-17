package net.minebo.cobalt.acf.completion;

import co.aikar.commands.CommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.InvalidCommandArgument;
import net.minebo.cobalt.util.EnchantmentWrapper;
import org.bukkit.enchantments.Enchantment;

import java.util.Arrays;
import java.util.Collection;

public class EnchantCompletionHandler implements CommandCompletions.CommandCompletionHandler {
    @Override
    public Collection<String> getCompletions(CommandCompletionContext context) throws InvalidCommandArgument {
        return Arrays.stream(Enchantment.values())
                .map(EnchantmentWrapper::parse) // or your own method
                .map(EnchantmentWrapper::getFriendlyName)
                .toList();
    }
}