package me.sjnez.renosense.features.modules.client;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.ColorUtil;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Colors
        extends Module {
    public static Colors INSTANCE;
    public Setting<Boolean> rainbow = this.register( new Setting <> ( "Rainbow" , Boolean.FALSE , "Rainbow colors." ));
    public Setting<Integer> rainbowSpeed = this.register(new Setting<Object>("Speed", 20 , 0 , 100 , v -> this.rainbow.getValue()));
    public Setting<Integer> rainbowSaturation = this.register(new Setting<Object>("Saturation", 255 , 0 , 255 , v -> this.rainbow.getValue()));
    public Setting<Integer> rainbowBrightness = this.register(new Setting<Object>("Brightness", 255 , 0 , 255 , v -> this.rainbow.getValue()));
    public Setting<Integer> red = this.register(new Setting<Object>("Red", 255 , 0 , 255 , v -> ! this.rainbow.getValue ( ) ));
    public Setting<Integer> green = this.register(new Setting<Object>("Green", 255 , 0 , 255 , v -> ! this.rainbow.getValue ( ) ));
    public Setting<Integer> blue = this.register(new Setting<Object>("Blue", 255 , 0 , 255 , v -> ! this.rainbow.getValue ( ) ));
    public Setting<Integer> alpha = this.register(new Setting<Object>("Alpha", 255 , 0 , 255 , v -> ! this.rainbow.getValue ( ) ));
    public float hue;
    public Map<Integer, Integer> colorHeightMap = new HashMap <> ( );

    public Colors() {
        super("Colors", "Universal colors.", Module.Category.CLIENT, true, false, true);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        int colorSpeed = 101 - this.rainbowSpeed.getValue();
        float tempHue = this.hue = (float) (System.currentTimeMillis() % (long) (360 * colorSpeed)) / (360.0f * (float) colorSpeed);
        for (int i = 0; i <= 510; ++i) {
            this.colorHeightMap.put(i, Color.HSBtoRGB(tempHue, (float) this.rainbowSaturation.getValue ( ) / 255.0f, (float) this.rainbowBrightness.getValue ( ) / 255.0f));
            tempHue += 0.0013071896f;
        }
        if ( ClickGui.getInstance ( ).colorSync.getValue ( ) ) {
            RenoSense.colorManager.setColor(INSTANCE.getCurrentColor().getRed(), INSTANCE.getCurrentColor().getGreen(), INSTANCE.getCurrentColor().getBlue(), ClickGui.getInstance().hoverAlpha.getValue());
        }
    }

    public int getCurrentColorHex() {
        if ( this.rainbow.getValue ( ) ) {
            return Color.HSBtoRGB(this.hue, (float) this.rainbowSaturation.getValue ( ) / 255.0f, (float) this.rainbowBrightness.getValue ( ) / 255.0f);
        }
        return ColorUtil.toARGB(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue());
    }

    public Color getCurrentColor() {
        if ( this.rainbow.getValue ( ) ) {
            return Color.getHSBColor(this.hue, (float) this.rainbowSaturation.getValue ( ) / 255.0f, (float) this.rainbowBrightness.getValue ( ) / 255.0f);
        }
        return new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue());
    }
}

