package net.minebo.cobalt.acf.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import org.bukkit.enchantments.Enchantment;

//                                                  vvv this is why we dont trust ai blindly
public class EnchantmentContextResolver implements ContextResolver<Enchantment, BukkitCommandExecutionContext> {

    @Override
    public Enchantment getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        String arg = context.popFirstArg();

        if (arg == null) return null;

        Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(arg.toLowerCase()));
        if (enchantment != null) {
            return enchantment;
        }

        for (Enchantment e : Enchantment.values()) {
            String key = e.getKey().getKey();
            String friendly = key.replace("_", " ");

            if (key.equalsIgnoreCase(arg) || friendly.equalsIgnoreCase(arg)) {
                return e;
            }
        }

        return null;
    }
}