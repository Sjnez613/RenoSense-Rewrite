package me.sjnez.renosense.features.modules.client;

import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;

public class Screens
        extends Module {
    public static Screens INSTANCE;
    public Setting<Boolean> mainScreen = this.register(new Setting<Boolean>("MainScreen", false));

    public Screens() {
        super("Screens", "Controls custom screens used by the client", Module.Category.CLIENT, true, false, false);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
    }
}

