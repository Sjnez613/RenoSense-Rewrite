package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoGlitchBlocks extends Module {

    private static NoGlitchBlocks INSTANCE = new NoGlitchBlocks();

    public NoGlitchBlocks() {
        super("NoGlitchBlocks", "deletes blocks", Module.Category.PLAYER, true, false, false);
        this.setInstance();
    }

    public static NoGlitchBlocks getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new NoGlitchBlocks();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (NoGlitchBlocks.fullNullCheck()) {
            return;
        }
        if (!(NoGlitchBlocks.mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) {
            BlockPos pos = NoGlitchBlocks.mc.player.getPosition();
            this.removeGlitchBlocks(pos);
        }
    }

    private void removeGlitchBlocks(BlockPos pos) {
        for (int dx = -4; dx <= 4; ++dx) {
            for (int dy = -4; dy <= 4; ++dy) {
                for (int dz = -4; dz <= 4; ++dz) {
                    BlockPos blockPos = new BlockPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (!NoGlitchBlocks.mc.world.getBlockState(blockPos).getBlock().equals(Blocks.AIR)) continue;
                    NoGlitchBlocks.mc.playerController.processRightClickBlock(NoGlitchBlocks.mc.player, NoGlitchBlocks.mc.world, blockPos, EnumFacing.DOWN, new Vec3d(0.5, 0.5, 0.5), EnumHand.MAIN_HAND);
                }
            }
        }
    }
}

