package me.sjnez.renosense.features.modules.module;

import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;

public class Display extends Module {

    private static Display INSTANCE = new Display();
    public Setting<String> gang = this.register(new Setting("Title", "RenoSense"));
    public Setting<Boolean> version = this.register(new Setting("Version", true));

    public Display(){
        super("Display", "Sets the title of your game", Category.MODULE, true, false, false);
        this.setInstance();
    }
    public static Display getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Display();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        org.lwjgl.opengl.Display.setTitle(this.gang.getValue());
    }
}
