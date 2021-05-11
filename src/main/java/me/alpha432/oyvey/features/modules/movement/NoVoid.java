package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class NoVoid
        extends Module {
    public NoVoid() {
        super("NoVoid", "Glitches you up from void.", Module.Category.MOVEMENT, false, false, false);
    }

    @Override
    public void onUpdate() {
        if (NoVoid.fullNullCheck()) {
            return;
        }
        if (!NoVoid.mc.player.noClip && NoVoid.mc.player.posY <= 0.0) {
            RayTraceResult trace = NoVoid.mc.world.rayTraceBlocks(NoVoid.mc.player.getPositionVector(), new Vec3d(NoVoid.mc.player.posX, 0.0, NoVoid.mc.player.posZ), false, false, false);
            if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK) {
                return;
            }
            NoVoid.mc.player.setVelocity(0.0, 0.0, 0.0);
            if (NoVoid.mc.player.getRidingEntity() != null) {
                NoVoid.mc.player.getRidingEntity().setVelocity(0.0, 0.0, 0.0);
            }
        }
    }
}

