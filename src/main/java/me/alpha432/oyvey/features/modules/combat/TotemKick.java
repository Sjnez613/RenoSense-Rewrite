package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.misc.PopCounter;
import me.alpha432.oyvey.features.setting.Setting;

public class TotemKick extends Module{
    public Setting<Integer> attacks = this.register(new Setting<Integer>("Pops", 4, 1, 36));
    private static TotemKick INSTANCE = new TotemKick();

    public TotemKick() {
        super("TotemKick", "Kicks player after certain amount of pops", Module.Category.COMBAT, true, true, false);
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
    public void onUpdate() {
        if (PopCounter.fullNullCheck()) {
            return;
        }


    }

}
