package me.sjnez.renosense.features.modules.render;

import me.sjnez.renosense.event.events.BlockBreakingEvent;
import me.sjnez.renosense.event.events.Render3DEvent;
import me.sjnez.renosense.features.modules.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class BreakingESP
        extends Module {
    private final Map<BlockPos, Integer> breakingProgressMap = new HashMap <> ( );

    public BreakingESP() {
        super("BreakingESP", "Shows block breaking progress", Module.Category.RENDER, true, false, false);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockBreakingEvent event) {
        this.breakingProgressMap.put(event.pos, event.breakStage);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
    }

    public enum Mode {
        BAR,
        ALPHA,
        WIDTH

    }
}

