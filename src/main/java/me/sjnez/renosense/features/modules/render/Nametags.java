package me.sjnez.renosense.features.modules.render;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.event.events.Render3DEvent;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.modules.client.Colors;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.DamageUtil;
import me.sjnez.renosense.util.EntityUtil;
import me.sjnez.renosense.util.RotationUtil;
import me.sjnez.renosense.util.Util;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;

public class Nametags
        extends Module {
    private static Nametags INSTANCE = new Nametags();
    private final Setting<Boolean> health = this.register( new Setting <> ( "Health" , true ));
    private final Setting<Boolean> armor = this.register( new Setting <> ( "Armor" , true ));
    private final Setting<Float> scaling = this.register( new Setting <> ( "Size" , 0.3f , 0.1f , 20.0f ));
    private final Setting<Boolean> invisibles = this.register( new Setting <> ( "Invisibles" , false ));
    private final Setting<Boolean> ping = this.register( new Setting <> ( "Ping" , true ));
    private final Setting<Boolean> totemPops = this.register( new Setting <> ( "TotemPops" , true ));
    private final Setting<Boolean> gamemode = this.register( new Setting <> ( "Gamemode" , false ));
    private final Setting<Boolean> entityID = this.register( new Setting <> ( "ID" , false ));
    private final Setting<Boolean> rect = this.register( new Setting <> ( "Rectangle" , true ));
    private final Setting<Boolean> outline = this.register(new Setting<Object>("Outline", Boolean.FALSE , v -> this.rect.getValue()));
    private final Setting<Boolean> colorSync = this.register(new Setting<Object>("Sync", Boolean.FALSE , v -> this.outline.getValue()));
    private final Setting<Integer> redSetting = this.register(new Setting<Object>("Red", 255 , 0 , 255 , v -> this.outline.getValue()));
    private final Setting<Integer> greenSetting = this.register(new Setting<Object>("Green", 255 , 0 , 255 , v -> this.outline.getValue()));
    private final Setting<Integer> blueSetting = this.register(new Setting<Object>("Blue", 255 , 0 , 255 , v -> this.outline.getValue()));
    private final Setting<Integer> alphaSetting = this.register(new Setting<Object>("Alpha", 255 , 0 , 255 , v -> this.outline.getValue()));
    private final Setting<Float> lineWidth = this.register(new Setting<Object>("LineWidth", 1.5f , 0.1f , 5.0f , v -> this.outline.getValue()));
    private final Setting<Boolean> sneak = this.register( new Setting <> ( "SneakColor" , false ));
    private final Setting<Boolean> heldStackName = this.register( new Setting <> ( "StackName" , false ));
    private final Setting<Boolean> whiter = this.register( new Setting <> ( "White" , false ));
    private final Setting<Boolean> onlyFov = this.register( new Setting <> ( "OnlyFov" , false ));
    private final Setting<Boolean> scaleing = this.register( new Setting <> ( "Scale" , false ));
    private final Setting<Float> factor = this.register(new Setting<Object>("Factor", 0.3f , 0.1f , 1.0f , v -> this.scaleing.getValue()));
    private final Setting<Boolean> smartScale = this.register(new Setting<Object>("SmartScale", Boolean.FALSE , v -> this.scaleing.getValue()));

    public Nametags() {
        super("Nametags", "Better Nametags", Module.Category.RENDER, false, false, false);
        this.setInstance();
    }

    public static Nametags getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Nametags();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!Nametags.fullNullCheck()) {
            for (EntityPlayer player : Nametags.mc.world.playerEntities) {
                if (player == null || player.equals(Nametags.mc.player) || !player.isEntityAlive() || player.isInvisible() && ! this.invisibles.getValue ( ) || this.onlyFov.getValue ( ) && !RotationUtil.isInFov(player))
                    continue;
                double x = this.interpolate(player.lastTickPosX, player.posX, event.getPartialTicks()) - Nametags.mc.getRenderManager().renderPosX;
                double y = this.interpolate(player.lastTickPosY, player.posY, event.getPartialTicks()) - Nametags.mc.getRenderManager().renderPosY;
                double z = this.interpolate(player.lastTickPosZ, player.posZ, event.getPartialTicks()) - Nametags.mc.getRenderManager().renderPosZ;
                this.renderNameTag(player, x, y, z, event.getPartialTicks());
            }
        }
    }

    public void drawRect(float x, float y, float w, float h, int color) {
        float alpha = (float) (color >> 24 & 0xFF) / 255.0f;
        float red = (float) (color >> 16 & 0xFF) / 255.0f;
        float green = (float) (color >> 8 & 0xFF) / 255.0f;
        float blue = (float) (color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth( this.lineWidth.getValue ( ) );
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, y, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public void drawOutlineRect(float x, float y, float w, float h, int color) {
        float alpha = (float) (color >> 24 & 0xFF) / 255.0f;
        float red = (float) (color >> 16 & 0xFF) / 255.0f;
        float green = (float) (color >> 8 & 0xFF) / 255.0f;
        float blue = (float) (color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth( this.lineWidth.getValue ( ) );
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        bufferbuilder.begin(2, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, y, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void renderNameTag(EntityPlayer player, double x, double y, double z, float delta) {
        double tempY = y;
        tempY += player.isSneaking() ? 0.5 : 0.7;
        Entity camera = Util.mc.getRenderViewEntity();
        assert (camera != null);
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = this.interpolate(camera.prevPosX, camera.posX, delta);
        camera.posY = this.interpolate(camera.prevPosY, camera.posY, delta);
        camera.posZ = this.interpolate(camera.prevPosZ, camera.posZ, delta);
        String displayTag = this.getDisplayTag(player);
        double distance = camera.getDistance(x + Nametags.mc.getRenderManager().viewerPosX, y + Nametags.mc.getRenderManager().viewerPosY, z + Nametags.mc.getRenderManager().viewerPosZ);
        int width = this.renderer.getStringWidth(displayTag) / 2;
        double scale = (0.0018 + (double) this.scaling.getValue ( ) * (distance * (double) this.factor.getValue ( ) )) / 1000.0;
        if (distance <= 8.0 && this.smartScale.getValue ( ) ) {
            scale = 0.0245;
        }
        if (! this.scaleing.getValue ( ) ) {
            scale = (double) this.scaling.getValue ( ) / 100.0;
        }
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.translate((float) x, (float) tempY + 1.4f, (float) z);
        GlStateManager.rotate(-Nametags.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(Nametags.mc.getRenderManager().playerViewX, Nametags.mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableBlend();
        if ( this.rect.getValue ( ) ) {
            drawRect(-width - 2, -(this.renderer.getFontHeight() + 1), (float) width + 2.0f, 1.5f, 0x55000000);
            if (this.outline.getValue()) {
                final int color = this.colorSync.getValue() ? Colors.INSTANCE.getCurrentColorHex() : new Color(this.redSetting.getValue(), this.greenSetting.getValue(), this.blueSetting.getValue(), this.alphaSetting.getValue()).getRGB();
                this.drawOutlineRect((float) (-width - 2), (float) (-(Nametags.mc.fontRenderer.FONT_HEIGHT + 1)), width + 2.0f, 1.5f, color);
            }
        }
        GlStateManager.disableBlend();
        ItemStack renderMainHand = player.getHeldItemMainhand().copy();
        if (renderMainHand.hasEffect() && (renderMainHand.getItem() instanceof ItemTool || renderMainHand.getItem() instanceof ItemArmor)) {
            renderMainHand.stackSize = 1;
        }
        if ( this.heldStackName.getValue ( ) && !renderMainHand.isEmpty && renderMainHand.getItem() != Items.AIR) {
            String stackName = renderMainHand.getDisplayName();
            int stackNameWidth = this.renderer.getStringWidth(stackName) / 2;
            GL11.glPushMatrix();
            GL11.glScalef(0.75f, 0.75f, 0.0f);
            this.renderer.drawStringWithShadow(stackName, -stackNameWidth, -(this.getBiggestArmorTag(player) + 20.0f), -1);
            GL11.glScalef(1.5f, 1.5f, 1.0f);
            GL11.glPopMatrix();
        }
        if ( this.armor.getValue ( ) ) {
            GlStateManager.pushMatrix();
            int xOffset = -8;
            for (ItemStack stack : player.inventory.armorInventory) {
                if (stack == null) continue;
                xOffset -= 8;
            }
            xOffset -= 8;
            ItemStack renderOffhand = player.getHeldItemOffhand().copy();
            if (renderOffhand.hasEffect() && (renderOffhand.getItem() instanceof ItemTool || renderOffhand.getItem() instanceof ItemArmor)) {
                renderOffhand.stackSize = 1;
            }
            this.renderItemStack(renderOffhand, xOffset, -26);
            xOffset += 16;
            for (ItemStack stack : player.inventory.armorInventory) {
                if (stack == null) continue;
                ItemStack armourStack = stack.copy();
                if (armourStack.hasEffect() && (armourStack.getItem() instanceof ItemTool || armourStack.getItem() instanceof ItemArmor)) {
                    armourStack.stackSize = 1;
                }
                this.renderItemStack(armourStack, xOffset, -26);
                xOffset += 16;
            }
            this.renderItemStack(renderMainHand, xOffset, -26);
            GlStateManager.popMatrix();
        }
        this.renderer.drawStringWithShadow(displayTag, -width, -(this.renderer.getFontHeight() - 1), this.getDisplayColour(player));
        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
    }

    private void renderItemStack(ItemStack stack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(256);
        RenderHelper.enableStandardItemLighting();
        Nametags.mc.getRenderItem().zLevel = -150.0f;
        GlStateManager.disableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.disableCull();
        Util.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        Util.mc.getRenderItem().renderItemOverlays(Nametags.mc.fontRenderer, stack, x, y);
        Nametags.mc.getRenderItem().zLevel = 0.0f;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        GlStateManager.disableDepth();
        this.renderEnchantmentText(stack, x, y);
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        GlStateManager.popMatrix();
    }

    private void renderEnchantmentText(ItemStack stack, int x, int y) {
        int enchantmentY = y - 8;
        if (stack.getItem() == Items.GOLDEN_APPLE && stack.hasEffect()) {
            this.renderer.drawStringWithShadow("god", x * 2, enchantmentY, -3977919);
            enchantmentY -= 8;
        }
        NBTTagList enchants = stack.getEnchantmentTagList();
        for (int index = 0; index < enchants.tagCount(); ++index) {
            short id = enchants.getCompoundTagAt(index).getShort("id");
            short level = enchants.getCompoundTagAt(index).getShort("lvl");
            Enchantment enc = Enchantment.getEnchantmentByID(id);
            if (enc == null) continue;
            String encName = enc.isCurse() ? TextFormatting.RED + enc.getTranslatedName(level).substring(11).substring(0, 1).toLowerCase() : enc.getTranslatedName(level).substring(0, 1).toLowerCase();
            encName = encName + level;
            this.renderer.drawStringWithShadow(encName, x * 2, enchantmentY, -1);
            enchantmentY -= 8;
        }
        if (DamageUtil.hasDurability(stack)) {
            int percent = DamageUtil.getRoundedDamage(stack);
            String color = percent >= 60 ? "\u00a7a" : (percent >= 25 ? "\u00a7e" : "\u00a7c");
            this.renderer.drawStringWithShadow(color + percent + "%", x * 2, enchantmentY, -1);
        }
    }

    private float getBiggestArmorTag(EntityPlayer player) {
        ItemStack renderOffHand;
        Enchantment enc;
        int index;
        float enchantmentY = 0.0f;
        boolean arm = false;
        for (ItemStack stack : player.inventory.armorInventory) {
            float encY = 0.0f;
            if (stack != null) {
                NBTTagList enchants = stack.getEnchantmentTagList();
                for (index = 0; index < enchants.tagCount(); ++index) {
                    short id = enchants.getCompoundTagAt(index).getShort("id");
                    enc = Enchantment.getEnchantmentByID(id);
                    if (enc == null) continue;
                    encY += 8.0f;
                    arm = true;
                }
            }
            if (!(encY > enchantmentY)) continue;
            enchantmentY = encY;
        }
        ItemStack renderMainHand = player.getHeldItemMainhand().copy();
        if (renderMainHand.hasEffect()) {
            float encY = 0.0f;
            NBTTagList enchants = renderMainHand.getEnchantmentTagList();
            for (int index2 = 0; index2 < enchants.tagCount(); ++index2) {
                short id = enchants.getCompoundTagAt(index2).getShort("id");
                Enchantment enc2 = Enchantment.getEnchantmentByID(id);
                if (enc2 == null) continue;
                encY += 8.0f;
                arm = true;
            }
            if (encY > enchantmentY) {
                enchantmentY = encY;
            }
        }
        if ((renderOffHand = player.getHeldItemOffhand().copy()).hasEffect()) {
            float encY = 0.0f;
            NBTTagList enchants = renderOffHand.getEnchantmentTagList();
            for (index = 0; index < enchants.tagCount(); ++index) {
                short id = enchants.getCompoundTagAt(index).getShort("id");
                enc = Enchantment.getEnchantmentByID(id);
                if (enc == null) continue;
                encY += 8.0f;
                arm = true;
            }
            if (encY > enchantmentY) {
                enchantmentY = encY;
            }
        }
        return (float) (arm ? 0 : 20) + enchantmentY;
    }

    private String getDisplayTag(EntityPlayer player) {
        String name = player.getDisplayName().getFormattedText();
        if (name.contains(Util.mc.getSession().getUsername())) {
            name = "You";
        }
        if (! this.health.getValue ( ) ) {
            return name;
        }
        float health = EntityUtil.getHealth(player);
        String color = health > 18.0f ? "\u00a7a" : (health > 16.0f ? "\u00a72" : (health > 12.0f ? "\u00a7e" : (health > 8.0f ? "\u00a76" : (health > 5.0f ? "\u00a7c" : "\u00a74"))));
        String pingStr = "";
        if ( this.ping.getValue ( ) ) {
            try {
                int responseTime = Objects.requireNonNull(Util.mc.getConnection()).getPlayerInfo(player.getUniqueID()).getResponseTime();
                pingStr = pingStr + responseTime + "ms ";
            } catch (Exception responseTime) {
                // empty catch block
            }
        }
        String popStr = " ";
        if ( this.totemPops.getValue ( ) ) {
            popStr = popStr + RenoSense.totemPopManager.getTotemPopString(player);
        }
        String idString = "";
        if ( this.entityID.getValue ( ) ) {
            idString = idString + "ID: " + player.getEntityId() + " ";
        }
        String gameModeStr = "";
        if ( this.gamemode.getValue ( ) ) {
            gameModeStr = player.isCreative() ? gameModeStr + "[C] " : (player.isSpectator() || player.isInvisible() ? gameModeStr + "[I] " : gameModeStr + "[S] ");
        }
        name = Math.floor(health) == (double) health ? name + color + " " + (health > 0.0f ? Integer.valueOf((int) Math.floor(health)) : "dead") : name + color + " " + (health > 0.0f ? Integer.valueOf((int) health) : "dead");
        return pingStr + idString + gameModeStr + name + popStr;
    }

    private int getDisplayColour(EntityPlayer player) {
        int colour = -5592406;
        if ( this.whiter.getValue ( ) ) {
            colour = -1;
        }
        if (RenoSense.friendManager.isFriend(player)) {
            return -11157267;
        }
        if (player.isInvisible()) {
            colour = -1113785;
        } else if (player.isSneaking() && this.sneak.getValue ( ) ) {
            colour = -6481515;
        }
        return colour;
    }

    private double interpolate(double previous, double current, float delta) {
        return previous + (current - previous) * (double) delta;
    }
}

