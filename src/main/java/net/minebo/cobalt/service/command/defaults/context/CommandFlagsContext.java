package net.minebo.cobalt.service.command.defaults.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import net.minebo.cobalt.service.command.CommonCommandContext;
import net.minebo.cobalt.service.command.flag.CommandFlags;

public class CommandFlagsContext extends CommonCommandContext<CommandFlags> {

    public CommandFlagsContext() {
        super(null, CommandFlags.class);
    }

    /** Takes the rest of the arguments. */
    @Override
    public CommandFlags getContext(BukkitCommandExecutionContext context) {
        String raw = context.joinArgs();
        context.getArgs().clear();
        return CommandFlags.parse(raw, context.getFlags().keySet());
    }
}
