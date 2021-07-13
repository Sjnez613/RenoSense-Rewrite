package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Mouse;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;

public class MCP extends Module {

    private boolean clickedbutton = false;

    private final Setting<Mode> mode = this.register(new Setting<Mode>("Mode", Mode.MiddleClick));

    public enum Mode {
        Toggle,
        MiddleClick
    }

    public MCP() {
        super("MCP", "Throws a pearl", Module.Category.PLAYER, false, false, false);
    }

    @Override
    public void onEnable() {
        if (!MCP.fullNullCheck() && this.mode.getValue() == Mode.Toggle) {
            this.throwPearl();
            this.disable();
        }
    }

    @Override
    public void onTick() {
        if (this.mode.getValue() == Mode.MiddleClick) {
            if (Mouse.isButtonDown(2)) {
                if (!this.clickedbutton) {
                    this.throwPearl();
                }
                this.clickedbutton = true;
            } else {
                this.clickedbutton = false;
            }
        }
    }

    private void throwPearl() {
        boolean offhand;
        Entity entity;
        RayTraceResult result;
        int pearlSlot = InventoryUtil.findHotbarBlock(ItemEnderPearl.class);
        if ((result = MCP.mc.objectMouseOver) != null && result.typeOfHit == RayTraceResult.Type.ENTITY && (entity = result.entityHit) instanceof EntityPlayer) {
            return;
        }
        boolean bl = offhand = MCP.mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
        if (pearlSlot != -1 || offhand) {
            int oldslot = MCP.mc.player.inventory.currentItem;
            if (!offhand) {
                InventoryUtil.switchToHotbarSlot(pearlSlot, false);
            }
            MCP.mc.playerController.processRightClick(MCP.mc.player, MCP.mc.world, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            if (!offhand) {
                InventoryUtil.switchToHotbarSlot(oldslot, false);
            }
        }
    }
}

