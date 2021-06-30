package me.alpha432.oyvey.mixin.mixins;

import me.alpha432.oyvey.features.modules.movement.Speed;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ RenderGlobal.class })
public abstract class MixinRenderGlobal
{
    @Redirect(method = { "setupTerrain" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ChunkRenderContainer;initialize(DDD)V"))
    public void initializeHook(final ChunkRenderContainer chunkRenderContainer, final double viewEntityXIn, final double viewEntityYIn, final double viewEntityZIn) {
        double y = viewEntityYIn;
        if (Speed.getInstance().isOn() && Speed.getInstance().noShake.getValue() && Speed.getInstance().mode.getValue() != Speed.Mode.INSTANT && Speed.getInstance().antiShake) {
            y = Speed.getInstance().startY;
        }
        chunkRenderContainer.initialize(viewEntityXIn, y, viewEntityZIn);
    }

    @Redirect(method = { "renderEntities" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;setRenderPosition(DDD)V"))
    public void setRenderPositionHook(final RenderManager renderManager, final double renderPosXIn, final double renderPosYIn, final double renderPosZIn) {
        double y = renderPosYIn;
        if (Speed.getInstance().isOn() && Speed.getInstance().noShake.getValue() && Speed.getInstance().mode.getValue() != Speed.Mode.INSTANT && Speed.getInstance().antiShake) {
            y = Speed.getInstance().startY;
        }
        renderManager.setRenderPosition(renderPosXIn, TileEntityRendererDispatcher.staticPlayerY = y, renderPosZIn);
    }

    @Redirect(method = { "drawSelectionBox" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/AxisAlignedBB;offset(DDD)Lnet/minecraft/util/math/AxisAlignedBB;"))
    public AxisAlignedBB offsetHook(final AxisAlignedBB axisAlignedBB, final double x, final double y, final double z) {
        double yIn = y;
        if (Speed.getInstance().isOn() && Speed.getInstance().noShake.getValue() && Speed.getInstance().mode.getValue() != Speed.Mode.INSTANT && Speed.getInstance().antiShake) {
            yIn = Speed.getInstance().startY;
        }
        return axisAlignedBB.offset(x, y, z);
    }
}