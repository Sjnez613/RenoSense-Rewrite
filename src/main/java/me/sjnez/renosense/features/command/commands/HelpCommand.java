package me.sjnez.renosense.features.command.commands;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.features.command.Command;

public class HelpCommand
        extends Command {
    public HelpCommand() {
        super("commands");
    }

    @Override
    public void execute(String[] commands) {
        HelpCommand.sendMessage("You can use following commands: ");
        for (Command command : RenoSense.commandManager.getCommands()) {
            HelpCommand.sendMessage(RenoSense.commandManager.getPrefix() + command.getName());
        }
    }
}

