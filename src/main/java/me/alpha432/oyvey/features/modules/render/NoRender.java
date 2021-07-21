package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoRender
        extends Module {
    private static NoRender INSTANCE = new NoRender();

    static {
        NoRender.INSTANCE = new NoRender();
    }
    public Setting<Boolean> blocks = register(new Setting("Blocks", Boolean.valueOf(false), "Blocks"));
    public Setting<NoArmor> noArmor = this.register(new Setting<NoArmor>("NoArmor", NoArmor.NONE, "Doesnt Render Armor on players."));
    public Setting<Skylight> skylight = this.register(new Setting<Skylight>("Skylight", Skylight.NONE));
    public Setting<Boolean> advancements = this.register(new Setting<Boolean>("Advancements", false));
    public Setting<Boolean> hurtCam = this.register(new Setting<Boolean>("NoHurtCam", false));
    public Setting<Boolean> fire = this.register(new Setting<Boolean>("Fire", Boolean.valueOf(false), "Removes the portal overlay."));
    public Setting<Boolean> explosion = this.register(new Setting<Boolean>("Explosions", false, "Removes explosions"));

    public NoRender() {
        super("NoRender", "Allows you to stop rendering stuff", Module.Category.RENDER, true, false, false);
        this.setInstance();
    }

    public static NoRender getInstance() {
        if (NoRender.INSTANCE == null) {
            NoRender.INSTANCE = new NoRender();
        }
        return NoRender.INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacketReceive(final PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketExplosion && this.explosion.getValue().booleanValue()) {
            event.setCanceled(true);
        }
    }

public enum Skylight {
    NONE,
    WORLD,
    ENTITY,
    ALL
}

public enum NoArmor {
    NONE,
    ALL,
    HELMET
}

}