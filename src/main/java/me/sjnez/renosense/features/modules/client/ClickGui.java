package me.sjnez.renosense.features.modules.client;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.event.events.ClientEvent;
import me.sjnez.renosense.features.command.Command;
import me.sjnez.renosense.features.gui.PhobosGui;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.Util;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClickGui
        extends Module {
    private static ClickGui INSTANCE = new ClickGui();
    public Setting<Boolean> colorSync = this.register( new Setting <> ( "Sync" , false ));
    public Setting<Boolean> outline = this.register( new Setting <> ( "Outline" , false ));
    public Setting<Boolean> rainbowRolling = this.register(new Setting<Object>("RollingRainbow", Boolean.FALSE , v -> this.colorSync.getValue ( ) && Colors.INSTANCE.rainbow.getValue ( ) ));
    public Setting<String> prefix = this.register( new Setting <> ( "Prefix" , "." ).setRenderName(true));
    public Setting<Integer> red = this.register( new Setting <> ( "Red" , 255 , 0 , 255 ));
    public Setting<Integer> green = this.register( new Setting <> ( "Green" , 0 , 0 , 255 ));
    public Setting<Integer> blue = this.register( new Setting <> ( "Blue" , 0 , 0 , 255 ));
    public Setting<Integer> hoverAlpha = this.register( new Setting <> ( "Alpha" , 180 , 0 , 255 ));
    public Setting<Integer> alpha = this.register( new Setting <> ( "HoverAlpha" , 240 , 0 , 255 ));
    public Setting<Boolean> customFov = this.register( new Setting <> ( "CustomFov" , false ));
    public Setting<Float> fov = this.register(new Setting<Object>("Fov", 150.0f , - 180.0f , 180.0f , v -> this.customFov.getValue()));
    public Setting<Boolean> openCloseChange = this.register( new Setting <> ( "Open/Close" , false ));
    public Setting<String> open = this.register(new Setting<Object>("Open:", "", v -> this.openCloseChange.getValue()).setRenderName(true));
    public Setting<String> close = this.register(new Setting<Object>("Close:", "", v -> this.openCloseChange.getValue()).setRenderName(true));
    public Setting<String> moduleButton = this.register(new Setting<Object>("Buttons:", "", v -> ! this.openCloseChange.getValue ( ) ).setRenderName(true));
    public Setting<Boolean> devSettings = this.register( new Setting <> ( "DevSettings" , false ));
    public Setting<Integer> topRed = this.register(new Setting<Object>("TopRed", 255 , 0 , 255 , v -> this.devSettings.getValue()));
    public Setting<Integer> topGreen = this.register(new Setting<Object>("TopGreen", 0 , 0 , 255 , v -> this.devSettings.getValue()));
    public Setting<Integer> topBlue = this.register(new Setting<Object>("TopBlue", 0 , 0 , 255 , v -> this.devSettings.getValue()));
    public Setting<Integer> topAlpha = this.register(new Setting<Object>("TopAlpha", 255 , 0 , 255 , v -> this.devSettings.getValue()));

    public ClickGui() {
        super("ClickGui", "Opens the ClickGui", Module.Category.CLIENT, true, false, false);
        this.setInstance();
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if ( this.customFov.getValue ( ) ) {
            ClickGui.mc.gameSettings.setOptionFloatValue(GameSettings.Options.FOV, this.fov.getValue ( ) );
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
            if (event.getSetting().equals(this.prefix)) {
                RenoSense.commandManager.setPrefix(this.prefix.getPlannedValue());
                Command.sendMessage("Prefix set to \u00a7a" + RenoSense.commandManager.getPrefix());
            }
            RenoSense.colorManager.setColor(this.red.getPlannedValue(), this.green.getPlannedValue(), this.blue.getPlannedValue(), this.hoverAlpha.getPlannedValue());
        }
    }

    @Override
    public void onEnable() {
        Util.mc.displayGuiScreen(new PhobosGui());
    }

    @Override
    public void onLoad() {
        if ( this.colorSync.getValue ( ) ) {
            RenoSense.colorManager.setColor(Colors.INSTANCE.getCurrentColor().getRed(), Colors.INSTANCE.getCurrentColor().getGreen(), Colors.INSTANCE.getCurrentColor().getBlue(), this.hoverAlpha.getValue());
        } else {
            RenoSense.colorManager.setColor(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.hoverAlpha.getValue());
        }
        RenoSense.commandManager.setPrefix(this.prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(ClickGui.mc.currentScreen instanceof PhobosGui)) {
            this.disable();
        }
    }

    @Override
    public void onDisable() {
        if (ClickGui.mc.currentScreen instanceof PhobosGui) {
            Util.mc.displayGuiScreen(null);
        }
    }
}

