package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.PerspectiveEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Aspect extends Module{
    public Setting<Double> aspect = this.register(new Setting<Double>("aspect", mc.displayWidth / mc.displayHeight +0.0, 0.0, 3.0));


    public Aspect(){
        super("AspectRatio", "Stretched res like fortnite", Category.RENDER, true, false, false);

    }

    @SubscribeEvent
    public void onPerspectiveEvent(PerspectiveEvent event){
        event.setAspect(aspect.getValue().floatValue());
    }
}