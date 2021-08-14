package me.sjnez.renosense.features.modules.movement;

import me.sjnez.renosense.event.events.PacketEvent;
import me.sjnez.renosense.event.events.PushEvent;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity
        extends Module {
    private static Velocity INSTANCE = new Velocity();
    public Setting<Boolean> knockBack = this.register( new Setting <> ( "KnockBack" , true ));
    public Setting<Boolean> noPush = this.register( new Setting <> ( "NoPush" , true ));
    public Setting<Float> horizontal = this.register( new Setting <> ( "Horizontal" , 0.0f , 0.0f , 100.0f ));
    public Setting<Float> vertical = this.register( new Setting <> ( "Vertical" , 0.0f , 0.0f , 100.0f ));
    public Setting<Boolean> explosions = this.register( new Setting <> ( "Explosions" , true ));
    public Setting<Boolean> bobbers = this.register( new Setting <> ( "Bobbers" , true ));
    public Setting<Boolean> water = this.register( new Setting <> ( "Water" , false ));
    public Setting<Boolean> blocks = this.register( new Setting <> ( "Blocks" , false ));
    public Setting<Boolean> ice = this.register( new Setting <> ( "Ice" , false ));

    public Velocity() {
        super("Velocity", "Allows you to control your velocity", Module.Category.MOVEMENT, true, false, false);
        this.setInstance();
    }

    public static Velocity getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Velocity();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if ( this.ice.getValue ( ) ) {
            Blocks.ICE.slipperiness = 0.6f;
            Blocks.PACKED_ICE.slipperiness = 0.6f;
            Blocks.FROSTED_ICE.slipperiness = 0.6f;
        }
    }

    @Override
    public void onDisable() {
            Blocks.ICE.slipperiness = 0.98f;
            Blocks.PACKED_ICE.slipperiness = 0.98f;
            Blocks.FROSTED_ICE.slipperiness = 0.98f;
    }

    @SubscribeEvent
    public void onPacketReceived(PacketEvent.Receive event) {
        if (event.getStage() == 0 && Velocity.mc.player != null) {
            Entity entity;
            SPacketEntityStatus packet;
            SPacketEntityVelocity velocity;
            if ( this.knockBack.getValue ( ) && event.getPacket() instanceof SPacketEntityVelocity && (velocity = event.getPacket()).getEntityID() == Velocity.mc.player.entityId) {
                if ( this.horizontal.getValue ( ) == 0.0f && this.vertical.getValue ( ) == 0.0f) {
                    event.setCanceled(true);
                    return;
                }
                velocity.motionX = (int) ((float) velocity.motionX * this.horizontal.getValue ( ) );
                velocity.motionY = (int) ((float) velocity.motionY * this.vertical.getValue ( ) );
                velocity.motionZ = (int) ((float) velocity.motionZ * this.horizontal.getValue ( ) );
            }
            if (event.getPacket() instanceof SPacketEntityStatus && this.bobbers.getValue ( ) && (packet = event.getPacket()).getOpCode() == 31 && (entity = packet.getEntity(Velocity.mc.world)) instanceof EntityFishHook) {
                EntityFishHook fishHook = (EntityFishHook) entity;
                if (fishHook.caughtEntity == Velocity.mc.player) {
                    event.setCanceled(true);
                }
            }
            if ( this.explosions.getValue ( ) && event.getPacket() instanceof SPacketExplosion) {
                //velocity = (SPacketExplosion)event.getPacket();
                SPacketExplosion velocity_ = event.getPacket();
                velocity_.motionX *= this.horizontal.getValue ( );
                velocity_.motionY *= this.vertical.getValue ( );
                velocity_.motionZ *= this.horizontal.getValue ( );
            }
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        if (event.getStage() == 0 && this.noPush.getValue ( ) && event.entity.equals(Velocity.mc.player)) {
            if ( this.horizontal.getValue ( ) == 0.0f && this.vertical.getValue ( ) == 0.0f) {
                event.setCanceled(true);
                return;
            }
            event.x = -event.x * (double) this.horizontal.getValue ( );
            event.y = -event.y * (double) this.vertical.getValue ( );
            event.z = -event.z * (double) this.horizontal.getValue ( );
        } else if (event.getStage() == 1 && this.blocks.getValue ( ) ) {
            event.setCanceled(true);
        } else if (event.getStage() == 2 && this.water.getValue ( ) && Velocity.mc.player != null && Velocity.mc.player.equals(event.entity)) {
            event.setCanceled(true);
        }
    }
}

