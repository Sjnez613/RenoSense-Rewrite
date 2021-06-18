// 
// Decompiled by Procyon v0.5.36
// 

package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.DiscordPresence;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class RPC extends Module {
    public Setting<Boolean> showIP = (Setting<Boolean>) this.register(new Setting("ShowIP", true, "Shows the server IP in your discord presence."));
    public Setting<String> state = (Setting<String>) this.register(new Setting("State", "RenoSense Rewrite 0.6.2", "Sets the state of the DiscordRPC."));
    public Setting<String> largeImageText = (Setting<String>) this.register(new Setting("LargeImageText", "RenoSense Rewrite 0.6.2", "Sets the large image text of the DiscordRPC."));
    public Setting<String> smallImageText = (Setting<String>) this.register(new Setting("SmallImageText", "Best Client -Skitty", "Sets the small image text of the DiscordRPC."));
    public Setting<smallImage> smallimage = this.register(new Setting<Object>("SmallImage", smallImage.skitttyy));
    public Setting<largeImage> largeimage = this.register(new Setting<Object>("LargeImage", largeImage.renosense));

    public static RPC INSTANCE = new RPC();

    public RPC() {
        super("RPC", "Discord rich presence", Category.MISC, false, false, false);
    }
    @Override
    public void onEnable() {
        DiscordPresence.start();
    }
    @Override
    public void onDisable() {
        DiscordPresence.stop();
    }
    public enum smallImage{
        eu,
        skitttyy,
        ii,
        renosense
    }
    public enum largeImage{
        eu,
        skitttyy,
        ii,
        renosense
    }
}