package me.sjnez.renosense.features.modules.render;

import me.sjnez.renosense.event.events.Render3DEvent;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class VoidESP
        extends Module {
    private final Setting<Float> radius = this.register( new Setting <> ( "Radius" , 8.0f , 0.0f , 50.0f ));
    private final Timer timer = new Timer();
    public Setting<Boolean> air = this.register( new Setting <> ( "OnlyAir" , true ));
    public Setting<Boolean> noEnd = this.register( new Setting <> ( "NoEnd" , true ));
    public Setting<Boolean> box = this.register( new Setting <> ( "Box" , true ));
    public Setting<Boolean> outline = this.register( new Setting <> ( "Outline" , true ));
    public Setting<Boolean> colorSync = this.register( new Setting <> ( "Sync" , false ));
    public Setting<Double> height = this.register( new Setting <> ( "Height" , 0.0 , - 2.0 , 2.0 ));
    public Setting<Boolean> customOutline = this.register(new Setting<Object>("CustomLine", Boolean.FALSE , v -> this.outline.getValue()));
    private final Setting<Integer> updates = this.register( new Setting <> ( "Updates" , 500 , 0 , 1000 ));
    private final Setting<Integer> voidCap = this.register( new Setting <> ( "VoidCap" , 500 , 0 , 1000 ));
    private final Setting<Integer> red = this.register( new Setting <> ( "Red" , 0 , 0 , 255 ));
    private final Setting<Integer> green = this.register( new Setting <> ( "Green" , 255 , 0 , 255 ));
    private final Setting<Integer> blue = this.register( new Setting <> ( "Blue" , 0 , 0 , 255 ));
    private final Setting<Integer> alpha = this.register( new Setting <> ( "Alpha" , 255 , 0 , 255 ));
    private final Setting<Integer> boxAlpha = this.register(new Setting<Object>("BoxAlpha", 125 , 0 , 255 , v -> this.box.getValue()));
    private final Setting<Float> lineWidth = this.register(new Setting<Object>("LineWidth", 1.0f , 0.1f , 5.0f , v -> this.outline.getValue()));
    private final Setting<Integer> cRed = this.register(new Setting<Object>("OL-Red", 0 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cGreen = this.register(new Setting<Object>("OL-Green", 0 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cBlue = this.register(new Setting<Object>("OL-Blue", 255 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cAlpha = this.register(new Setting<Object>("OL-Alpha", 255 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private List<BlockPos> voidHoles = new CopyOnWriteArrayList <> ( );

    public VoidESP() {
        super("VoidEsp", "Esps the void", Module.Category.RENDER, true, false, false);
    }

    @Override
    public void onToggle() {
        this.timer.reset();
    }

    @Override
    public void onLogin() {
        this.timer.reset();
    }

    @Override
    public void onTick() {
        if (!(VoidESP.fullNullCheck() || this.noEnd.getValue ( ) && VoidESP.mc.player.dimension == 1 || !this.timer.passedMs( this.updates.getValue ( ) ))) {
            this.voidHoles.clear();
            this.voidHoles = this.findVoidHoles();
            if (this.voidHoles.size() > this.voidCap.getValue()) {
                this.voidHoles.clear();
            }
            this.timer.reset();
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (VoidESP.fullNullCheck() || this.noEnd.getValue ( ) && VoidESP.mc.player.dimension == 1) {
            return;
        }
        for (BlockPos pos : this.voidHoles) {
            if (!RotationUtil.isInFov(pos)) continue;
            RenderUtil.drawBoxESP(pos, new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()), this.customOutline.getValue(), new Color(this.cRed.getValue(), this.cGreen.getValue(), this.cBlue.getValue(), this.cAlpha.getValue()), this.lineWidth.getValue ( ) , this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), true, this.height.getValue(), false, false, false, false, 0);
        }
    }

    private List<BlockPos> findVoidHoles() {
        BlockPos playerPos = EntityUtil.getPlayerPos(VoidESP.mc.player);
        return BlockUtil.getDisc(playerPos.add(0, -playerPos.getY(), 0), this.radius.getValue ( ) ).stream().filter(this::isVoid).collect(Collectors.toList());
    }

    private boolean isVoid(BlockPos pos) {
        return (VoidESP.mc.world.getBlockState(pos).getBlock() == Blocks.AIR || ! this.air.getValue ( ) && VoidESP.mc.world.getBlockState(pos).getBlock() != Blocks.BEDROCK) && pos.getY() < 1 && pos.getY() >= 0;
    }
}

