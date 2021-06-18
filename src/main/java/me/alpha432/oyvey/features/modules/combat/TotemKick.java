package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.misc.PopCounter;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;

public class TotemKick extends Module{
    public static HashMap<String, Integer> TotemPopContainer = new HashMap();
    public Setting<Integer> attacks = this.register(new Setting<Integer>("Pops", 4, 1, 36));
    private static TotemKick INSTANCE = new TotemKick();

    public TotemKick() {
        super("TotemKick", "Kicks player after certain amount of pops", Module.Category.COMBAT, true, false, false);
    }

    public static TotemKick getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TotemKick();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }
    public void onTotemPop(EntityPlayer player) {
        if (PopCounter.fullNullCheck()) {
            return;
        }


    }

}
