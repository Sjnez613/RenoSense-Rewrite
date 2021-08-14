package me.sjnez.renosense.features.modules.render;

import me.sjnez.renosense.event.events.Render3DEvent;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.modules.client.Colors;
import me.sjnez.renosense.features.modules.client.HUD;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.RenderUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.awt.*;

public class BlockHighlight
        extends Module {
    private final Setting<Integer> red = this.register( new Setting <> ( "Red" , 0 , 0 , 255 ));
    private final Setting<Integer> green = this.register( new Setting <> ( "Green" , 255 , 0 , 255 ));
    private final Setting<Integer> blue = this.register( new Setting <> ( "Blue" , 0 , 0 , 255 ));
    private final Setting<Integer> alpha = this.register( new Setting <> ( "Alpha" , 255 , 0 , 255 ));
    public Setting<Boolean> colorSync = this.register( new Setting <> ( "Sync" , false ));
    public Setting<Boolean> rolling = this.register(new Setting<Object>("Rolling", Boolean.FALSE , v -> this.colorSync.getValue()));
    public Setting<Boolean> box = this.register( new Setting <> ( "Box" , false ));
    private final Setting<Integer> boxAlpha = this.register(new Setting<Object>("BoxAlpha", 125 , 0 , 255 , v -> this.box.getValue()));
    public Setting<Boolean> outline = this.register( new Setting <> ( "Outline" , true ));
    private final Setting<Float> lineWidth = this.register(new Setting<Object>("LineWidth", 1.0f , 0.1f , 5.0f , v -> this.outline.getValue()));
    public Setting<Boolean> customOutline = this.register(new Setting<Object>("CustomLine", Boolean.FALSE , v -> this.outline.getValue()));
    private final Setting<Integer> cRed = this.register(new Setting<Object>("OL-Red", 255 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cGreen = this.register(new Setting<Object>("OL-Green", 255 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cBlue = this.register(new Setting<Object>("OL-Blue", 255 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cAlpha = this.register(new Setting<Object>("OL-Alpha", 255 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));

    public BlockHighlight() {
        super("BlockHighlight", "Highlights the block u look at.", Module.Category.RENDER, false, false, false);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        RayTraceResult ray = BlockHighlight.mc.objectMouseOver;
        if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = ray.getBlockPos();
            if ( this.rolling.getValue ( ) ) {
                RenderUtil.drawProperGradientBlockOutline(blockpos, new Color(HUD.getInstance().colorMap.get(0)), new Color(HUD.getInstance().colorMap.get(this.renderer.scaledHeight / 4)), new Color(HUD.getInstance().colorMap.get(this.renderer.scaledHeight / 2)), 1.0f);
            } else {
                RenderUtil.drawBoxESP(blockpos, this.colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor() : new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()), this.customOutline.getValue(), new Color(this.cRed.getValue(), this.cGreen.getValue(), this.cBlue.getValue(), this.cAlpha.getValue()), this.lineWidth.getValue ( ) , this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), false);
            }
        }
    }
}

