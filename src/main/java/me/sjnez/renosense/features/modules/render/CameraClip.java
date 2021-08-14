package me.sjnez.renosense.features.modules.render;

import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;

public class CameraClip
        extends Module {
    private static CameraClip INSTANCE = new CameraClip();
    public Setting<Boolean> extend = this.register( new Setting <> ( "Extend" , false ));
    public Setting<Double> distance = this.register(new Setting<Object>("Distance", 10.0, 0.0, 50.0, v -> this.extend.getValue(), "By how much you want to extend the distance."));

    public CameraClip() {
        super("CameraClip", "Makes your Camera clip.", Module.Category.RENDER, false, false, false);
        this.setInstance();
    }

    public static CameraClip getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CameraClip();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }
}

