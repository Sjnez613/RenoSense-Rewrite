package me.sjnez.renosense.features.command.commands;

import me.sjnez.renosense.features.command.Command;
import me.sjnez.renosense.util.Util;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ReloadSoundCommand
        extends Command {
    public ReloadSoundCommand() {
        super("sound", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        try {
            SoundManager sndManager = ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class, Util.mc.getSoundHandler(), new String[]{"sndManager", "field_147694_f"});
            sndManager.reloadSoundSystem();
            ReloadSoundCommand.sendMessage("\u00a7aReloaded Sound System.");
        } catch (Exception e) {
            System.out.println("Could not restart sound manager: " + e );
            e.printStackTrace();
            ReloadSoundCommand.sendMessage("\u00a7cCouldnt Reload Sound System!");
        }
    }
}
