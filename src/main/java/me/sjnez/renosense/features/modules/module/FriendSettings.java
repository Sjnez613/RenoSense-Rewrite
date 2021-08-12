package me.sjnez.renosense.features.modules.module;

import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;

public class FriendSettings extends Module {
    private static FriendSettings INSTANCE;

    public Setting<Boolean> notify = this.register(new Setting("Notify", false));


    public FriendSettings(){
        super("FriendSettings", "Change aspects of friends", Category.MODULE, true, false, false);
        INSTANCE = this;
    }

    public static FriendSettings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FriendSettings();
        }
        return INSTANCE;
    }

}