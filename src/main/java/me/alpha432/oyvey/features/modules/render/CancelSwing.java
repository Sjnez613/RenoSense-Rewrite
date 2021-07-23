package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;

public class CancelSwing extends Module {

    public CancelSwing() {
        super("Swing", "si", Category.PLAYER, true, false, false);
    }

    public Setting<mode> swingmode = register(new Setting("Mode", mode.CancelAnimation));
    public Setting<Boolean> cancelmotion = register(new Setting("Motion", true));

    public enum mode {
        CancelAnimation,
        Mainhand,
        Offhand
    }
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketAnimation && swingmode.getValue() == mode.CancelAnimation) {
            event.setCanceled(true);
        }
    }
    @Override
    public void onUpdate() {
        if (cancelmotion.getValue()) {
            if (mc.entityRenderer.itemRenderer.prevEquippedProgressOffHand >= 0.9) {
                mc.entityRenderer.itemRenderer.equippedProgressOffHand = 1.0f;
            }
            if (mc.entityRenderer.itemRenderer.prevEquippedProgressMainHand >= 0.9) {
                mc.entityRenderer.itemRenderer.equippedProgressMainHand = 1.0f;
            }
        }
        switch (swingmode.getValue()) {
            case Mainhand: {
                setSwingingHand(EnumHand.MAIN_HAND);
            }

            case Offhand: {
                setSwingingHand(EnumHand.OFF_HAND);
            }
        }

    }

    public static void setSwingingHand(final EnumHand hand) {
        if (!mc.player.isSwingInProgress || mc.player.swingProgressInt < 0) {
            mc.player.swingProgressInt = -1;
            mc.player.swingingHand = hand;
        }
    }
}