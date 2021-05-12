package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.player.TimerSpeed;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.EntityUtil;

//made by sjnez

public class AntiWeb extends Module {
    private Setting<Boolean> HoleOnly;

    public AntiWeb() {
        super("AntiWeb", "Turns on timer when in a web", Module.Category.COMBAT, true, false, false);
        this.HoleOnly = (Setting<Boolean>) this.register(new Setting("HoleOnly", true));
    }

    @Override
    public void onUpdate() {

        TimerSpeed timerspeed = OyVey.moduleManager.getModuleByClass(TimerSpeed.class);
        if (HoleOnly.getValue()) {
            if (mc.player.isInWeb && EntityUtil.isInHole(mc.player))
                mc.timer.tickLength = 2;
            else {
                mc.timer.elapsedTicks = 0;
            }
            if (mc.player.onGround && EntityUtil.isInHole(mc.player))
                mc.timer.elapsedTicks = 0;
        }
        if (!HoleOnly.getValue()) {
            if (mc.player.isInWeb)
                mc.timer.tickLength = 2;
            else {
                mc.timer.elapsedTicks = 0;
            }
            if (mc.player.onGround)
                mc.timer.elapsedTicks = 0;
        }
    }
}