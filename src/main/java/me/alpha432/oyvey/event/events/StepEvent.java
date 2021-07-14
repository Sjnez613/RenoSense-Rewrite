package me.alpha432.oyvey.event.events;

import me.alpha432.oyvey.event.EventStage;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class StepEvent
        extends EventStage {
    private final Entity entity;
    private float height;

    public StepEvent(int stage, Entity entity) {
        super(stage);
        this.entity = entity;
        this.height = entity.stepHeight;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public float getHeight() {
        return this.height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
