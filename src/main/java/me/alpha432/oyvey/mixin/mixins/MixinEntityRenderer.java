package me.alpha432.oyvey.mixin.mixins;

import com.google.common.base.Predicate;
import me.alpha432.oyvey.features.modules.misc.NoHitBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = {EntityRenderer.class})
public class MixinEntityRenderer {
    @Redirect(method = {"getMouseOver"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate predicate) {
        if (NoHitBox.getINSTANCE().isOn() && (Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ItemPickaxe && NoHitBox.getINSTANCE().pickaxe.getValue() != false || Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && NoHitBox.getINSTANCE().crystal.getValue() != false || Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() == Items.GOLDEN_APPLE && NoHitBox.getINSTANCE().gapple.getValue() != false || Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() == Items.FLINT_AND_STEEL || Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() == Items.TNT_MINECART)) {
            return new ArrayList<Entity>();
        }
        return worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }
}

