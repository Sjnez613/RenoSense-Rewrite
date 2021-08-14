package me.sjnez.renosense.features.modules.render;

import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;

public class Chams
        extends Module {
    private static Chams INSTANCE = new Chams();
    public Setting<Boolean> colorSync = this.register( new Setting <> ( "Sync" , false ));
    public Setting<Boolean> colored = this.register( new Setting <> ( "Colored" , false ));
    public Setting<Boolean> textured = this.register( new Setting <> ( "Textured" , false ));
    public Setting<Boolean> rainbow = this.register(new Setting<Object>("Rainbow", Boolean.FALSE , v -> this.colored.getValue()));
    public Setting<Integer> saturation = this.register(new Setting<Object>("Saturation", 50 , 0 , 100 , v -> this.colored.getValue ( ) && this.rainbow.getValue ( ) ));
    public Setting<Integer> brightness = this.register(new Setting<Object>("Brightness", 100 , 0 , 100 , v -> this.colored.getValue ( ) && this.rainbow.getValue ( ) ));
    public Setting<Integer> speed = this.register(new Setting<Object>("Speed", 40 , 1 , 100 , v -> this.colored.getValue ( ) && this.rainbow.getValue ( ) ));
    public Setting<Boolean> xqz = this.register(new Setting<Object>("XQZ", Boolean.FALSE , v -> this.colored.getValue ( ) && ! this.rainbow.getValue ( ) ));
    public Setting<Integer> red = this.register(new Setting<Object>("Red", 0 , 0 , 255 , v -> this.colored.getValue ( ) && ! this.rainbow.getValue ( ) ));
    public Setting<Integer> green = this.register(new Setting<Object>("Green", 255 , 0 , 255 , v -> this.colored.getValue ( ) && ! this.rainbow.getValue ( ) ));
    public Setting<Integer> blue = this.register(new Setting<Object>("Blue", 0 , 0 , 255 , v -> this.colored.getValue ( ) && ! this.rainbow.getValue ( ) ));
    public Setting<Integer> alpha = this.register(new Setting<Object>("Alpha", 255 , 0 , 255 , v -> this.colored.getValue()));
    public Setting<Integer> hiddenRed = this.register(new Setting<Object>("Hidden Red", 255 , 0 , 255 , v -> this.colored.getValue ( ) && this.xqz.getValue ( ) && ! this.rainbow.getValue ( ) ));
    public Setting<Integer> hiddenGreen = this.register(new Setting<Object>("Hidden Green", 0 , 0 , 255 , v -> this.colored.getValue ( ) && this.xqz.getValue ( ) && ! this.rainbow.getValue ( ) ));
    public Setting<Integer> hiddenBlue = this.register(new Setting<Object>("Hidden Blue", 255 , 0 , 255 , v -> this.colored.getValue ( ) && this.xqz.getValue ( ) && ! this.rainbow.getValue ( ) ));
    public Setting<Integer> hiddenAlpha = this.register(new Setting<Object>("Hidden Alpha", 255 , 0 , 255 , v -> this.colored.getValue ( ) && this.xqz.getValue ( ) && ! this.rainbow.getValue ( ) ));

    public Chams() {
        super("Chams", "Renders players through walls.", Module.Category.RENDER, false, false, false);
        this.setInstance();
    }

    public static Chams getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Chams();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }
}

