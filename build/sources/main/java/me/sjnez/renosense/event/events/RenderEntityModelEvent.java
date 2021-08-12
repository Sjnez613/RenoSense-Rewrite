package me.sjnez.renosense.event.events;

import me.sjnez.renosense.event.EventStage;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class RenderEntityModelEvent
        extends EventStage {
    public ModelBase modelBase;
    public Entity entity;
    public float limbSwing;
    public float limbSwingAmount;
    public float age;
    public float headYaw;
    public float headPitch;
    public float scale;

    public RenderEntityModelEvent(int stage, ModelBase modelBase, Entity entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch, float scale) {
        super(stage);
        this.modelBase = modelBase;
        this.entity = entity;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.age = age;
        this.headYaw = headYaw;
        this.headPitch = headPitch;
        this.scale = scale;
    }
}

