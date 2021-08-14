package me.sjnez.renosense.features.modules.render;

import me.sjnez.renosense.event.events.Render3DEvent;
import me.sjnez.renosense.event.events.RenderEntityModelEvent;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.modules.client.Colors;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.EntityUtil;
import me.sjnez.renosense.util.RenderUtil;
import me.sjnez.renosense.util.Util;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ESP
        extends Module {
    private static ESP INSTANCE = new ESP();
    private final Setting<Mode> mode = this.register( new Setting <> ( "Mode" , Mode.OUTLINE ));
    private final Setting<Boolean> colorSync = this.register( new Setting <> ( "Sync" , false ));
    private final Setting<Boolean> players = this.register( new Setting <> ( "Players" , true ));
    private final Setting<Boolean> animals = this.register( new Setting <> ( "Animals" , false ));
    private final Setting<Boolean> mobs = this.register( new Setting <> ( "Mobs" , false ));
    private final Setting<Boolean> items = this.register( new Setting <> ( "Items" , false ));
    private final Setting<Boolean> xporbs = this.register( new Setting <> ( "XpOrbs" , false ));
    private final Setting<Boolean> xpbottles = this.register( new Setting <> ( "XpBottles" , false ));
    private final Setting<Boolean> pearl = this.register( new Setting <> ( "Pearls" , false ));
    private final Setting<Integer> red = this.register( new Setting <> ( "Red" , 255 , 0 , 255 ));
    private final Setting<Integer> green = this.register( new Setting <> ( "Green" , 255 , 0 , 255 ));
    private final Setting<Integer> blue = this.register( new Setting <> ( "Blue" , 255 , 0 , 255 ));
    private final Setting<Integer> boxAlpha = this.register( new Setting <> ( "BoxAlpha" , 120 , 0 , 255 ));
    private final Setting<Integer> alpha = this.register( new Setting <> ( "Alpha" , 255 , 0 , 255 ));
    private final Setting<Float> lineWidth = this.register( new Setting <> ( "LineWidth" , 2.0f , 0.1f , 5.0f ));
    private final Setting<Boolean> colorFriends = this.register( new Setting <> ( "Friends" , true ));
    private final Setting<Boolean> self = this.register( new Setting <> ( "Self" , true ));
    private final Setting<Boolean> onTop = this.register( new Setting <> ( "onTop" , true ));
    private final Setting<Boolean> invisibles = this.register( new Setting <> ( "Invisibles" , false ));

    public ESP() {
        super("ESP", "Renders a nice ESP.", Module.Category.RENDER, false, false, false);
        this.setInstance();
    }

    public static ESP getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ESP();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        AxisAlignedBB bb;
        Vec3d interp;
        int i;
        if ( this.items.getValue ( ) ) {
            i = 0;
            for (Entity entity : ESP.mc.world.loadedEntityList) {
                if (!(entity instanceof EntityItem) || !(ESP.mc.player.getDistanceSq(entity) < 2500.0)) continue;
                interp = EntityUtil.getInterpolatedRenderPos(entity, Util.mc.getRenderPartialTicks());
                bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0 - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1 - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ + interp.z);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GL11.glEnable(2848);
                GL11.glHint(3154, 4354);
                GL11.glLineWidth(1.0f);
                RenderGlobal.renderFilledBox(bb, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getRed() / 255.0f : (float) this.red.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getGreen() / 255.0f : (float) this.green.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getBlue() / 255.0f : (float) this.blue.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getAlpha() : (float) this.boxAlpha.getValue ( ) / 255.0f);
                GL11.glDisable(2848);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                RenderUtil.drawBlockOutline(bb, this.colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor() : new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()), 1.0f);
                if (++i < 50) continue;
                break;
            }
        }
        if ( this.xporbs.getValue ( ) ) {
            i = 0;
            for (Entity entity : ESP.mc.world.loadedEntityList) {
                if (!(entity instanceof EntityXPOrb) || !(ESP.mc.player.getDistanceSq(entity) < 2500.0)) continue;
                interp = EntityUtil.getInterpolatedRenderPos(entity, Util.mc.getRenderPartialTicks());
                bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0 - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1 - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ + interp.z);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GL11.glEnable(2848);
                GL11.glHint(3154, 4354);
                GL11.glLineWidth(1.0f);
                RenderGlobal.renderFilledBox(bb, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getRed() / 255.0f : (float) this.red.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getGreen() / 255.0f : (float) this.green.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getBlue() / 255.0f : (float) this.blue.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getAlpha() / 255.0f : (float) this.boxAlpha.getValue ( ) / 255.0f);
                GL11.glDisable(2848);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                RenderUtil.drawBlockOutline(bb, this.colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor() : new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()), 1.0f);
                if (++i < 50) continue;
                break;
            }
        }
        if ( this.pearl.getValue ( ) ) {
            i = 0;
            for (Entity entity : ESP.mc.world.loadedEntityList) {
                if (!(entity instanceof EntityEnderPearl) || !(ESP.mc.player.getDistanceSq(entity) < 2500.0)) continue;
                interp = EntityUtil.getInterpolatedRenderPos(entity, Util.mc.getRenderPartialTicks());
                bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0 - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1 - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ + interp.z);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GL11.glEnable(2848);
                GL11.glHint(3154, 4354);
                GL11.glLineWidth(1.0f);
                RenderGlobal.renderFilledBox(bb, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getRed() / 255.0f : (float) this.red.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getGreen() / 255.0f : (float) this.green.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getBlue() / 255.0f : (float) this.blue.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getAlpha() / 255.0f : (float) this.boxAlpha.getValue ( ) / 255.0f);
                GL11.glDisable(2848);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                RenderUtil.drawBlockOutline(bb, this.colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor() : new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()), 1.0f);
                if (++i < 50) continue;
                break;
            }
        }
        if ( this.xpbottles.getValue ( ) ) {
            i = 0;
            for (Entity entity : ESP.mc.world.loadedEntityList) {
                if (!(entity instanceof EntityExpBottle) || !(ESP.mc.player.getDistanceSq(entity) < 2500.0)) continue;
                interp = EntityUtil.getInterpolatedRenderPos(entity, Util.mc.getRenderPartialTicks());
                bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0 - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1 - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ + interp.z);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GL11.glEnable(2848);
                GL11.glHint(3154, 4354);
                GL11.glLineWidth(1.0f);
                RenderGlobal.renderFilledBox(bb, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getRed() / 255.0f : (float) this.red.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getGreen() / 255.0f : (float) this.green.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getBlue() / 255.0f : (float) this.blue.getValue ( ) / 255.0f, this.colorSync.getValue ( ) ? (float) Colors.INSTANCE.getCurrentColor().getAlpha() / 255.0f : (float) this.boxAlpha.getValue ( ) / 255.0f);
                GL11.glDisable(2848);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                RenderUtil.drawBlockOutline(bb, this.colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor() : new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()), 1.0f);
                if (++i < 50) continue;
                break;
            }
        }
    }

    public void onRenderModel(RenderEntityModelEvent event) {
        if (event.getStage() != 0 || event.entity == null || event.entity.isInvisible() && ! this.invisibles.getValue ( ) || ! this.self.getValue ( ) && event.entity.equals(ESP.mc.player) || ! this.players.getValue ( ) && event.entity instanceof EntityPlayer || ! this.animals.getValue ( ) && EntityUtil.isPassive(event.entity) || ! this.mobs.getValue ( ) && !EntityUtil.isPassive(event.entity) && !(event.entity instanceof EntityPlayer)) {
            return;
        }
        Color color = this.colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor() : EntityUtil.getColor(event.entity, this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue(), this.colorFriends.getValue());
        boolean fancyGraphics = ESP.mc.gameSettings.fancyGraphics;
        ESP.mc.gameSettings.fancyGraphics = false;
        float gamma = ESP.mc.gameSettings.gammaSetting;
        ESP.mc.gameSettings.gammaSetting = 10000.0f;
        if (!(! this.onTop.getValue ( ) || Chams.getInstance().isEnabled() && Chams.getInstance ( ).colored.getValue ( ) )) {
            event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale);
        }
        if (this.mode.getValue() == Mode.OUTLINE) {
            RenderUtil.renderOne( this.lineWidth.getValue ( ) );
            event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale);
            GlStateManager.glLineWidth( this.lineWidth.getValue ( ) );
            RenderUtil.renderTwo();
            event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale);
            GlStateManager.glLineWidth( this.lineWidth.getValue ( ) );
            RenderUtil.renderThree();
            RenderUtil.renderFour(color);
            event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale);
            GlStateManager.glLineWidth( this.lineWidth.getValue ( ) );
            RenderUtil.renderFive();
        } else {
            GL11.glPushMatrix();
            GL11.glPushAttrib(1048575);
            if (this.mode.getValue() == Mode.WIREFRAME) {
                GL11.glPolygonMode(1032, 6913);
            } else {
                GL11.glPolygonMode(1028, 6913);
            }
            GL11.glDisable(3553);
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            GL11.glEnable(2848);
            GL11.glEnable(3042);
            GlStateManager.blendFunc(770, 771);
            GlStateManager.color((float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f, (float) color.getBlue() / 255.0f, (float) color.getAlpha() / 255.0f);
            GlStateManager.glLineWidth( this.lineWidth.getValue ( ) );
            event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
        if (!( this.onTop.getValue ( ) || Chams.getInstance().isEnabled() && Chams.getInstance ( ).colored.getValue ( ) )) {
            event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale);
        }
        try {
            ESP.mc.gameSettings.fancyGraphics = fancyGraphics;
            ESP.mc.gameSettings.gammaSetting = gamma;
        } catch (Exception exception) {
            // empty catch block
        }
        event.setCanceled(true);
    }

    public enum Mode {
        WIREFRAME,
        OUTLINE

    }
}

