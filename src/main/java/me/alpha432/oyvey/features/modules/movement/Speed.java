package me.alpha432.oyvey.features.modules.movement;


import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.ClientEvent;
import me.alpha432.oyvey.event.events.MoveEvent;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.EntityUtil;
import net.minecraft.util.MovementInput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Speed extends Module
{
    public Setting<Mode> mode;
    public Setting<Boolean> strafeJump;
    public Setting<Boolean> noShake;
    public Setting<Boolean> useTimer;
    private static Speed INSTANCE;
    private double highChainVal;
    private double lowChainVal;
    private boolean oneTime;
    public double startY;
    public boolean antiShake;
    private double bounceHeight;
    private float move;

    public Speed() {
        super("Speed", "Makes you faster", Category.MOVEMENT, true, false, false);
        this.mode = (Setting<Mode>)this.register(new Setting("Mode", Mode.INSTANT));
        this.strafeJump = (Setting<Boolean>)this.register(new Setting("Jump", false, v -> this.mode.getValue() == Mode.INSTANT));
        this.noShake = (Setting<Boolean>)this.register(new Setting("NoShake", true, v -> this.mode.getValue() != Mode.INSTANT));
        this.useTimer = (Setting<Boolean>)this.register(new Setting("UseTimer", false, v -> this.mode.getValue() != Mode.INSTANT));
        this.highChainVal = 0.0;
        this.lowChainVal = 0.0;
        this.oneTime = false;
        this.startY = 0.0;
        this.antiShake = false;
        this.bounceHeight = 0.4;
        this.move = 0.26f;
        this.setInstance();
    }

    private void setInstance() {
        Speed.INSTANCE = this;
    }

    public static Speed getInstance() {
        if (Speed.INSTANCE == null) {
            Speed.INSTANCE = new Speed();
        }
        return Speed.INSTANCE;
    }

    private boolean shouldReturn() {
        return OyVey.moduleManager.isModuleEnabled("Freecam") || OyVey.moduleManager.isModuleEnabled("Phase") || OyVey.moduleManager.isModuleEnabled("ElytraFlight") || OyVey.moduleManager.isModuleEnabled("Strafe") || OyVey.moduleManager.isModuleEnabled("Flight");
    }

    @Override
    public void onUpdate() {
        if (this.shouldReturn() || Speed.mc.player.isSneaking() || Speed.mc.player.isInWater() || Speed.mc.player.isInLava()) {
            return;
        }

    }

    @Override
    public void onDisable() {
        OyVey.timerManager.setTimer(1.0f);
        this.highChainVal = 0.0;
        this.lowChainVal = 0.0;
        this.antiShake = false;
    }

    @SubscribeEvent
    public void onSettingChange(final ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().equals(this.mode) && this.mode.getPlannedValue() == Mode.INSTANT) {
            Speed.mc.player.motionY = -0.1;
        }
    }

    @Override
    public String getDisplayInfo() {
        return this.mode.currentEnumName();
    }

    @SubscribeEvent
    public void onMode(final MoveEvent event) {
        if (!this.shouldReturn() && event.getStage() == 0 && this.mode.getValue() == Mode.INSTANT && !Feature.nullCheck() && !Speed.mc.player.isSneaking() && !Speed.mc.player.isInWater() && !Speed.mc.player.isInLava() && (Speed.mc.player.movementInput.moveForward != 0.0f || Speed.mc.player.movementInput.moveStrafe != 0.0f)) {
            if (Speed.mc.player.onGround && this.strafeJump.getValue()) {
                event.setY(Speed.mc.player.motionY = 0.4);
            }
            final MovementInput movementInput = Speed.mc.player.movementInput;
            float moveForward = movementInput.moveForward;
            float moveStrafe = movementInput.moveStrafe;
            float rotationYaw = Speed.mc.player.rotationYaw;
            if (moveForward == 0.0 && moveStrafe == 0.0) {
                event.setX(0.0);
                event.setZ(0.0);
            }
            else {
                if (moveForward != 0.0) {
                    if (moveStrafe > 0.0) {
                        rotationYaw += ((moveForward > 0.0) ? -45 : 45);
                    }
                    else if (moveStrafe < 0.0) {
                        rotationYaw += ((moveForward > 0.0) ? 45 : -45);
                    }
                    moveStrafe = 0.0f;
                    moveForward = ((moveForward == 0.0f) ? moveForward : ((moveForward > 0.0) ? 1.0f : -1.0f));
                }
                moveStrafe = ((moveStrafe == 0.0f) ? moveStrafe : ((moveStrafe > 0.0) ? 1.0f : -1.0f));
                event.setX(moveForward * EntityUtil.getMaxSpeed() * Math.cos(Math.toRadians(rotationYaw + 90.0f)) + moveStrafe * EntityUtil.getMaxSpeed() * Math.sin(Math.toRadians(rotationYaw + 90.0f)));
                event.setZ(moveForward * EntityUtil.getMaxSpeed() * Math.sin(Math.toRadians(rotationYaw + 90.0f)) - moveStrafe * EntityUtil.getMaxSpeed() * Math.cos(Math.toRadians(rotationYaw + 90.0f)));
            }
        }
    }

    static {
        Speed.INSTANCE = new Speed();
    }

    public enum Mode
    {
        INSTANT,
    }
}