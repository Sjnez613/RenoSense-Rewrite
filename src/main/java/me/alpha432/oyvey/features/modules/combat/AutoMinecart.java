package me.alpha432.oyvey.features.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BlockUtil;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.InventoryUtil;
import me.alpha432.oyvey.util.MathUtil;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoMinecart
        extends Module {
    private final Setting<Boolean> web = this.register(new Setting<Boolean>("Web", Boolean.FALSE));
    private final Setting<Boolean> rotate = this.register(new Setting<Boolean>("Rotate", Boolean.FALSE));
    private final Setting<Boolean> packet = this.register(new Setting<Boolean>("PacketPlace", Boolean.FALSE));
    private final Setting<Integer> blocksPerTick = this.register(new Setting<Integer>("BlocksPerTick", 1, 1, 4));
    private final Setting<Integer> delay = this.register(new Setting<Integer>("Carts", 20, 0, 50));
    public Setting<Float> minHP = this.register(new Setting<Float>("MinHP", Float.valueOf(4.0f), Float.valueOf(0.0f), Float.valueOf(36.0f)));
    int wait;
    int waitFlint;
    int originalSlot;
    private boolean check;

    public AutoMinecart() {
        super("AutoMinecart", "Places and explodes minecarts on other players.", Module.Category.COMBAT, true, false, false);
    }

    @Override
    public void onEnable() {
        if (AutoMinecart.fullNullCheck()) {
            this.toggle();
        }
        this.wait = 0;
        this.waitFlint = 0;
        this.originalSlot = AutoMinecart.mc.player.inventory.currentItem;
        this.check = true;
    }

    @Override
    public void onUpdate() {
        EntityPlayer target;
        if (AutoMinecart.fullNullCheck()) {
            this.toggle();
        }
        int i = InventoryUtil.findStackInventory(Items.TNT_MINECART);
        for (int j = 0; j < 9; ++j) {
            if (AutoMinecart.mc.player.inventory.getStackInSlot(j).getItem() != Items.AIR || i == -1) continue;
            AutoMinecart.mc.playerController.windowClick(AutoMinecart.mc.player.inventoryContainer.windowId, i, 0, ClickType.QUICK_MOVE, AutoMinecart.mc.player);
            AutoMinecart.mc.playerController.updateController();
        }
        int webSlot = InventoryUtil.findHotbarBlock(BlockWeb.class);
        int tntSlot = InventoryUtil.getItemHotbar(Items.TNT_MINECART);
        int flintSlot = InventoryUtil.getItemHotbar(Items.FLINT_AND_STEEL);
        int railSlot = InventoryUtil.findHotbarBlock(Blocks.ACTIVATOR_RAIL);
        int picSlot = InventoryUtil.getItemHotbar(Items.DIAMOND_PICKAXE);
        if (tntSlot == -1 || railSlot == -1 || flintSlot == -1 || picSlot == -1 || this.web.getValue().booleanValue() && webSlot == -1) {
            Command.sendMessage("<" + this.getDisplayName() + "> " + ChatFormatting.RED + "No (tnt minecart/activator rail/flint/pic/webs) in hotbar disabling...");
            this.toggle();
        }
        if ((target = this.getTarget()) == null) {
            return;
        }
        BlockPos pos = new BlockPos(target.posX, target.posY, target.posZ);
        Vec3d hitVec = new Vec3d(pos).add(0.0, -0.5, 0.0);
        if (AutoMinecart.mc.player.getDistanceSq(pos) <= MathUtil.square(6.0)) {
            this.check = true;
            if (AutoMinecart.mc.world.getBlockState(pos).getBlock() != Blocks.ACTIVATOR_RAIL && !AutoMinecart.mc.world.getEntitiesWithinAABB(EntityMinecartTNT.class, new AxisAlignedBB(pos)).isEmpty()) {
                InventoryUtil.switchToHotbarSlot(flintSlot, false);
                BlockUtil.rightClickBlock(pos.down(), hitVec, EnumHand.MAIN_HAND, EnumFacing.UP, this.packet.getValue());
            }
            if (AutoMinecart.mc.world.getBlockState(pos).getBlock() != Blocks.ACTIVATOR_RAIL && AutoMinecart.mc.world.getEntitiesWithinAABB(EntityMinecartTNT.class, new AxisAlignedBB(pos)).isEmpty() && AutoMinecart.mc.world.getEntitiesWithinAABB(EntityMinecartTNT.class, new AxisAlignedBB(pos.up())).isEmpty() && AutoMinecart.mc.world.getEntitiesWithinAABB(EntityMinecartTNT.class, new AxisAlignedBB(pos.down())).isEmpty()) {
                InventoryUtil.switchToHotbarSlot(railSlot, false);
                BlockUtil.rightClickBlock(pos.down(), hitVec, EnumHand.MAIN_HAND, EnumFacing.UP, this.packet.getValue());
                this.wait = 0;
            }
            if (this.web.getValue().booleanValue() && this.wait != 0 && AutoMinecart.mc.world.getBlockState(pos).getBlock() == Blocks.ACTIVATOR_RAIL && !target.isInWeb && (BlockUtil.isPositionPlaceable(pos.up(), false) == 1 || BlockUtil.isPositionPlaceable(pos.up(), false) == 3) && AutoMinecart.mc.world.getEntitiesWithinAABB(EntityMinecartTNT.class, new AxisAlignedBB(pos.up())).isEmpty()) {
                InventoryUtil.switchToHotbarSlot(webSlot, false);
                BlockUtil.placeBlock(pos.up(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false);
            }
            if (AutoMinecart.mc.world.getBlockState(pos).getBlock() == Blocks.ACTIVATOR_RAIL) {
                InventoryUtil.switchToHotbarSlot(tntSlot, false);
                for (int u = 0; u < this.blocksPerTick.getValue(); ++u) {
                    BlockUtil.rightClickBlock(pos, hitVec, EnumHand.MAIN_HAND, EnumFacing.UP, this.packet.getValue());
                }
            }
            if (this.wait < this.delay.getValue()) {
                ++this.wait;
                return;
            }
            this.check = false;
            this.wait = 0;
            InventoryUtil.switchToHotbarSlot(picSlot, false);
            if (AutoMinecart.mc.world.getBlockState(pos).getBlock() == Blocks.ACTIVATOR_RAIL && !AutoMinecart.mc.world.getEntitiesWithinAABB(EntityMinecartTNT.class, new AxisAlignedBB(pos)).isEmpty()) {
                AutoMinecart.mc.playerController.onPlayerDamageBlock(pos, EnumFacing.UP);
            }
            InventoryUtil.switchToHotbarSlot(flintSlot, false);
            if (AutoMinecart.mc.world.getBlockState(pos).getBlock().getBlockState().getBaseState().getMaterial() != Material.FIRE && !AutoMinecart.mc.world.getEntitiesWithinAABB(EntityMinecartTNT.class, new AxisAlignedBB(pos)).isEmpty()) {
                BlockUtil.rightClickBlock(pos.down(), hitVec, EnumHand.MAIN_HAND, EnumFacing.UP, this.packet.getValue());
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        if (this.check) {
            return ChatFormatting.GREEN + "Placing";
        }
        return ChatFormatting.RED + "Breaking";
    }

    @Override
    public void onDisable() {
        AutoMinecart.mc.player.inventory.currentItem = this.originalSlot;
    }

    private EntityPlayer getTarget() {
        EntityPlayer target = null;
        double distance = Math.pow(6.0, 2.0) + 1.0;
        for (EntityPlayer player : AutoMinecart.mc.world.playerEntities) {
            if (EntityUtil.isntValid(player, 6.0) || player.isInWater() || player.isInLava() || !EntityUtil.isTrapped(player, false, false, false, false, false) || player.getHealth() + player.getAbsorptionAmount() > this.minHP.getValue().floatValue())
                continue;
            if (target == null) {
                target = player;
                distance = AutoMinecart.mc.player.getDistanceSq(player);
                continue;
            }
            if (!(AutoMinecart.mc.player.getDistanceSq(player) < distance)) continue;
            target = player;
            distance = AutoMinecart.mc.player.getDistanceSq(player);
        }
        return target;
    }
}

