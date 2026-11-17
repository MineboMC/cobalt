package net.minebo.cobalt.service.command.defaults.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import net.minebo.cobalt.service.command.CommonCommandContext;

import java.util.UUID;

public class UUIDContext extends CommonCommandContext<UUID> {

    public UUIDContext() {
        super(null, UUID.class);
    }

    @Override
    public UUID getContext(BukkitCommandExecutionContext context) {
        return UUID.fromString(context.getFirstArg());
    }
}
