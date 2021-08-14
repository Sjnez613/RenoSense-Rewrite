package me.sjnez.renosense.mixin.mixins.accessors;

import net.minecraft.client.multiplayer.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ServerAddress.class})
public interface IServerAddress {
    @Invoker(value="getServerAddress")
    static String[] getServerAddress ( String string ) {
        throw new IllegalStateException("Mixin didnt transform this");
    }
}

