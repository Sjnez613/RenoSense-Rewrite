package me.sjnez.renosense.mixin.mixins;

import me.sjnez.renosense.event.events.JesusEvent;
import me.sjnez.renosense.features.modules.player.LiquidInteract;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockLiquid.class})
public class MixinBlockLiquid
extends Block {
    protected MixinBlockLiquid(Material materialIn) {
        super(materialIn);
    }

    @Inject(method={"getCollisionBoundingBox"}, at={@At(value="HEAD")}, cancellable=true)
    public void getCollisionBoundingBoxHook(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<AxisAlignedBB> info) {
        JesusEvent event = new JesusEvent(0, pos);
        MinecraftForge.EVENT_BUS.post( event );
        if (event.isCanceled()) {
            info.setReturnValue(event.getBoundingBox());
        }
    }

    @Inject(method={"canCollideCheck"}, at={@At(value="HEAD")}, cancellable=true)
    public void canCollideCheckHook(IBlockState blockState, boolean hitIfLiquid, CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(hitIfLiquid && (Integer)blockState.getValue((IProperty)BlockLiquid.LEVEL) == 0 || LiquidInteract.getInstance().isOn());
    }
}

