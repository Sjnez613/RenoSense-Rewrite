package me.sjnez.renosense.mixin.mixins.accessors;

import net.minecraft.network.play.server.SPacketSetSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={SPacketSetSlot.class})
public interface ISPacketSetSlot {
    @Accessor(value="windowId")
    int getId ( );

    @Accessor(value="windowId")
    void setWindowId ( int var1 );
}

