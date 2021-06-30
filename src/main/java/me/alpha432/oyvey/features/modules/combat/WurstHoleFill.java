package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BlockUtil;
import me.alpha432.oyvey.util.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class WurstHoleFill extends Module {

    public WurstHoleFill() {
        super("WurstHoleFill", "fills holes", Module.Category.COMBAT, true, false, false);
    }

    public Setting<Boolean> hole_toggle = this.register(new Setting<Boolean>("Toggle", false));
    public Setting<Boolean> hole_rotate = this.register(new Setting<Boolean>("Rotate", false));
    public Setting<Integer> hole_range = this.register(new Setting<Integer>("Range", 4, 1, 6));


    private final ArrayList<BlockPos> holes = new ArrayList<>();

    @Override
    public void onEnable() {
        if (find_in_hotbar() == -1) {
            this.toggle();
        }
        find_new_holes();
    }

    @Override
    public void onDisable() {
        holes.clear();
    }

    @Override
    public void onUpdate() {

        if (find_in_hotbar() == -1) {
            this.disable();
            return;
        }

        BlockPos posToFill = null;
        if (holes.isEmpty() && hole_toggle.getValue()) {
            this.disable();
            return;
        } else {
            find_new_holes();
        }

        BlockPos pos_to_fill = null;

        for (BlockPos pos : new ArrayList<>(holes)) {

            if (pos == null) continue;

            BlockUtil.ValidResult result = BlockUtil.valid(pos);

            if (result != BlockUtil.ValidResult.Ok) {
                holes.remove(pos);
                continue;
            }
            pos_to_fill = pos;
            break;
        }

        if (find_in_hotbar() == -1) {
            this.disable();
            return;
        }

        if (pos_to_fill != null) {
            int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
            int echestSlot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
            if (obbySlot == -1) { InventoryUtil.mc.player.connection.sendPacket(new CPacketHeldItemChange(echestSlot));
                InventoryUtil.mc.playerController.updateController(); }
            if (echestSlot == -1) { InventoryUtil.mc.player.connection.sendPacket(new CPacketHeldItemChange(obbySlot));
                InventoryUtil.mc.playerController.updateController(); } else if (echestSlot != -1 && obbySlot != -1) {
                InventoryUtil.mc.player.connection.sendPacket(new CPacketHeldItemChange(obbySlot));
                InventoryUtil.mc.playerController.updateController();
            }
            if (BlockUtil.placeBlock(pos_to_fill, EnumHand.MAIN_HAND, hole_rotate.getValue(), false, false)) {
                holes.remove(pos_to_fill);
            }
            int originalSlot = HoleFiller.mc.player.inventory.currentItem;
            InventoryUtil.mc.player.connection.sendPacket(new CPacketHeldItemChange(originalSlot));
            InventoryUtil.mc.player.inventory.currentItem = originalSlot;
            InventoryUtil.mc.playerController.updateController();
        }
    }




    public void find_new_holes() {

        holes.clear();

        for (BlockPos pos : BlockUtil.getSphere(BlockUtil.GetLocalPlayerPosFloored(), hole_range.getValue(), (int) hole_range.getValue(), false, true, 0)) {

            if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            if (!mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            if (!mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            boolean possible = true;

            for (BlockPos seems_blocks : new BlockPos[] {
                    new BlockPos( 0, -1,  0),
                    new BlockPos( 0,  0, -1),
                    new BlockPos( 1,  0,  0),
                    new BlockPos( 0,  0,  1),
                    new BlockPos(-1,  0,  0)
            }) {
                Block block = mc.world.getBlockState(pos.add(seems_blocks)).getBlock();

                if (block != Blocks.BEDROCK && block != Blocks.OBSIDIAN && block != Blocks.ENDER_CHEST && block != Blocks.ANVIL) {
                    possible = false;
                    break;
                }
            }

            if (possible) {
                holes.add(pos);
            }
        }
    }

    private int find_in_hotbar() {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                final Block block = ((ItemBlock) stack.getItem()).getBlock();

                if (block instanceof BlockEnderChest) {
                    return i;
                }

                if (block instanceof BlockObsidian) {
                    return i;
                }
            }
        }
        return -1;
    }

}