package me.alpha432.oyvey.mixin.mixins;

import me.alpha432.oyvey.features.modules.client.ClickGui;
import me.alpha432.oyvey.features.modules.render.Wireframe;
import me.alpha432.oyvey.util.ColorUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.client.renderer.entity.RenderEnderCrystal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin({RenderEnderCrystal.class})
public class MixinRenderEnderCrystal extends Render<EntityEnderCrystal> {
    @Shadow
    private static final ResourceLocation ENDER_CRYSTAL_TEXTURES = new ResourceLocation("textures/entity/endercrystal/endercrystal.png");

    @Shadow
    private final ModelBase modelEnderCrystal = new ModelEnderCrystal(0.0F, true);

    @Shadow
    private final ModelBase modelEnderCrystalNoBase = new ModelEnderCrystal(0.0F, false);

    protected MixinRenderEnderCrystal(RenderManager renderManager) {
        super(renderManager);
    }

    @Overwrite
    public void doRender(EntityEnderCrystal entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float f = entity.innerRotation + partialTicks;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        bindTexture(ENDER_CRYSTAL_TEXTURES);
        float f1 = MathHelper.sin(f * 0.2F) / 2.0F + 0.5F;
        f1 += f1 * f1;
        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(getTeamColor(entity));
        }
        if (Wireframe.getINSTANCE().isOn() && (Wireframe.getINSTANCE()).crystals.getValue().booleanValue()) {
            float red = (ClickGui.getInstance()).red.getValue().intValue() / 255.0F;
            float green = (ClickGui.getInstance()).green.getValue().intValue() / 255.0F;
            float blue = (ClickGui.getInstance()).blue.getValue().intValue() / 255.0F;
            if ((Wireframe.getINSTANCE()).cMode.getValue().equals(Wireframe.RenderMode.WIREFRAME) && (Wireframe.getINSTANCE()).crystalModel.getValue().booleanValue())
                this.modelEnderCrystalNoBase.render(entity, 0.0F, f * 3.0F, f1 * 0.2F, 0.0F, 0.0F, 0.0625F);
            GlStateManager.pushMatrix();
            GL11.glPushAttrib(1048575);
            if ((Wireframe.getINSTANCE()).cMode.getValue().equals(Wireframe.RenderMode.WIREFRAME))
                GL11.glPolygonMode(1032, 6913);
            GL11.glDisable(3553);
            GL11.glDisable(2896);
            if ((Wireframe.getINSTANCE()).cMode.getValue().equals(Wireframe.RenderMode.WIREFRAME))
                GL11.glEnable(2848);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glColor4f((ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRed() / 255.0F) : red, (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getGreen() / 255.0F) : green, (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getBlue() / 255.0F) : blue, (Wireframe.getINSTANCE()).cAlpha.getValue().floatValue() / 255.0F);
            if ((Wireframe.getINSTANCE()).cMode.getValue().equals(Wireframe.RenderMode.WIREFRAME))
                GL11.glLineWidth((Wireframe.getINSTANCE()).crystalLineWidth.getValue().floatValue());
            this.modelEnderCrystalNoBase.render(entity, 0.0F, f * 3.0F, f1 * 0.2F, 0.0F, 0.0F, 0.0625F);
            GL11.glDisable(2896);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glColor4f((ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRed() / 255.0F) : red, (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getGreen() / 255.0F) : green, (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getBlue() / 255.0F) : blue, (Wireframe.getINSTANCE()).cAlpha.getValue().floatValue() / 255.0F);
            if ((Wireframe.getINSTANCE()).cMode.getValue().equals(Wireframe.RenderMode.WIREFRAME))
                GL11.glLineWidth((Wireframe.getINSTANCE()).crystalLineWidth.getValue().floatValue());
            this.modelEnderCrystalNoBase.render(entity, 0.0F, f * 3.0F, f1 * 0.2F, 0.0F, 0.0F, 0.0625F);
            GlStateManager.enableDepth();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        } else {
            this.modelEnderCrystalNoBase.render(entity, 0.0F, f * 3.0F, f1 * 0.2F, 0.0F, 0.0F, 0.0625F);
        }
        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }
        GlStateManager.popMatrix();
        BlockPos blockpos = entity.getBeamTarget();
        if (blockpos != null) {
            bindTexture(RenderDragon.ENDERCRYSTAL_BEAM_TEXTURES);
            float f2 = blockpos.getX() + 0.5F;
            float f3 = blockpos.getY() + 0.5F;
            float f4 = blockpos.getZ() + 0.5F;
            double d0 = f2 - entity.posX;
            double d1 = f3 - entity.posY;
            double d2 = f4 - entity.posZ;
            RenderDragon.renderCrystalBeams(x + d0, y - 0.3D + (f1 * 0.4F) + d1, z + d2, partialTicks, f2, f3, f4, entity.innerRotation, entity.posX, entity.posY, entity.posZ);
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    protected ResourceLocation getEntityTexture(EntityEnderCrystal entityEnderCrystal) {
        return null;
    }
}
