package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.RenderItemEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ViewModel extends Module {

    public Setting<Double> mainX = this.register(new Setting<Double>("mainX", 1.2, 0.0, 6.0));
    public Setting<Double> mainY = this.register(new Setting<Double>("mainY", -0.95, -3.0, 3.0));
    public Setting<Double> mainZ = this.register(new Setting<Double>("mainZ", -1.45, -5.0, 5.0));
    public Setting<Double> offX = this.register(new Setting<Double>("offX", -1.2, -6.0, 0.0));
    public Setting<Double> offY = this.register(new Setting<Double>("offY", -0.95, -3.0, 3.0));
    public Setting<Double> offZ = this.register(new Setting<Double>("offZ", -1.45, -5.0, 5.0));
    public Setting<Double> mainAngel = this.register(new Setting<Double>("mainAngle", 0.0, 0.0, 360.0));
    public Setting<Double> mainRx = this.register(new Setting<Double>("mainRotationPointX", 0.0, -1.0, 1.0));
    public Setting<Double> mainRy = this.register(new Setting<Double>("mainRotationPointY", 0.0, -1.0, 1.0));
    public Setting<Double> mainRz = this.register(new Setting<Double>("mainRotationPointZ", 0.0, -1.0, 1.0));
    public Setting<Double> offAngel = this.register(new Setting<Double>("offAngle", 0.0, 0.0, 360.0));
    public Setting<Double> offRx = this.register(new Setting<Double>("offRotationPointX", 0.0, -1.0, 1.0));
    public Setting<Double> offRy = this.register(new Setting<Double>("offRotationPointY", 0.0, -1.0, 1.0));
    public Setting<Double> offRz = this.register(new Setting<Double>("offRotationPointZ", 0.0, -1.0, 1.0));
    public Setting<Double> mainScaleX = this.register(new Setting<Double>("mainScaleX", 1.0, -5.0, 10.0));
    public Setting<Double> mainScaleY = this.register(new Setting<Double>("mainScaleY", 1.0, -5.0, 10.0));
    public Setting<Double> mainScaleZ = this.register(new Setting<Double>("mainScaleZ", 1.0, -5.0, 10.0));
    public Setting<Double> offScaleX = this.register(new Setting<Double>("offScaleX", 1.0, -5.0, 10.0));
    public Setting<Double> offScaleY = this.register(new Setting<Double>("offScaleY", 1.0, -5.0, 10.0));
    public Setting<Double> offScaleZ = this.register(new Setting<Double>("offScaleZ", 1.0, -5.0, 10.0));


    public ViewModel() {
        super("ViewModel", "Cool", Category.RENDER, true, false, false);

    }

    @SubscribeEvent
    public void onItemRender(RenderItemEvent event) {
        event.setMainX(mainX.getValue());
        event.setMainY(mainY.getValue());
        event.setMainZ(mainZ.getValue());

        event.setOffX(offX.getValue());
        event.setOffY(offY.getValue());
        event.setOffZ(offZ.getValue());

        event.setMainRAngel(mainAngel.getValue());
        event.setMainRx(mainRx.getValue());
        event.setMainRy(mainRy.getValue());
        event.setMainRz(mainRz.getValue());

        event.setOffRAngel(offAngel.getValue());
        event.setOffRx(offRx.getValue());
        event.setOffRy(offRy.getValue());
        event.setOffRz(offRz.getValue());

        event.setMainHandScaleX(mainScaleX.getValue());
        event.setMainHandScaleY(mainScaleY.getValue());
        event.setMainHandScaleZ(mainScaleZ.getValue());

        event.setOffHandScaleX(offScaleX.getValue());
        event.setOffHandScaleY(offScaleY.getValue());
        event.setOffHandScaleZ(offScaleZ.getValue());
    }




}