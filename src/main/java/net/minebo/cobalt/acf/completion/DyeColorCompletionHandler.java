package net.minebo.cobalt.acf.completion;

import co.aikar.commands.CommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.InvalidCommandArgument;
import org.bukkit.DyeColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DyeColorCompletionHandler implements CommandCompletions.CommandCompletionHandler {

    @Override
    public Collection<String> getCompletions(CommandCompletionContext context) throws InvalidCommandArgument {
        List<String> completions = new ArrayList<>();

        for(DyeColor color: DyeColor.values()) completions.add(color.name());

        return completions;
    }

}