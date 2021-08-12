package me.sjnez.renosense.features.gui.components.items.buttons;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.features.gui.PhobosGui;
import me.sjnez.renosense.features.modules.client.ClickGui;
import me.sjnez.renosense.features.modules.client.HUD;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.ColorUtil;
import me.sjnez.renosense.util.MathUtil;
import me.sjnez.renosense.util.RenderUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

public class EnumButton
        extends Button {
    public Setting setting;

    public EnumButton(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (ClickGui.getInstance().rainbowRolling.getValue().booleanValue()) {
            int color = ColorUtil.changeAlpha(HUD.getInstance().colorMap.get(MathUtil.clamp((int) this.y, 0, this.renderer.scaledHeight)), RenoSense.moduleManager.getModuleByClass(ClickGui.class).hoverAlpha.getValue());
            int color1 = ColorUtil.changeAlpha(HUD.getInstance().colorMap.get(MathUtil.clamp((int) this.y + this.height, 0, this.renderer.scaledHeight)), RenoSense.moduleManager.getModuleByClass(ClickGui.class).hoverAlpha.getValue());
            RenderUtil.drawGradientRect(this.x, this.y, (float) this.width + 7.4f, (float) this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? HUD.getInstance().colorMap.get(MathUtil.clamp((int) this.y, 0, this.renderer.scaledHeight)) : color) : (!this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515), this.getState() ? (!this.isHovering(mouseX, mouseY) ? HUD.getInstance().colorMap.get(MathUtil.clamp((int) this.y + this.height, 0, this.renderer.scaledHeight)) : color1) : (!this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
        } else {
            RenderUtil.drawRect(this.x, this.y, this.x + (float) this.width + 7.4f, this.y + (float) this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? RenoSense.colorManager.getColorWithAlpha(RenoSense.moduleManager.getModuleByClass(ClickGui.class).hoverAlpha.getValue()) : RenoSense.colorManager.getColorWithAlpha(RenoSense.moduleManager.getModuleByClass(ClickGui.class).alpha.getValue())) : (!this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
        }
        RenoSense.textManager.drawStringWithShadow(this.setting.getName() + " " + "\u00a77" + this.setting.currentEnumName(), this.x + 2.3f, this.y - 1.7f - (float) PhobosGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void toggle() {
        this.setting.increaseEnum();
    }

    @Override
    public boolean getState() {
        return true;
    }
}

