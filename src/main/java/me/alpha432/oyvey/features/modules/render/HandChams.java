package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class HandChams
        extends Module {
    private static HandChams INSTANCE = new HandChams();
    public Setting<RenderMode> mode = this.register(new Setting<RenderMode>("Mode", RenderMode.SOLID));
    public Setting<Integer> red = this.register(new Setting<Integer>("Red", 255, 0, 255));
    public Setting<Integer> green = this.register(new Setting<Integer>("Green", 0, 0, 255));
    public Setting<Integer> blue = this.register(new Setting<Integer>("Blue", 0, 0, 255));
    public Setting<Integer> alpha = this.register(new Setting<Integer>("Alpha", 240, 0, 255));

    public HandChams() {
        super("HandChams", "Changes your hand color.", Module.Category.RENDER, false, false, false);
        this.setInstance();
    }

    public static HandChams getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new HandChams();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public enum RenderMode {
        SOLID,
        WIREFRAME

    }
}

