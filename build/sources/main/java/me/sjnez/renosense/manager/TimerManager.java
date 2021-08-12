package me.sjnez.renosense.manager;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.features.Feature;
import me.sjnez.renosense.features.modules.player.TimerSpeed;

public class TimerManager
        extends Feature {
    private float timer = 1.0f;
    private TimerSpeed module;

    public void init() {
        this.module = RenoSense.moduleManager.getModuleByClass(TimerSpeed.class);
    }

    public void unload() {
        this.timer = 1.0f;
        TimerManager.mc.timer.tickLength = 50.0f;
    }

    public void update() {
        if (this.module != null && this.module.isEnabled()) {
            this.timer = this.module.speed;
        }
        TimerManager.mc.timer.tickLength = 50.0f / (this.timer <= 0.0f ? 0.1f : this.timer);
    }

    public float getTimer() {
        return this.timer;
    }

    public void setTimer(float timer) {
        if (timer > 0.0f) {
            this.timer = timer;
        }
    }

    @Override
    public void reset() {
        this.timer = 1.0f;
    }
}

