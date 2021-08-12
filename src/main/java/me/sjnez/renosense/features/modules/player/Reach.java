package me.sjnez.renosense.features.modules.player;

import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;

public class Reach
        extends Module {
    private static Reach INSTANCE = new Reach();
    public Setting<Boolean> override = this.register(new Setting<Boolean>("Override", false));
    public Setting<Float> add = this.register(new Setting<Object>("Add", Float.valueOf(3.0f), v -> this.override.getValue() == false));
    public Setting<Float> reach = this.register(new Setting<Object>("Reach", Float.valueOf(6.0f), v -> this.override.getValue()));

    public Reach() {
        super("Reach", "Extends your block reach", Module.Category.PLAYER, true, false, false);
        this.setInstance();
    }

    public static Reach getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Reach();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public String getDisplayInfo() {
        return this.override.getValue() != false ? this.reach.getValue().toString() : this.add.getValue().toString();
    }
}

