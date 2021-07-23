package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.event.events.RenderEntityModelEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.ClickGui;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.ColorUtil;
import me.alpha432.oyvey.util.EntityUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Chams
        extends Module {
    public static Chams INSTANCE;
    public Setting<Boolean> chams = this.register(new Setting<Boolean>("Chams", false));
    public Setting<Boolean> throughWalls = this.register(new Setting<Boolean>("ThroughWalls", true));
    public Setting<Boolean> wireframeThroughWalls = this.register(new Setting<Boolean>("WireThroughWalls", true));
    public Setting<Boolean> glint = this.register(new Setting<Object>("Glint", Boolean.valueOf(false), v -> this.chams.getValue()));
    public Setting<Boolean> wireframe = this.register(new Setting<Boolean>("Wireframe", false));
    public Setting<Float> scale = this.register(new Setting<Float>("Scale", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(10.0f)));
    public Setting<Float> lineWidth = this.register(new Setting<Float>("LineWidth", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(3.0f)));
    public Setting<Boolean> rainbow = this.register(new Setting<Boolean>("Rainbow", false));
    public Setting<Boolean> xqz = this.register(new Setting<Object>("XQZ", Boolean.valueOf(false), v -> this.rainbow.getValue() == false && this.throughWalls.getValue() != false));
    public Setting<Integer> red = this.register(new Setting<Object>("Red", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.rainbow.getValue() == false));
    public Setting<Integer> green = this.register(new Setting<Object>("Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.rainbow.getValue() == false));
    public Setting<Integer> blue = this.register(new Setting<Object>("Blue", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.rainbow.getValue() == false));
    public Setting<Integer> alpha = this.register(new Setting<Integer>("Alpha", 255, 0, 255));
    public Setting<Integer> hiddenRed = this.register(new Setting<Object>("Hidden Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.xqz.getValue() != false && this.rainbow.getValue() == false));
    public Setting<Integer> hiddenGreen = this.register(new Setting<Object>("Hidden Green", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> this.xqz.getValue() != false && this.rainbow.getValue() == false));
    public Setting<Integer> hiddenBlue = this.register(new Setting<Object>("Hidden Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.xqz.getValue() != false && this.rainbow.getValue() == false));
    public Setting<Integer> hiddenAlpha = this.register(new Setting<Object>("Hidden Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> this.xqz.getValue() != false && this.rainbow.getValue() == false));
    public Map<EntityEnderCrystal, Float> scaleMap = new ConcurrentHashMap<EntityEnderCrystal, Float>();

    public Chams() {
        super("CrystalChams", "Modifies crystal rendering in different ways", Module.Category.RENDER, true, false, false);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        for (Entity crystal : Chams.mc.world.loadedEntityList) {
            if (!(crystal instanceof EntityEnderCrystal)) continue;
            if (!this.scaleMap.containsKey(crystal)) {
                this.scaleMap.put((EntityEnderCrystal) crystal, Float.valueOf(3.125E-4f));
            } else {
                this.scaleMap.put((EntityEnderCrystal) crystal, Float.valueOf(this.scaleMap.get(crystal).floatValue() + 3.125E-4f));
            }
            if (!(this.scaleMap.get(crystal).floatValue() >= 0.0625f * this.scale.getValue().floatValue()))
                continue;
            this.scaleMap.remove(crystal);
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketDestroyEntities) {
            SPacketDestroyEntities packet = event.getPacket();
            for (int id : packet.getEntityIDs()) {
                Entity entity = Chams.mc.world.getEntityByID(id);
                if (!(entity instanceof EntityEnderCrystal)) continue;
                this.scaleMap.remove(entity);
            }
        }
    }

    public void onRenderModel(RenderEntityModelEvent event) {
        if (event.getStage() != 0 || !(event.entity instanceof EntityEnderCrystal) || !this.wireframe.getValue().booleanValue()) {
            return;
        }
        Color color = this.rainbow.getValue() != false ? ColorUtil.rainbow((int) ClickGui.getInstance().rainbowHue.getValue()) : EntityUtil.getColor(event.entity, this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue(), false);
        boolean fancyGraphics = Chams.mc.gameSettings.fancyGraphics;
        Chams.mc.gameSettings.fancyGraphics = false;
        float gamma = Chams.mc.gameSettings.gammaSetting;
        Chams.mc.gameSettings.gammaSetting = 10000.0f;
        GL11.glPushMatrix();
        GL11.glPushAttrib(1048575);
        GL11.glPolygonMode(1032, 6913);
        GL11.glDisable(3553);
        GL11.glDisable(2896);
        if (this.wireframeThroughWalls.getValue().booleanValue()) {
            GL11.glDisable(2929);
        }
        GL11.glEnable(2848);
        GL11.glEnable(3042);
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color((float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f, (float) color.getBlue() / 255.0f, (float) color.getAlpha() / 255.0f);
        GlStateManager.glLineWidth(this.lineWidth.getValue().floatValue());
        event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
}
