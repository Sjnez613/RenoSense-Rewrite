package me.sjnez.renosense.features.command.commands;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.features.command.Command;

public class BaritoneNoStop
        extends Command {
    public BaritoneNoStop() {
        super("noStop", new String[]{"<prefix>", "<x>", "<y>", "<z>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 5) {
            RenoSense.baritoneManager.setPrefix(commands[0]);
            int x = 0;
            int y = 0;
            int z = 0;
            try {
                x = Integer.parseInt(commands[1]);
                y = Integer.parseInt(commands[2]);
                z = Integer.parseInt(commands[3]);
            } catch (NumberFormatException e) {
                BaritoneNoStop.sendMessage("Invalid Input for x, y or z!");
                RenoSense.baritoneManager.stop();
                return;
            }
            RenoSense.baritoneManager.start(x, y, z);
            return;
        }
        BaritoneNoStop.sendMessage("Stoping Baritone-Nostop.");
        RenoSense.baritoneManager.stop();
    }
}

