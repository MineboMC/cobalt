package net.minebo.cobalt.service.command.defaults.context;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.command.CommonCommandContext;
import net.minebo.cobalt.service.hologram.Hologram;

import java.util.Collection;

/** Internal holograms (e.g. NPC name tags) can't be picked from commands. */
public class HologramContext extends CommonCommandContext<Hologram> {

    public HologramContext() {
        super("holograms", Hologram.class);
    }

    @Override
    public Hologram getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        String id = context.popFirstArg();
        Hologram hologram = CobaltAPI.getHologramService().byId(id);
        if (hologram == null || hologram.internal()) {
            throw new InvalidCommandArgument("No hologram named " + id, false);
        }
        return hologram;
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) {
        return CobaltAPI.getHologramService().registry().all().stream()
                .filter(hologram -> !hologram.internal())
                .map(Hologram::id)
                .toList();
    }
}
