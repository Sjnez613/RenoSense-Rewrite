package me.sjnez.renosense.features.modules.misc;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.event.events.PacketEvent;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.MathUtil;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoLog
        extends Module {
    private static AutoLog INSTANCE = new AutoLog();
    private final Setting<Float> health = this.register( new Setting <> ( "Health" , 16.0f , 0.1f , 36.0f ));
    private final Setting<Boolean> bed = this.register( new Setting <> ( "Beds" , true ));
    private final Setting<Float> range = this.register(new Setting<Object>("BedRange", 6.0f , 0.1f , 36.0f , v -> this.bed.getValue()));
    private final Setting<Boolean> logout = this.register( new Setting <> ( "LogoutOff" , true ));

    public AutoLog() {
        super("AutoLog", "Logs when in danger.", Module.Category.MISC, false, false, false);
        this.setInstance();
    }

    public static AutoLog getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AutoLog();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (!AutoLog.nullCheck() && AutoLog.mc.player.getHealth() <= this.health.getValue ( ) ) {
            RenoSense.moduleManager.disableModule("AutoReconnect");
            AutoLog.mc.player.connection.sendPacket(new SPacketDisconnect(new TextComponentString("AutoLogged")));
            if ( this.logout.getValue ( ) ) {
                this.disable();
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        SPacketBlockChange packet;
        if (event.getPacket() instanceof SPacketBlockChange && this.bed.getValue ( ) && (packet = event.getPacket()).getBlockState().getBlock() == Blocks.BED && AutoLog.mc.player.getDistanceSqToCenter(packet.getBlockPosition()) <= MathUtil.square( this.range.getValue ( ) )) {
            RenoSense.moduleManager.disableModule("AutoReconnect");
            AutoLog.mc.player.connection.sendPacket(new SPacketDisconnect(new TextComponentString("AutoLogged")));
            if ( this.logout.getValue ( ) ) {
                this.disable();
            }
        }
    }
}

