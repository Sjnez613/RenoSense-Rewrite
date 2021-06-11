// 
// Decompiled by Procyon v0.5.36
// 

package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.DiscordPresence;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class RPC extends Module {
    public static RPC INSTANCE;
    public Setting<Boolean> showIP;
    public Setting<String> state;
    public Setting<String> largeImageText;
    public Setting<String> smallImageText;

    public RPC() {
        super("RPC", "Discord rich presence", Category.MISC, false, false, false);
        this.showIP = (Setting<Boolean>) this.register(new Setting("ShowIP", true, "Shows the server IP in your discord presence."));
        this.state = (Setting<String>) this.register(new Setting("State", "RenoSense Rewrite 0.5", "Sets the state of the DiscordRPC."));
        this.largeImageText = (Setting<String>) this.register(new Setting("LargeImageText", "RenoSense Rewrite 0.5", "Sets the large image text of the DiscordRPC."));
        this.smallImageText = (Setting<String>) this.register(new Setting("SmallImageText", "Best Client -Skitty", "Sets the small image text of the DiscordRPC."));

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