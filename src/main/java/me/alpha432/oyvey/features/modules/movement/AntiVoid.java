package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class AntiVoid
        extends Module {
    public Setting<Double> yLevel = this.register(new Setting<Double>("YLevel", 1.0, 0.1, 5.0));
    public Setting<Double> yForce = this.register(new Setting<Double>("YMotion", 0.1, 0.0, 1.0));



    public AntiVoid() {
        super("AntiVoid", "Glitches you up from void.", Module.Category.MOVEMENT, false, false, false);
    }

    @Override
    public void onUpdate() {
        if (AntiVoid.fullNullCheck()) {
            return;
        }
        if (!AntiVoid.mc.player.noClip && AntiVoid.mc.player.posY <= yLevel.getValue()) {
            RayTraceResult trace = AntiVoid.mc.world.rayTraceBlocks(AntiVoid.mc.player.getPositionVector(), new Vec3d(AntiVoid.mc.player.posX, 0.0, AntiVoid.mc.player.posZ), false, false, false);
            if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK) {
                return;
            }
            AntiVoid.mc.player.motionY = yForce.getValue();
            if (AntiVoid.mc.player.getRidingEntity() != null) {
                AntiVoid.mc.player.getRidingEntity().motionY = yForce.getValue();
            }
        }
    }
}

