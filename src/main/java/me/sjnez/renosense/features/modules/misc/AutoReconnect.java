package me.sjnez.renosense.features.modules.misc;

import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.MathUtil;
import me.sjnez.renosense.util.Timer;
import me.sjnez.renosense.util.Util;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoReconnect
        extends Module {
    private static ServerData serverData;
    private static AutoReconnect INSTANCE;

    static {
        INSTANCE = new AutoReconnect();
    }

    private final Setting<Integer> delay = this.register( new Setting <> ( "Delay" , 5 ));

    public AutoReconnect() {
        super("AutoReconnect", "Reconnects you if you disconnect.", Module.Category.MISC, true, false, false);
        this.setInstance();
    }

    public static AutoReconnect getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AutoReconnect();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void sendPacket(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiDisconnected) {
            this.updateLastConnectedServer();
            if (AutoLog.getInstance().isOff()) {
                GuiDisconnected disconnected = (GuiDisconnected) event.getGui();
                event.setGui(new GuiDisconnectedHook(disconnected));
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        this.updateLastConnectedServer();
    }

    public void updateLastConnectedServer() {
        ServerData data = Util.mc.getCurrentServerData();
        if (data != null) {
            serverData = data;
        }
    }

    private class GuiDisconnectedHook
            extends GuiDisconnected {
        private final Timer timer;

        public GuiDisconnectedHook(GuiDisconnected disconnected) {
            super(disconnected.parentScreen, disconnected.reason, disconnected.message);
            this.timer = new Timer();
            this.timer.reset();
        }

        public void updateScreen() {
            if (this.timer.passedS( AutoReconnect.this.delay.getValue ( ) )) {
                this.mc.displayGuiScreen(new GuiConnecting(this.parentScreen, this.mc, serverData == null ? this.mc.currentServerData : serverData));
            }
        }

        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            String s = "Reconnecting in " + MathUtil.round((double) ((long) (AutoReconnect.this.delay.getValue() * 1000) - this.timer.getPassedTimeMs()) / 1000.0, 1);
            AutoReconnect.this.renderer.drawString(s, this.width / 2 - AutoReconnect.this.renderer.getStringWidth(s) / 2, this.height - 16, 0xFFFFFF, true);
        }
    }
}

