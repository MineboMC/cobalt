package net.minebo.cobalt.service.command;

import co.aikar.commands.*;
import net.minebo.cobalt.util.Coloring;

public class CobaltHelpFormatter extends CommandHelpFormatter {
    public CobaltHelpFormatter(CommandManager manager) {
        super(manager);
    }

    @Override
    public void printHelpHeader(CommandHelp help, CommandIssuer issuer) {
        issuer.sendMessage(Coloring.translateColors("<gold>=== <white>Showing help for <yellow>/" + help.getCommandName() + " <gold>==="));
    }

    @Override
    public void printHelpCommand(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
        issuer.sendMessage(Coloring.translateColors("<yellow>" + entry.getCommand() + "<white> " + entry.getParameterSyntax() + " <dark_gray>- <gray>" + entry.getDescription()));
    }

    @Override
    public void printHelpFooter(CommandHelp help, CommandIssuer issuer) {
        if (help.isOnlyPage()) return;

        issuer.sendMessage(Coloring.translateColors("<white>- Showing page <yellow>" + help.getPage() + " <white>of <yellow>" + help.getTotalPages() + " <gray>(" + help.getTotalResults() + " entries)"));
    }
}
