package net.minebo.cobalt.acf.completion;

import co.aikar.commands.CommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.InvalidCommandArgument;
import net.minebo.cobalt.util.ItemUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MaterialCompletionHandler implements CommandCompletions.CommandCompletionHandler {
    @Override
    public Collection<String> getCompletions(CommandCompletionContext context) throws InvalidCommandArgument {
        return ItemUtil.getListOfMaterials();
    }
}