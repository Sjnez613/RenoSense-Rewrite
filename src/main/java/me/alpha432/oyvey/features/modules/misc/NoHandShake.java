package me.alpha432.oyvey.features.modules.misc;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.network.PacketBuffer;
import io.netty.buffer.Unpooled;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;

public class NoHandShake extends Module
{
    public NoHandShake() {
        super("NoHandshake", "Doesnt send your modlist to the server.", Category.MISC, true, false, false);
    }

    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        if (event.getPacket() instanceof FMLProxyPacket && !NoHandShake.mc.isSingleplayer()) {
            event.setCanceled(true);
        }
        final CPacketCustomPayload packet;
        if (event.getPacket() instanceof CPacketCustomPayload && (packet = event.getPacket()).getChannelName().equals("MC|Brand")) {
            packet.data = new PacketBuffer(Unpooled.buffer()).writeString("vanilla");
        }
    }
}