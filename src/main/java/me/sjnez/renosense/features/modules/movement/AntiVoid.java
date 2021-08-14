package me.sjnez.renosense.features.modules.movement;

import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class AntiVoid extends Module {

    public Setting<Double> yLevel = this.register( new Setting <> ( "YLevel" , 1.0 , 0.1 , 5.0 ));
    public Setting<Double> yForce = this.register( new Setting <> ( "YMotion" , 0.1 , 0.0 , 1.0 ));

    public AntiVoid() {
        super("AntiVoid", "Glitches you up from void.", Module.Category.MOVEMENT, false, false, false);
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (!mc.player.noClip && mc.player.posY <= yLevel.getValue()) {
            RayTraceResult trace = mc.world.rayTraceBlocks(mc.player.getPositionVector(), new Vec3d(mc.player.posX, 0.0, mc.player.posZ), false, false, false);
            if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK) {
                return;
            }
            mc.player.motionY = yForce.getValue();
            if (mc.player.getRidingEntity() != null) {
                mc.player.getRidingEntity().motionY = yForce.getValue();
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        return this.yLevel.getValue().toString() + ", " + this.yForce.getValue().toString();
    }
}


