package me.sjnez.renosense.features.command.commands;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.features.command.Command;

public class UnloadCommand
        extends Command {
    public UnloadCommand() {
        super("unload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        RenoSense.unload(true);
    }
}

