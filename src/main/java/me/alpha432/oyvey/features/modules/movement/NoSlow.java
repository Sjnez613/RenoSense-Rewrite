package me.alpha432.oyvey.features.modules.movement;


import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoSlow extends Module {
    public Setting<Boolean> noSlow = register(new Setting<Boolean>("NoSlow", true));
    public Setting<Boolean> explosions = register(new Setting<Boolean>("Explosions", false));
    public Setting<Float> horizontal = this.register(new Setting<Float>("Horizontal", Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(100.0f), v -> this.explosions.getValue()));
    public Setting<Float> vertical = this.register(new Setting<Float>("Vertical", Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(100.0f), v -> this.explosions.getValue()));
    private static NoSlow INSTANCE = new NoSlow();
    private boolean sneaking = false;
    private static KeyBinding[] keys = new KeyBinding[]{NoSlow.mc.gameSettings.keyBindForward, NoSlow.mc.gameSettings.keyBindBack, NoSlow.mc.gameSettings.keyBindLeft, NoSlow.mc.gameSettings.keyBindRight, NoSlow.mc.gameSettings.keyBindJump, NoSlow.mc.gameSettings.keyBindSprint};

    public NoSlow() {
        super("NoSlow", "Prevents you from getting slowed down.", Module.Category.MOVEMENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static NoSlow getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoSlow();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public void onPacketReceived(PacketEvent.Receive event) {
        if (event.getStage() == 0 && mc.player != null) {
            if (event.getPacket() instanceof SPacketEntityVelocity) {
                SPacketEntityVelocity velocity = (SPacketEntityVelocity)event.getPacket();
                if (velocity.getEntityID() == mc.player.entityId) {
                    if (((Float)this.horizontal.getValue()).floatValue() == 0.0F && ((Float)this.vertical.getValue()).floatValue() == 0.0F) {
                        event.setCanceled(true);
                        return;
                    }
                    velocity.motionX = (int)(velocity.motionX * ((Float)this.horizontal.getValue()).floatValue());
                    velocity.motionY = (int)(velocity.motionY * ((Float)this.vertical.getValue()).floatValue());
                    velocity.motionZ = (int)(velocity.motionZ * ((Float)this.horizontal.getValue()).floatValue());
                }
            }
            if (((Boolean)this.explosions.getValue()).booleanValue() && event.getPacket() instanceof SPacketExplosion) {
                SPacketExplosion velocity = (SPacketExplosion)event.getPacket();
                velocity.motionX *= ((Float)this.horizontal.getValue()).floatValue();
                velocity.motionY *= ((Float)this.vertical.getValue()).floatValue();
                velocity.motionZ *= ((Float)this.horizontal.getValue()).floatValue();
            }
        }
    }
    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (noSlow.getValue().booleanValue() && NoSlow.mc.player.isHandActive() && !NoSlow.mc.player.isRiding()) {
            event.getMovementInput().moveStrafe *= 5.0f;
            event.getMovementInput().moveForward *= 5.0f;
        }
    }
}