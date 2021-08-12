package me.sjnez.renosense.features.command.commands;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.features.command.Command;

public class ReloadCommand
        extends Command {
    public ReloadCommand() {
        super("reload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        RenoSense.reload();
    }
}

