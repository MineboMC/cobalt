package net.minebo.cobalt.service.command;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

/**
 * A command argument type and its tab completions in one place. Commands use the completions
 * with {@code @CommandCompletion("@<id>")}.
 */
@Getter
public abstract class CommonCommandContext<T> implements ContextResolver<T, BukkitCommandExecutionContext> {

    private final String id;
    private final Class<T> type;

    /** @param id completion id, or null if this type has no completions */
    protected CommonCommandContext(String id, Class<T> type) {
        this.id = id;
        this.type = type;
    }

    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
        return List.of();
    }

    public void register(BukkitCommandManager manager) {
        manager.getCommandContexts().registerContext(type, this);
        if (id != null) {
            manager.getCommandCompletions().registerCompletion(id, this::getCompletions);
        }
    }

}
