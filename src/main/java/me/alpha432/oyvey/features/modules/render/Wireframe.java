package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Wireframe
        extends Module {
    private static Wireframe INSTANCE = new Wireframe();
    public final Setting<Float> alpha = this.register(new Setting<Float>("PAlpha", Float.valueOf(255.0f), Float.valueOf(0.1f), Float.valueOf(255.0f)));
    public final Setting<Float> cAlpha = this.register(new Setting<Float>("CAlpha", Float.valueOf(255.0f), Float.valueOf(0.1f), Float.valueOf(255.0f)));
    public final Setting<Float> lineWidth = this.register(new Setting<Float>("PLineWidth", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(3.0f)));
    public final Setting<Float> crystalLineWidth = this.register(new Setting<Float>("CLineWidth", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(3.0f)));
    public Setting<RenderMode> mode = this.register(new Setting<RenderMode>("PMode", RenderMode.SOLID));
    public Setting<RenderMode> cMode = this.register(new Setting<RenderMode>("CMode", RenderMode.SOLID));
    public Setting<Boolean> players = this.register(new Setting<Boolean>("Players", Boolean.FALSE));
    public Setting<Boolean> playerModel = this.register(new Setting<Boolean>("PlayerModel", Boolean.FALSE));
    public Setting<Boolean> crystals = this.register(new Setting<Boolean>("Crystals", Boolean.FALSE));
    public Setting<Boolean> crystalModel = this.register(new Setting<Boolean>("CrystalModel", Boolean.FALSE));

    public Wireframe() {
        super("Wireframe", "Draws a wireframe esp around other players.", Module.Category.RENDER, false, false, false);
        this.setInstance();
    }

    public static Wireframe getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Wireframe();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onRenderPlayerEvent(RenderPlayerEvent.Pre event) {
        event.getEntityPlayer().hurtTime = 0;
    }

    public enum RenderMode {
        SOLID,
        WIREFRAME

    }
}

