package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.ClickGui;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.ColorUtil;
import me.alpha432.oyvey.util.RenderUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BurrowESP extends Module{

    private Setting<Boolean> rainbow = register(new Setting("Rainbow", false));
    private Setting<Integer> red = register(new Setting("Red", 0, 0, 255, v -> !this.rainbow.getValue()));
    private Setting<Integer> green = register(new Setting("Green", 255, 0, 255, v -> !this.rainbow.getValue()));
    private Setting<Integer> blue = register(new Setting("Blue", 0, 0, 255, v -> !this.rainbow.getValue()));
    private Setting<Integer> alpha = register(new Setting("Alpha", 0, 0, 255));
    private Setting<Integer> outlineAlpha = register(new Setting("OL-Alpha", 0, 0, 255));

    private final List<BlockPos> posList = new ArrayList<>();


    public BurrowESP(){
        super("BurrowESP", "BURROWESP", Category.RENDER, true, false, false);
    }

    public void onTick(){
        posList.clear();
        for (EntityPlayer player : mc.world.playerEntities){
            BlockPos blockPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY+0.2), Math.floor(player.posZ));
            if(mc.world.getBlockState(blockPos).getBlock() == Blocks.ENDER_CHEST || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN){
                posList.add(blockPos);
            }
        }
    }



    @Override
    public void onRender3D(Render3DEvent event){

        for (BlockPos blockPos : posList){
            RenderUtil.drawBoxESP(blockPos, rainbow.getValue() ? ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()) : new Color(red.getValue(), green.getValue(), blue.getValue(), outlineAlpha.getValue()), 1.5F, true, true, alpha.getValue());
        }

    }


}
