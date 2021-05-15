package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.EntityUtil;

//made by sjnez

public class AntiWeb extends Module {
    private Setting<Boolean> HoleOnly;
    public Setting<Float> timerSpeed = register(new Setting("Speed", 4.0f, 0.1f, 50.0f));

    public float speed = 1.0f;


    public AntiWeb() {
        super("AntiWeb", "Turns on timer when in a web", Category.MOVEMENT, true, false, false);
        this.HoleOnly = (Setting<Boolean>) this.register(new Setting("HoleOnly", true));
    }

    @Override
    public void onEnable() {
        this.speed = timerSpeed.getValue();
    }


    @Override
    public void onUpdate() {

        if (HoleOnly.getValue()) {
            if (mc.player.isInWeb && EntityUtil.isInHole(mc.player)) {
                AntiWeb.mc.timer.tickLength = 50.0f / ((this.timerSpeed.getValue() == 0.0f) ? 0.1f : this.timerSpeed.getValue());
            } else {
                AntiWeb.mc.timer.tickLength = 50.0f;
            }
            if (mc.player.onGround && EntityUtil.isInHole(mc.player)) {
                AntiWeb.mc.timer.tickLength = 50.0f;
            }
        }
        if (!HoleOnly.getValue()) {
            if (mc.player.isInWeb) {
                AntiWeb.mc.timer.tickLength = 50.0f / ((this.timerSpeed.getValue() == 0.0f) ? 0.1f : this.timerSpeed.getValue());

            } else {
                AntiWeb.mc.timer.tickLength = 50.0f;

            }
            if (mc.player.onGround) {
                AntiWeb.mc.timer.tickLength = 50.0f;

            }
        }
    }
}