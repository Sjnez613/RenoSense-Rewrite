package me.sjnez.renosense.features.modules.player;

import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.util.Util;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryBasic;

public class EchestBP
        extends Module {
    private GuiScreen echestScreen = null;

    public EchestBP() {
        super("EchestBP", "Allows to open your echest later.", Module.Category.PLAYER, false, false, false);
    }

    @Override
    public void onUpdate() {
        Container container;
        if (EchestBP.mc.currentScreen instanceof GuiContainer && (container = ((GuiContainer) EchestBP.mc.currentScreen).inventorySlots) instanceof ContainerChest && ((ContainerChest) container).getLowerChestInventory() instanceof InventoryBasic && ((ContainerChest) container).getLowerChestInventory().getName().equalsIgnoreCase("Ender Chest")) {
            this.echestScreen = EchestBP.mc.currentScreen;
            EchestBP.mc.currentScreen = null;
        }
    }

    @Override
    public void onDisable() {
        if (!EchestBP.fullNullCheck() && this.echestScreen != null) {
            Util.mc.displayGuiScreen(this.echestScreen);
        }
        this.echestScreen = null;
    }
}

