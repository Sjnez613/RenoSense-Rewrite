package me.alpha432.oyvey.features.modules.player;

import com.mojang.authlib.GameProfile;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.entity.EntityOtherPlayerMP;

import java.util.UUID;

public class FakePlayer
        extends Module {
    private final String name = "Scott";
    private EntityOtherPlayerMP _fakePlayer;

    public FakePlayer() {
        super("FakePlayer", "Spawns a FakePlayer for testing", Module.Category.PLAYER, false, false, false);
    }

    @Override
    public void onEnable() {
        if (FakePlayer.fullNullCheck()) {
            this.disable();
            return;
        }
        this._fakePlayer = null;
        if (FakePlayer.mc.player != null) {
            this._fakePlayer = new EntityOtherPlayerMP(FakePlayer.mc.world, new GameProfile(UUID.randomUUID(), this.name));
            Command.sendMessage(String.format("%s has been spawned.", this.name));
            this._fakePlayer.copyLocationAndAnglesFrom(FakePlayer.mc.player);
            this._fakePlayer.rotationYawHead = FakePlayer.mc.player.rotationYawHead;
            FakePlayer.mc.world.addEntityToWorld(-100, this._fakePlayer);
        }
    }

    @Override
    public void onDisable() {
        if (_fakePlayer != null) {
            mc.world.removeEntity(_fakePlayer);
            _fakePlayer = null;
        }

    }
}