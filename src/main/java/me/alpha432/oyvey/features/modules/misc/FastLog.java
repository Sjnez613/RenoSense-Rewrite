package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;

public class FastLog
        extends Module {
    private final Setting<Boolean> FakeKick;

    public FastLog() {
        super("FastLog", "Log with the press of a button", Category.PLAYER, true, false, false);
        this.FakeKick = (Setting<Boolean>) this.register(new Setting("FakeKick", false));
    }

    public void onEnable() {
        if (!FakeKick.getValue()) {

            Minecraft.getMinecraft().getConnection().handleDisconnect(new SPacketDisconnect(new TextComponentString("[FastLog] Logged out")));
            this.disable();
            {

                Minecraft.getMinecraft().getConnection().handleDisconnect(new SPacketDisconnect(new TextComponentString("Internal Exception: java.lang.NullPointerException")));
                this.disable();
            }
        }
    }
}