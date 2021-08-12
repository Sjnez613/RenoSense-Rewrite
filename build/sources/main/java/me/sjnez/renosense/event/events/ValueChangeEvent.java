package me.sjnez.renosense.event.events;

import me.sjnez.renosense.event.EventStage;
import me.sjnez.renosense.features.setting.Setting;

public class ValueChangeEvent
        extends EventStage {
    public Setting setting;
    public Object value;

    public ValueChangeEvent(Setting setting, Object value) {
        this.setting = setting;
        this.value = value;
    }
}

