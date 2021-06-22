package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;

public class FastLog
        extends Module {
    private final Setting<Boolean> FakeKick = this.register(new Setting<Boolean>("FakeKick", false));

    public FastLog() {
        super("FastLog", "Log with the press of a button", Category.PLAYER, true, false, false);
    }

    public void onEnable(){
            if (FakeKick.getValue() == false)

                Minecraft.getMinecraft().getConnection().handleDisconnect(new SPacketDisconnect(new TextComponentString("[FastLog] Logged out")));
            this.disable();

            if (FakeKick.getValue() == true)
                Minecraft.getMinecraft().getConnection().handleDisconnect(new SPacketDisconnect(new TextComponentString("Internal Exception: java.lang.NullPointerException")));
            this.disable();
        }
    }