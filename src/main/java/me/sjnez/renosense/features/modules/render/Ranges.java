package me.sjnez.renosense.features.modules.render;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.event.events.Render3DEvent;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.EntityUtil;
import me.sjnez.renosense.util.RenderUtil;
import me.sjnez.renosense.util.Util;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class Ranges
        extends Module {
    private final Setting<Boolean> hitSpheres = this.register( new Setting <> ( "HitSpheres" , false ));
    private final Setting<Boolean> circle = this.register( new Setting <> ( "Circle" , true ));
    private final Setting<Boolean> ownSphere = this.register(new Setting<Object>("OwnSphere", Boolean.FALSE , v -> this.hitSpheres.getValue()));
    private final Setting<Boolean> raytrace = this.register(new Setting<Object>("RayTrace", Boolean.FALSE , v -> this.circle.getValue()));
    private final Setting<Float> lineWidth = this.register( new Setting <> ( "LineWidth" , 1.5f , 0.1f , 5.0f ));
    private final Setting<Double> radius = this.register( new Setting <> ( "Radius" , 4.5 , 0.1 , 8.0 ));

    public Ranges() {
        super("Ranges", "Draws a circle around the player.", Module.Category.RENDER, false, false, false);
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if ( this.circle.getValue ( ) ) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderManager renderManager = Util.mc.getRenderManager();
            float hue = (float) (System.currentTimeMillis() % 7200L) / 7200.0f;
            Color color = new Color(Color.HSBtoRGB(hue, 1.0f, 1.0f));
            ArrayList<Vec3d> hVectors = new ArrayList <> ( );
            double x = Ranges.mc.player.lastTickPosX + (Ranges.mc.player.posX - Ranges.mc.player.lastTickPosX) * (double) event.getPartialTicks() - renderManager.renderPosX;
            double y = Ranges.mc.player.lastTickPosY + (Ranges.mc.player.posY - Ranges.mc.player.lastTickPosY) * (double) event.getPartialTicks() - renderManager.renderPosY;
            double z = Ranges.mc.player.lastTickPosZ + (Ranges.mc.player.posZ - Ranges.mc.player.lastTickPosZ) * (double) event.getPartialTicks() - renderManager.renderPosZ;
            GL11.glLineWidth( this.lineWidth.getValue ( ) );
            GL11.glBegin(1);
            for (int i = 0; i <= 360; ++i) {
                Vec3d vec = new Vec3d(x + Math.sin((double) i * Math.PI / 180.0) * this.radius.getValue(), y + 0.1, z + Math.cos((double) i * Math.PI / 180.0) * this.radius.getValue());
                RayTraceResult result = Ranges.mc.world.rayTraceBlocks(new Vec3d(Ranges.mc.player.posX, Ranges.mc.player.posY + (double) Ranges.mc.player.getEyeHeight(), Ranges.mc.player.posZ), vec, false, false, true);
                if (result != null && this.raytrace.getValue ( ) ) {
                    RenoSense.LOGGER.info("raytrace was not null");
                    hVectors.add(result.hitVec);
                    continue;
                }
                hVectors.add(vec);
            }
            for (int j = 0; j < hVectors.size() - 1; ++j) {
                GL11.glColor4f((float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f, (float) color.getBlue() / 255.0f, (float) color.getAlpha() / 255.0f);
                GL11.glVertex3d(hVectors.get(j).x, hVectors.get(j).y, hVectors.get(j).z);
                GL11.glVertex3d(hVectors.get(j + 1).x, hVectors.get(j + 1).y, hVectors.get(j + 1).z);
                color = new Color(Color.HSBtoRGB(hue += 0.0027777778f, 1.0f, 1.0f));
            }
            GL11.glEnd();
            GlStateManager.resetColor();
            GlStateManager.disableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
        if ( this.hitSpheres.getValue ( ) ) {
            for (EntityPlayer player : Ranges.mc.world.playerEntities) {
                if (player == null || player.equals(Ranges.mc.player) && ! this.ownSphere.getValue ( ) )
                    continue;
                Vec3d interpolated = EntityUtil.interpolateEntity(player, event.getPartialTicks());
                if (RenoSense.friendManager.isFriend(player.getName())) {
                    GL11.glColor4f(0.15f, 0.15f, 1.0f, 1.0f);
                } else if (Ranges.mc.player.getDistance(player) >= 64.0f) {
                    GL11.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
                } else {
                    GL11.glColor4f(1.0f, Ranges.mc.player.getDistance(player) / 150.0f, 0.0f, 1.0f);
                }
                RenderUtil.drawSphere(interpolated.x, interpolated.y, interpolated.z, this.radius.getValue().floatValue(), 20, 15);
            }
        }
    }
}

