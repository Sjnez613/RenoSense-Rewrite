package me.sjnez.renosense.mixin.mixins;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.features.gui.custom.GuiCustomNewChat;
import me.sjnez.renosense.features.modules.client.HUD;
import me.sjnez.renosense.features.modules.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GuiIngame.class})
public class MixinGuiIngame
extends Gui {
    @Shadow
    @Final
    public GuiNewChat persistantChatGUI;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    public void init(Minecraft mcIn, CallbackInfo ci) {
        this.persistantChatGUI = new GuiCustomNewChat(mcIn);
    }

    @Inject(method={"renderPortal"}, at={@At(value="HEAD")}, cancellable=true)
    protected void renderPortalHook(float n, ScaledResolution scaledResolution, CallbackInfo info) {
        if (NoRender.getInstance().isOn() && NoRender.getInstance ( ).portal.getValue ( ) ) {
            info.cancel();
        }
    }

    @Inject(method={"renderPumpkinOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    protected void renderPumpkinOverlayHook(ScaledResolution scaledRes, CallbackInfo info) {
        if (NoRender.getInstance().isOn() && NoRender.getInstance ( ).pumpkin.getValue ( ) ) {
            info.cancel();
        }
    }

    @Inject(method={"renderPotionEffects"}, at={@At(value="HEAD")}, cancellable=true)
    protected void renderPotionEffectsHook(ScaledResolution scaledRes, CallbackInfo info) {
        if (RenoSense.moduleManager != null && ! HUD.getInstance ( ).potionIcons.getValue ( ) ) {
            info.cancel();
        }
    }
}

