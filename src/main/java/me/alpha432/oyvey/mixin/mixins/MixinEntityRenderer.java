package me.alpha432.oyvey.mixin.mixins;

import me.alpha432.oyvey.event.events.PerspectiveEvent;
import me.alpha432.oyvey.features.modules.player.NoEntityTrace;
import me.alpha432.oyvey.features.modules.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = EntityRenderer.class, priority = 1001)
public class MixinEntityRenderer {

    @Shadow
    public float farPlaneDistance;

    @Shadow
    public double cameraZoom;

    @Shadow
    public double cameraYaw;

    @Shadow
    public double cameraPitch;

    @Shadow
    public int frameCount;

    @Shadow
    public void orientCamera(float partialTicks) {
    }

    @Shadow
    public void hurtCameraEffect(float partialTicks) {
    }

    @Shadow
    public void applyBobbing(float partialTicks) {
    }

    @Shadow
    public void enableLightmap() {
    }

    @Shadow
    public void disableLightmap() {
    }

    @Shadow
    public void updateFogColor(float partialTicks) {
    }

    @Shadow
    public void setupFog(int startCoords, float partialTicks) {
    }

    @Shadow
    public int debugViewDirection;

    @Shadow
    public int rendererUpdateCount;

    @Shadow
    public boolean debugView;

    protected MixinEntityRenderer(RenderManager renderManager) {
    }

    Minecraft mc = Minecraft.getMinecraft();

//    @Inject(method = "setupFog", at = @At(value = "HEAD"), cancellable = true)
//    public void onSetupFog(int startCoords, float partialTicks, CallbackInfo callbackInfo) {
//        FogRenderEvent event = new FogRenderEvent(true);
//        MinecraftForge.EVENT_BUS.post(event);
//
//        if (!event.getRender()) {
//            callbackInfo.cancel();
//        }
//    }




    @Inject(method={"updateLightmap"}, at={@At(value="HEAD")}, cancellable=true)
    private void updateLightmap(float partialTicks, CallbackInfo info) {
        if (NoRender.getInstance().isOn() && (NoRender.getInstance().skylight.getValue() == NoRender.Skylight.ENTITY || NoRender.getInstance().skylight.getValue() == NoRender.Skylight.ALL)) {
            info.cancel();
        }
    }

    @Inject(method={"hurtCameraEffect"}, at={@At(value="HEAD")}, cancellable=true)
    public void hurtCameraEffect(float ticks, CallbackInfo info) {
        if (NoRender.getInstance().hurtCam.getValue() && NoRender.getInstance().isOn()) {
            info.cancel();
        }
    }

    @Redirect(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onSetupCameraTransform(float fovy, float aspect, float zNear, float zFar) {
        PerspectiveEvent event = new PerspectiveEvent((float) this.mc.displayWidth / (float) this.mc.displayHeight);
        MinecraftForge.EVENT_BUS.post(event);
        Project.gluPerspective(fovy, event.getAspect(), zNear, zFar);
    }

    @Redirect(method={"getMouseOver"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcludingHook(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, com.google.common.base.Predicate<? super Entity> predicate) {
        if (NoEntityTrace.getInstance().isOn() && (!NoEntityTrace.getInstance().pickaxe.getValue().booleanValue() || this.mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe)) {
            return new ArrayList<Entity>();
        }
        return worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onRenderWorldPass(float fovy, float aspect, float zNear, float zFar) {
        PerspectiveEvent event = new PerspectiveEvent((float) this.mc.displayWidth / (float) this.mc.displayHeight);
        MinecraftForge.EVENT_BUS.post(event);
        Project.gluPerspective(fovy, event.getAspect(), zNear, zFar);
    }

    @Redirect(method = "renderCloudsCheck", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onRenderCloudsCheck(float fovy, float aspect, float zNear, float zFar) {
        PerspectiveEvent event = new PerspectiveEvent((float) this.mc.displayWidth / (float) this.mc.displayHeight);
        MinecraftForge.EVENT_BUS.post(event);
        Project.gluPerspective(fovy, event.getAspect(), zNear, zFar);
    }

}