package me.sjnez.renosense.features.modules.combat;

import me.sjnez.renosense.features.modules.Module;

public class SelfCrystal
        extends Module {
    public SelfCrystal() {
        super("SelfCrystal", "Best module", Module.Category.COMBAT, true, false, false);
    }

    @Override
    public void onTick() {
        if (AutoCrystal.getInstance().isEnabled()) {
            AutoCrystal.target = mc.player;
        }
    }
}

