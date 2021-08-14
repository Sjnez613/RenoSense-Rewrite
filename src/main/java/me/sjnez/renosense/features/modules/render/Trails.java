package me.sjnez.renosense.features.modules.render;

import me.sjnez.renosense.event.events.Render3DEvent;
import me.sjnez.renosense.event.events.UpdateWalkingPlayerEvent;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trails
        extends Module {
    private final Setting<Float> lineWidth = this.register( new Setting <> ( "LineWidth" , 1.5f , 0.1f , 5.0f ));
    private final Setting<Integer> red = this.register( new Setting <> ( "Red" , 0 , 0 , 255 ));
    private final Setting<Integer> green = this.register( new Setting <> ( "Green" , 255 , 0 , 255 ));
    private final Setting<Integer> blue = this.register( new Setting <> ( "Blue" , 0 , 0 , 255 ));
    private final Setting<Integer> alpha = this.register( new Setting <> ( "Alpha" , 255 , 0 , 255 ));
    private final Map<Entity, List<Vec3d>> renderMap = new HashMap <> ( );

    public Trails() {
        super("Trails", "Draws trails on projectiles", Module.Category.RENDER, true, false, false);
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        for (Entity entity : Trails.mc.world.loadedEntityList) {
            if (!(entity instanceof EntityThrowable) && !(entity instanceof EntityArrow)) continue;
            List<Vec3d> vectors = this.renderMap.get(entity) != null ? this.renderMap.get(entity) : new ArrayList <> ( );
            vectors.add(new Vec3d(entity.posX, entity.posY, entity.posZ));
            this.renderMap.put(entity, vectors);
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        for (Entity entity : Trails.mc.world.loadedEntityList) {
            if (!this.renderMap.containsKey(entity)) continue;
            GlStateManager.pushMatrix();
            RenderUtil.GLPre( this.lineWidth.getValue ( ) );
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GL11.glColor4f((float) this.red.getValue ( ) / 255.0f, (float) this.green.getValue ( ) / 255.0f, (float) this.blue.getValue ( ) / 255.0f, (float) this.alpha.getValue ( ) / 255.0f);
            GL11.glLineWidth( this.lineWidth.getValue ( ) );
            GL11.glBegin(1);
            for (int i = 0; i < this.renderMap.get(entity).size() - 1; ++i) {
                GL11.glVertex3d(this.renderMap.get(entity).get(i).x, this.renderMap.get(entity).get(i).y, this.renderMap.get(entity).get(i).z);
                GL11.glVertex3d(this.renderMap.get(entity).get(i + 1).x, this.renderMap.get(entity).get(i + 1).y, this.renderMap.get(entity).get(i + 1).z);
            }
            GL11.glEnd();
            GlStateManager.resetColor();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            RenderUtil.GlPost();
            GlStateManager.popMatrix();
        }
    }
}

