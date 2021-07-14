package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.storage.MapData;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class PlayerFinder extends Module {

    public Setting<Integer> amountPerTick;

    public PlayerFinder() {
        super("PlayerFinder", "COORD EXPLOIT", Category.MISC, true, false, false);

        this.amountPerTick = (Setting<Integer>) this.register(new Setting("PacketsPerTick", 2, 0, 5));
    }

    public void onUpdate() {
        if (mc.player.inPortal && mc.player.getRidingEntity() instanceof EntityBoat) {
            if (mc.player.inventory.getCurrentItem().getItem().equals(Items.MAP))
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(mc.player.getPosition(), EnumFacing.UP, EnumHand.MAIN_HAND, 0, -1337.77f, 0));
            for (int i = 0; i < amountPerTick.getValue(); i++) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, -1337.77D, mc.player.posZ, false));
                mc.player.connection.sendPacket(new CPacketSteerBoat(false, true));


            }
        }
        for (Entity entity : mc.world.playerEntities) {
            if (!entity.getName().equalsIgnoreCase(mc.player.getName())) {
                Command.sendMessage("Found A Player Kek " + entity.getPosition());
            }
        }
    }
    @Listener
    public void onUpdate(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketMaps) {
            ((SPacketMaps) event.getPacket()).setMapdataTo(new MapData("haha i get ur coords"));
        }
        if (event.getPacket() instanceof SPacketEntityVelocity || event.getPacket() instanceof SPacketEntityTeleport) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void onUpdate(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketConfirmTeleport || event.getPacket() instanceof CPacketPlayerTryUseItem) {
            event.setCanceled(true);
        }
    }
}