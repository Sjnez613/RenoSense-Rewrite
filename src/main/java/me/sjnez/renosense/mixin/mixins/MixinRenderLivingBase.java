package me.sjnez.renosense.mixin.mixins;

import me.sjnez.renosense.features.modules.client.Colors;
import me.sjnez.renosense.features.modules.render.Chams;
import me.sjnez.renosense.util.EntityUtil;
import me.sjnez.renosense.util.RenderUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(value={RenderLivingBase.class})
public abstract class MixinRenderLivingBase<T extends EntityLivingBase>
extends Render<T> {
    private static final ResourceLocation glint = new ResourceLocation("textures/shinechams.png");

    public MixinRenderLivingBase(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn);
    }

    @Redirect(method={"renderModel"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"))
    private void renderModelHook(ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        Color visibleColor;
        boolean cancel = false;
        if (Chams.getInstance().isEnabled() && entityIn instanceof EntityPlayer && Chams.getInstance ( ).colored.getValue ( ) && ! Chams.getInstance ( ).textured.getValue ( ) ) {
            if (! Chams.getInstance ( ).textured.getValue ( ) ) {
                GL11.glPushAttrib( 1048575 );
                GL11.glDisable( 3008 );
                GL11.glDisable( 3553 );
                GL11.glDisable( 2896 );
                GL11.glEnable( 3042 );
                GL11.glBlendFunc( 770 , 771 );
                GL11.glLineWidth( 1.5f );
                GL11.glEnable( 2960 );
                if ( Chams.getInstance ( ).rainbow.getValue ( ) ) {
                    Color rainbowColor1 = Chams.getInstance ( ).colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor() : new Color(RenderUtil.getRainbow(Chams.getInstance().speed.getValue() * 100, 0, (float) Chams.getInstance ( ).saturation.getValue ( ) / 100.0f, (float) Chams.getInstance ( ).brightness.getValue ( ) / 100.0f));
                    Color rainbowColor = EntityUtil.getColor(entityIn, rainbowColor1.getRed(), rainbowColor1.getGreen(), rainbowColor1.getBlue(), Chams.getInstance().alpha.getValue(), true);
                    GL11.glDisable( 2929 );
                    GL11.glDepthMask( false );
                    GL11.glEnable( 10754 );
                    GL11.glColor4f( (float)rainbowColor.getRed() / 255.0f , (float)rainbowColor.getGreen() / 255.0f , (float)rainbowColor.getBlue() / 255.0f , (float) Chams.getInstance ( ).alpha.getValue ( ) / 255.0f );
                    modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                    GL11.glEnable( 2929 );
                    GL11.glDepthMask( true );
                } else if ( Chams.getInstance ( ).xqz.getValue ( ) ) {
                    Color hiddenColor = Chams.getInstance ( ).colorSync.getValue ( ) ? EntityUtil.getColor(entityIn, Chams.getInstance().hiddenRed.getValue(), Chams.getInstance().hiddenGreen.getValue(), Chams.getInstance().hiddenBlue.getValue(), Chams.getInstance().hiddenAlpha.getValue(), true) : EntityUtil.getColor(entityIn, Chams.getInstance().hiddenRed.getValue(), Chams.getInstance().hiddenGreen.getValue(), Chams.getInstance().hiddenBlue.getValue(), Chams.getInstance().hiddenAlpha.getValue(), true);
                    Color visibleColor2 = Chams.getInstance ( ).colorSync.getValue ( ) ? EntityUtil.getColor(entityIn, Chams.getInstance().red.getValue(), Chams.getInstance().green.getValue(), Chams.getInstance().blue.getValue(), Chams.getInstance().alpha.getValue(), true) : EntityUtil.getColor(entityIn, Chams.getInstance().red.getValue(), Chams.getInstance().green.getValue(), Chams.getInstance().blue.getValue(), Chams.getInstance().alpha.getValue(), true);
                    GL11.glDisable( 2929 );
                    GL11.glDepthMask( false );
                    GL11.glEnable( 10754 );
                    GL11.glColor4f( (float)hiddenColor.getRed() / 255.0f , (float)hiddenColor.getGreen() / 255.0f , (float)hiddenColor.getBlue() / 255.0f , (float) Chams.getInstance ( ).alpha.getValue ( ) / 255.0f );
                    modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                    GL11.glEnable( 2929 );
                    GL11.glDepthMask( true );
                    GL11.glColor4f( (float)visibleColor2.getRed() / 255.0f , (float)visibleColor2.getGreen() / 255.0f , (float)visibleColor2.getBlue() / 255.0f , (float) Chams.getInstance ( ).alpha.getValue ( ) / 255.0f );
                    modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                } else {
                    visibleColor = Chams.getInstance ( ).colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor() : EntityUtil.getColor(entityIn, Chams.getInstance().red.getValue(), Chams.getInstance().green.getValue(), Chams.getInstance().blue.getValue(), Chams.getInstance().alpha.getValue(), true);
                    GL11.glDisable( 2929 );
                    GL11.glDepthMask( false );
                    GL11.glEnable( 10754 );
                    GL11.glColor4f( (float)visibleColor.getRed() / 255.0f , (float)visibleColor.getGreen() / 255.0f , (float)visibleColor.getBlue() / 255.0f , (float) Chams.getInstance ( ).alpha.getValue ( ) / 255.0f );
                    modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                    GL11.glEnable( 2929 );
                    GL11.glDepthMask( true );
                }
                GL11.glEnable( 3042 );
                GL11.glEnable( 2896 );
                GL11.glEnable( 3553 );
                GL11.glEnable( 3008 );
                GL11.glPopAttrib();
            }
        } else if ( Chams.getInstance ( ).textured.getValue ( ) ) {
            GL11.glDisable( 2929 );
            GL11.glDepthMask( false );
            visibleColor = Chams.getInstance ( ).colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor() : EntityUtil.getColor(entityIn, Chams.getInstance().red.getValue(), Chams.getInstance().green.getValue(), Chams.getInstance().blue.getValue(), Chams.getInstance().alpha.getValue(), true);
            GL11.glColor4f( (float)visibleColor.getRed() / 255.0f , (float)visibleColor.getGreen() / 255.0f , (float)visibleColor.getBlue() / 255.0f , (float) Chams.getInstance ( ).alpha.getValue ( ) / 255.0f );
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GL11.glEnable( 2929 );
            GL11.glDepthMask( true );
        } else {
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Inject(method={"doRender"}, at={@At(value="HEAD")})
    public void doRenderPre(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (Chams.getInstance().isEnabled() && ! Chams.getInstance ( ).colored.getValue ( ) && entity != null) {
            GL11.glEnable( 32823 );
            GL11.glPolygonOffset( 1.0f , -1100000.0f );
        }
    }

    @Inject(method={"doRender"}, at={@At(value="RETURN")})
    public void doRenderPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (Chams.getInstance().isEnabled() && ! Chams.getInstance ( ).colored.getValue ( ) && entity != null) {
            GL11.glPolygonOffset( 1.0f , 1000000.0f );
            GL11.glDisable( 32823 );
        }
    }
}

