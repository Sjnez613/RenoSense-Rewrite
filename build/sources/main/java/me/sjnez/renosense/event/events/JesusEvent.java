package me.sjnez.renosense.event.events;

import me.sjnez.renosense.event.EventStage;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class JesusEvent
        extends EventStage {
    private BlockPos pos;
    private AxisAlignedBB boundingBox;

    public JesusEvent(int stage, BlockPos pos) {
        super(stage);
        this.pos = pos;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }
}

