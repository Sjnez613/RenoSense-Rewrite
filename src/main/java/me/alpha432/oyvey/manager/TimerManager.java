package me.alpha432.oyvey.manager;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.modules.player.TimerSpeed;

public class TimerManager extends Feature {

    private float timer = 1.0f;
    private TimerSpeed module;

    public void init() {
        module = OyVey.moduleManager.getModuleByClass(TimerSpeed.class);
    }

    public void unload() {
        timer = 1.0f;
        mc.timer.tickLength = 50.0f;
    }

    public void update() {
        if (module != null && module.isEnabled()) {
            this.timer = module.speed;
        }
        mc.timer.tickLength = 50.0f / (timer <= 0.0f ? 0.1f : timer);
    }

    public float getTimer() {
        return this.timer;
    }

    public void setTimer(float timer) {
        if (timer > 0.0f) {
            this.timer = timer;
        }
    }

    public void reset() {
        this.timer = 1.0f;
    }
}