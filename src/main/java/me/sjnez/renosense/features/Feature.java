package me.sjnez.renosense.features;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.features.gui.PhobosGui;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.manager.TextManager;
import me.sjnez.renosense.util.Util;

import java.util.ArrayList;
import java.util.List;

public class Feature
        implements Util {
    public List<Setting> settings = new ArrayList <> ( );
    public TextManager renderer = RenoSense.textManager;
    private String name;

    public Feature() {
    }

    public Feature(String name) {
        this.name = name;
    }

    public static boolean nullCheck() {
        return Feature.mc.player == null;
    }

    public static boolean fullNullCheck() {
        return Feature.mc.player == null || Feature.mc.world == null;
    }

    public String getName() {
        return this.name;
    }

    public List<Setting> getSettings() {
        return this.settings;
    }

    public boolean hasSettings() {
        return !this.settings.isEmpty();
    }

    public boolean isEnabled() {
        if (this instanceof Module) {
            return ((Module) this).isOn();
        }
        return false;
    }

    public boolean isDisabled() {
        return !this.isEnabled();
    }

    public Setting register(Setting setting) {
        setting.setFeature(this);
        this.settings.add(setting);
        if (this instanceof Module && Feature.mc.currentScreen instanceof PhobosGui) {
            PhobosGui.getInstance().updateModule((Module) this);
        }
        return setting;
    }

    public void unregister(Setting settingIn) {
        ArrayList<Setting> removeList = new ArrayList <> ( );
        for (Setting setting : this.settings) {
            if (!setting.equals(settingIn)) continue;
            removeList.add(setting);
        }
        if (!removeList.isEmpty()) {
            this.settings.removeAll(removeList);
        }
        if (this instanceof Module && Feature.mc.currentScreen instanceof PhobosGui) {
            PhobosGui.getInstance().updateModule((Module) this);
        }
    }

    public Setting getSettingByName(String name) {
        for (Setting setting : this.settings) {
            if (!setting.getName().equalsIgnoreCase(name)) continue;
            return setting;
        }
        return null;
    }

    public void reset() {
        for (Setting setting : this.settings) {
            setting.setValue(setting.getDefaultValue());
        }
    }

    public void clearSettings() {
        this.settings = new ArrayList <> ( );
    }
}

