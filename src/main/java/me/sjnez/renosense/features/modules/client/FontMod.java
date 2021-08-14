package me.sjnez.renosense.features.modules.client;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.event.events.ClientEvent;
import me.sjnez.renosense.features.command.Command;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class FontMod
        extends Module {
    private static FontMod INSTANCE = new FontMod();
    public Setting<String> fontName = this.register( new Setting <> ( "FontName" , "Arial" , "Name of the font." ));
    public Setting<Integer> fontSize = this.register( new Setting <> ( "FontSize" , 18 , "Size of the font." ));
    public Setting<Integer> fontStyle = this.register( new Setting <> ( "FontStyle" , 0 , "Style of the font." ));
    public Setting<Boolean> antiAlias = this.register( new Setting <> ( "AntiAlias" , Boolean.TRUE , "Smoother font." ));
    public Setting<Boolean> fractionalMetrics = this.register( new Setting <> ( "Metrics" , Boolean.TRUE , "Thinner font." ));
    public Setting<Boolean> shadow = this.register( new Setting <> ( "Shadow" , Boolean.TRUE , "Less shadow offset font." ));
    public Setting<Boolean> showFonts = this.register( new Setting <> ( "Fonts" , Boolean.FALSE , "Shows all fonts." ));
    public Setting<Boolean> full = this.register( new Setting <> ( "Full" , false ));
    private boolean reloadFont = false;

    public FontMod() {
        super("CustomFont", "CustomFont for all of the clients text. Use the font command.", Module.Category.CLIENT, true, false, false);
        this.setInstance();
    }

    public static FontMod getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FontMod();
        }
        return INSTANCE;
    }

    public static boolean checkFont(String font, boolean message) {
        for (String s : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (!message && s.equals(font)) {
                return true;
            }
            if (!message) continue;
            Command.sendMessage(s);
        }
        return false;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        Setting setting;
        if (event.getStage() == 2 && (setting = event.getSetting()) != null && setting.getFeature().equals(this)) {
            if (setting.getName().equals("FontName") && !FontMod.checkFont(setting.getPlannedValue().toString(), false)) {
                Command.sendMessage("\u00a7cThat font doesnt exist.");
                event.setCanceled(true);
                return;
            }
            this.reloadFont = true;
        }
    }

    @Override
    public void onTick() {
        if ( this.showFonts.getValue ( ) ) {
            FontMod.checkFont("Hello", true);
            Command.sendMessage("Current Font: " + this.fontName.getValue());
            this.showFonts.setValue(false);
        }
        if (this.reloadFont) {
            RenoSense.textManager.init(false);
            this.reloadFont = false;
        }
    }
}

