package me.sjnez.renosense.features.command.commands;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.features.command.Command;
import me.sjnez.renosense.features.modules.client.ClickGui;

public class PrefixCommand
        extends Command {
    public PrefixCommand() {
        super("prefix", new String[]{"<char>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage("\u00a7cSpecify a new prefix.");
            return;
        }
        RenoSense.moduleManager.getModuleByClass(ClickGui.class).prefix.setValue(commands[0]);
        Command.sendMessage("Prefix set to \u00a7a" + RenoSense.commandManager.getPrefix());
    }
}

