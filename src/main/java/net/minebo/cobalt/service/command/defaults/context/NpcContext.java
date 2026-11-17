package net.minebo.cobalt.service.command.defaults.context;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.command.CommonCommandContext;
import net.minebo.cobalt.service.npc.Npc;
import net.minebo.cobalt.util.Coloring;

import java.util.Collection;

public class NpcContext extends CommonCommandContext<Npc> {

    public NpcContext() {
        super("npcs", Npc.class);
    }

    @Override
    public Npc getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        String name = context.popFirstArg();
        Npc npc = CobaltAPI.getNpcService().byName(name);
        if (npc == null) {
            throw new InvalidCommandArgument(Coloring.translateColors("<red>No NPC named ") + name, false);
        }
        return npc;
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) {
        return CobaltAPI.getNpcService().registry().all().stream().map(Npc::name).toList();
    }
}
