package me.alpha432.oyvey.features.modules.player;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;

public class MiddleClick extends Module {
    private boolean clicked = false;
    private boolean clickedbutton = false;

    private final Setting<Boolean> friend = this.register(new Setting("Friend", false));

    private final Setting<Boolean> pearl = this.register(new Setting("Pearl", false));
    private final Setting<Mode> mode = this.register(new Setting<Mode>("Mode", Mode.MiddleClick, v -> this.pearl.getValue()));



    public enum Mode {
        Toggle,
        MiddleClick
    }

    public MiddleClick() {
        super("MiddleClick", "Stuff for middle clicking", Category.PLAYER, true, false, false);
    }

    @Override
    public void onEnable() {
        if(pearl.getValue()) {
            if (!fullNullCheck() && this.mode.getValue() == Mode.Toggle) {
                this.throwPearl();
                this.disable();
            }
        }
    }

    @Override
    public void onUpdate() {
        if(friend.getValue()) {
            if (Mouse.isButtonDown(2)) {
                if (!this.clicked && mc.currentScreen == null) {
                    this.onClick();
                }
                this.clicked = true;
            } else {
                this.clicked = false;
            }
        }
    }


        @Override
    public void onTick() {
        if(pearl.getValue()){

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
    }

        private void onClick() {
            if (friend.getValue()) {

                Entity entity;
                RayTraceResult result = mc.objectMouseOver;
                if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY && (entity = result.entityHit) instanceof EntityPlayer) {
                    if (OyVey.friendManager.isFriend(entity.getName())) {
                        OyVey.friendManager.removeFriend(entity.getName());
                        Command.sendMessage(ChatFormatting.RED + entity.getName() + ChatFormatting.RED + " has been unfriended.");
                    } else {
                        OyVey.friendManager.addFriend(entity.getName());
                        Command.sendMessage(ChatFormatting.AQUA + entity.getName() + ChatFormatting.AQUA + " has been friended.");
                    }
                }
                this.clicked = true;
            }
        }


    private void throwPearl() {
        if (pearl.getValue()) {
            boolean offhand;
            Entity entity;
            RayTraceResult result;
            int pearlSlot = InventoryUtil.findHotbarBlock(ItemEnderPearl.class);
            if ((result = mc.objectMouseOver) != null && result.typeOfHit == RayTraceResult.Type.ENTITY && (entity = result.entityHit) instanceof EntityPlayer) {
                return;
            }
            boolean bl = offhand = mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
            if (pearlSlot != -1 || offhand) {
                int oldslot = mc.player.inventory.currentItem;
                if (!offhand) {
                    InventoryUtil.switchToHotbarSlot(pearlSlot, false);
                }
                mc.playerController.processRightClick(mc.player, mc.world, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                if (!offhand) {
                    InventoryUtil.switchToHotbarSlot(oldslot, false);
                }
            }
        }
    }
}