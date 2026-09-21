package net.minebo.cobalt.service.command.defaults.completion;

import co.aikar.commands.CommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.InvalidCommandArgument;
import net.minebo.cobalt.util.Items;

import java.util.Collection;

public class MaterialCompletionHandler implements CommandCompletions.CommandCompletionHandler {
    @Override
    public Collection<String> getCompletions(CommandCompletionContext context) throws InvalidCommandArgument {
        return Items.getListOfMaterials();
    }
}