package me.sjnez.renosense.features.modules.render;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.event.events.Render3DEvent;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.RenderUtil;
import me.sjnez.renosense.util.RotationUtil;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.Random;

public class HoleESP
        extends Module {
    private static HoleESP INSTANCE = new HoleESP();
    public Setting<Boolean> ownHole = this.register( new Setting <> ( "OwnHole" , false ));
    public Setting<Boolean> box = this.register( new Setting <> ( "Box" , true ));
    public Setting<Boolean> gradientBox = this.register(new Setting<Object>("GradientBox", Boolean.FALSE , v -> this.box.getValue()));
    public Setting<Boolean> pulseAlpha = this.register(new Setting<Object>("PulseAlpha", Boolean.FALSE , v -> this.gradientBox.getValue()));
    public Setting<Boolean> pulseOutline = this.register(new Setting<Object>("PulseOutline", Boolean.TRUE , v -> this.gradientBox.getValue()));
    public Setting<Boolean> invertGradientBox = this.register(new Setting<Object>("InvertGradientBox", Boolean.FALSE , v -> this.gradientBox.getValue()));
    public Setting<Boolean> outline = this.register( new Setting <> ( "Outline" , true ));
    public Setting<Boolean> gradientOutline = this.register(new Setting<Object>("GradientOutline", Boolean.FALSE , v -> this.outline.getValue()));
    public Setting<Boolean> invertGradientOutline = this.register(new Setting<Object>("InvertGradientOutline", Boolean.FALSE , v -> this.gradientOutline.getValue()));
    public Setting<Double> height = this.register( new Setting <> ( "Height" , 0.0 , - 2.0 , 2.0 ));
    public Setting<Boolean> safeColor = this.register( new Setting <> ( "SafeColor" , false ));
    public Setting<Boolean> customOutline = this.register(new Setting<Object>("CustomLine", Boolean.FALSE , v -> this.outline.getValue()));
    private final Setting<Boolean> x = this.register(new Setting("X", true));
    private final Setting<Integer> holes = this.register( new Setting <> ( "Holes" , 3 , 1 , 500 ));
    private final Setting<Integer> minPulseAlpha = this.register(new Setting<Object>("MinPulse", 10 , 0 , 255 , v -> this.pulseAlpha.getValue()));
    private final Setting<Integer> maxPulseAlpha = this.register(new Setting<Object>("MaxPulse", 40 , 0 , 255 , v -> this.pulseAlpha.getValue()));
    private final Setting<Integer> pulseSpeed = this.register(new Setting<Object>("PulseSpeed", 10 , 1 , 50 , v -> this.pulseAlpha.getValue()));
    private final Setting<Integer> red = this.register( new Setting <> ( "Red" , 0 , 0 , 255 ));
    private final Setting<Integer> green = this.register( new Setting <> ( "Green" , 255 , 0 , 255 ));
    private final Setting<Integer> blue = this.register( new Setting <> ( "Blue" , 0 , 0 , 255 ));
    private final Setting<Integer> alpha = this.register( new Setting <> ( "Alpha" , 255 , 0 , 255 ));
    private final Setting<Integer> boxAlpha = this.register(new Setting<Object>("BoxAlpha", 125 , 0 , 255 , v -> this.box.getValue()));
    private final Setting<Float> lineWidth = this.register(new Setting<Object>("LineWidth", 1.0f , 0.1f , 5.0f , v -> this.outline.getValue()));
    private final Setting<Integer> safeRed = this.register(new Setting<Object>("SafeRed", 0 , 0 , 255 , v -> this.safeColor.getValue()));
    private final Setting<Integer> safeGreen = this.register(new Setting<Object>("SafeGreen", 255 , 0 , 255 , v -> this.safeColor.getValue()));
    private final Setting<Integer> safeBlue = this.register(new Setting<Object>("SafeBlue", 0 , 0 , 255 , v -> this.safeColor.getValue()));
    private final Setting<Integer> safeAlpha = this.register(new Setting<Object>("SafeAlpha", 255 , 0 , 255 , v -> this.safeColor.getValue()));
    private final Setting<Integer> cRed = this.register(new Setting<Object>("OL-Red", 0 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cGreen = this.register(new Setting<Object>("OL-Green", 0 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cBlue = this.register(new Setting<Object>("OL-Blue", 255 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cAlpha = this.register(new Setting<Object>("OL-Alpha", 255 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> safecRed = this.register(new Setting<Object>("OL-SafeRed", 0 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) && this.safeColor.getValue ( ) ));
    private final Setting<Integer> safecGreen = this.register(new Setting<Object>("OL-SafeGreen", 255 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) && this.safeColor.getValue ( ) ));
    private final Setting<Integer> safecBlue = this.register(new Setting<Object>("OL-SafeBlue", 0 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) && this.safeColor.getValue ( ) ));
    private final Setting<Integer> safecAlpha = this.register(new Setting<Object>("OL-SafeAlpha", 255 , 0 , 255 , v -> this.customOutline.getValue ( ) && this.outline.getValue ( ) && this.safeColor.getValue ( ) ));
    private boolean pulsing = false;
    private boolean shouldDecrease = false;
    private int pulseDelay = 0;
    private int currentPulseAlpha;
    private int currentAlpha = 0;

    public HoleESP() {
        super("HoleESP", "Shows safe spots.", Module.Category.RENDER, false, false, false);
        this.setInstance();
    }

    public static HoleESP getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HoleESP();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        int drawnHoles = 0;
        if (!this.pulsing && this.pulseAlpha.getValue ( ) ) {
            Random rand = new Random();
            this.currentPulseAlpha = rand.nextInt(this.maxPulseAlpha.getValue() - this.minPulseAlpha.getValue() + 1) + this.minPulseAlpha.getValue();
            this.pulsing = true;
            this.shouldDecrease = false;
        }
        if (this.pulseDelay == 0) {
            if (this.pulsing && this.pulseAlpha.getValue ( ) && !this.shouldDecrease) {
                ++this.currentAlpha;
                if (this.currentAlpha >= this.currentPulseAlpha) {
                    this.shouldDecrease = true;
                }
            }
            if (this.pulsing && this.pulseAlpha.getValue ( ) && this.shouldDecrease) {
                --this.currentAlpha;
            }
            if (this.currentAlpha <= 0) {
                this.pulsing = false;
                this.shouldDecrease = false;
            }
            ++this.pulseDelay;
        } else {
            ++this.pulseDelay;
            if (this.pulseDelay == 51 - this.pulseSpeed.getValue()) {
                this.pulseDelay = 0;
            }
        }
        if (! this.pulseAlpha.getValue ( ) || !this.pulsing) {
            this.currentAlpha = 0;
        }
        for (BlockPos pos : RenoSense.holeManager.getSortedHoles()) {
            if (drawnHoles >= this.holes.getValue()) break;
            if (pos.equals(new BlockPos(HoleESP.mc.player.posX, HoleESP.mc.player.posY, HoleESP.mc.player.posZ)) && ! this.ownHole.getValue ( ) || !RotationUtil.isInFov(pos))
                continue;
            if ( this.safeColor.getValue ( ) && RenoSense.holeManager.isSafe(pos)) {
                if (this.x.getValue()) {
                    RenderUtil.drawBoundingBoxBottomBlockPosXInMiddle(pos, 1, this.safeRed.getValue(), this.safeGreen.getValue(), this.safeBlue.getValue(), this.safeAlpha.getValue());
                    RenderUtil.drawBoundingBoxBottomBlockPosXInMiddle2(pos, 1, this.safeRed.getValue(), this.safeGreen.getValue(), this.safeBlue.getValue(), this.safeAlpha.getValue());
                }
                RenderUtil.drawBoxESP(pos, new Color(this.safeRed.getValue(), this.safeGreen.getValue(), this.safeBlue.getValue(), this.safeAlpha.getValue()), this.customOutline.getValue(), new Color(this.safecRed.getValue(), this.safecGreen.getValue(), this.safecBlue.getValue(), this.safecAlpha.getValue()), this.lineWidth.getValue ( ) , this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), true, this.height.getValue(), this.gradientBox.getValue(), this.gradientOutline.getValue(), this.invertGradientBox.getValue(), this.invertGradientOutline.getValue(), this.currentAlpha);
            } else {
                if (this.x.getValue()) {
                    RenderUtil.drawBoundingBoxBottomBlockPosXInMiddle(pos, 1, this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue());
                    RenderUtil.drawBoundingBoxBottomBlockPosXInMiddle2(pos, 1, this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue());
                }
                RenderUtil.drawBoxESP(pos, new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()), this.customOutline.getValue(), new Color(this.cRed.getValue(), this.cGreen.getValue(), this.cBlue.getValue(), this.cAlpha.getValue()), this.lineWidth.getValue ( ) , this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), true, this.height.getValue(), this.gradientBox.getValue(), this.gradientOutline.getValue(), this.invertGradientBox.getValue(), this.invertGradientOutline.getValue(), this.currentAlpha);
            }
            ++drawnHoles;
        }
    }
}

