package me.sjnez.renosense.features.modules.misc;

import me.sjnez.renosense.DiscordPresence;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;

public class RPC extends Module {
    public static RPC INSTANCE;
    public Setting<Boolean> showIP = (Setting<Boolean>) this.register(new Setting("ShowIP", true, "Shows the server IP in your discord presence."));
    public Setting<String> state = (Setting<String>) this.register(new Setting("State", "Charles Danahack", "Sets the state of the DiscordRPC."));
    public Setting<String> largeImageText = (Setting<String>) this.register(new Setting("LargeImageText", "Charles Danahack", "Sets the large image text of the DiscordRPC."));
    public Setting<String> smallImageText = (Setting<String>) this.register(new Setting("SmallImageText", "Charles Danahack", "Sets the small image text of the DiscordRPC."));


    public RPC() {
        super("RPC", "Discord rich presence", Category.MISC, false, false, false);

        RPC.INSTANCE = this;
    }

    @Override
    public void onEnable() {
        DiscordPresence.start();
    }


    @Override
    public void onDisable() {
        DiscordPresence.stop();
    }

}
