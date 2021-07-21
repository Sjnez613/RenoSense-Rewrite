package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;

public class FakeKick
        extends Module {
    private final Setting<Boolean> healthDisplay = this.register(new Setting<Boolean>("HealthDisplay", false));

    public FakeKick() {
        super("FakeKick", "Log with the press of a button", Category.COMBAT, true, false, false);
    }

    public void onEnable(){
            if (healthDisplay.getValue()){
            float health = (mc.player.getAbsorptionAmount()+mc.player.getHealth());
                Minecraft.getMinecraft().getConnection().handleDisconnect(new SPacketDisconnect(new TextComponentString("Logged out with " + health + " health remaining." )));
            this.disable();
            }
                Minecraft.getMinecraft().getConnection().handleDisconnect(new SPacketDisconnect(new TextComponentString("Internal Exception: java.lang.NullPointerException")));
            this.disable();
        }
    }