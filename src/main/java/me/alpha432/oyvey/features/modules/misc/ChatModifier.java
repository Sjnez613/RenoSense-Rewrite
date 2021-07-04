package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.HUD;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.TextUtil;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatModifier
        extends Module {
    public Setting<Suffix> suffix = this.register(new Setting<Suffix>("Suffix", Suffix.NONE, "Your Suffix."));
    public Setting<Boolean> clean = this.register(new Setting<Boolean>("CleanChat", Boolean.valueOf(false), "Cleans your chat"));
    public Setting<Boolean> infinite = this.register(new Setting<Boolean>("Infinite", Boolean.valueOf(false), "Makes your chat infinite."));
    public Setting<Boolean> autoQMain = this.register(new Setting<Boolean>("AutoQMain", Boolean.valueOf(false), "Spams AutoQMain"));
    public Setting<Boolean> qNotification = this.register(new Setting<Object>("QNotification", Boolean.valueOf(false), v -> this.autoQMain.getValue()));
    public Setting<Integer> qDelay = this.register(new Setting<Object>("QDelay", Integer.valueOf(9), Integer.valueOf(1), Integer.valueOf(90), v -> this.autoQMain.getValue()));
    public Setting<TextUtil.Color> timeStamps = this.register(new Setting<TextUtil.Color>("Time", TextUtil.Color.NONE));
    public Setting<Boolean> rainbowTimeStamps = this.register(new Setting<Object>("RainbowTimeStamps", Boolean.valueOf(false), v -> this.timeStamps.getValue() != TextUtil.Color.NONE));
    public Setting<TextUtil.Color> bracket = this.register(new Setting<Object>("Bracket", (Object)TextUtil.Color.WHITE, v -> this.timeStamps.getValue() != TextUtil.Color.NONE));
    public Setting<Boolean> space = this.register(new Setting<Object>("Space", Boolean.valueOf(true), v -> this.timeStamps.getValue() != TextUtil.Color.NONE));
    public Setting<Boolean> all = this.register(new Setting<Object>("All", Boolean.valueOf(false), v -> this.timeStamps.getValue() != TextUtil.Color.NONE));
    private final Timer timer = new Timer();
    private static ChatModifier INSTANCE = new ChatModifier();

    public ChatModifier() {
        super("Chat", "Modifies your chat", Module.Category.MISC, true, false, false);
        this.setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static ChatModifier getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatModifier();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {

        if (this.autoQMain.getValue().booleanValue()) {
            if (!this.shouldSendMessage((EntityPlayer)ChatModifier.mc.player)) {
                return;
            }
            if (this.qNotification.getValue().booleanValue()) {
                Command.sendMessage("<AutoQueueMain> Sending message: /queue main");
            }
            ChatModifier.mc.player.sendChatMessage("/queue main");
            this.timer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketChatMessage) {
            CPacketChatMessage packet = (CPacketChatMessage)event.getPacket();
            String s = packet.getMessage();
            if (s.startsWith("/")) {
                return;
            }
            switch (this.suffix.getValue()) {
                case RENOSENSE: {
                    s = s + " ||| RENOSENSE";
                    break;
                }
            }
            if (s.length() >= 256) {
                s = s.substring(0, 256);
            }
            packet.message = s;
        }
    }

@SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketChat) {
            if (((SPacketChat) event.getPacket()).getType() == ChatType.GAME_INFO) {
                return;
            }
            String originalMessage = ((SPacketChat) event.getPacket()).chatComponent.getFormattedText();
            String message = this.getTimeString(originalMessage);
            ((SPacketChat) event.getPacket()).chatComponent = new TextComponentString(message + originalMessage);
        }
    }

    public String getTimeString(String message) {
        String date = new SimpleDateFormat("k:mm").format(new Date());
        if (this.rainbowTimeStamps.getValue().booleanValue()) {
            String timeString = "<" + date + ">" + (this.space.getValue() != false ? " " : "");
            StringBuilder builder = new StringBuilder(timeString);
            builder.insert(0, "\u00a7+");
            if (!message.contains(HUD.getInstance().getRainbowCommandMessage())) {
                builder.append("\u00a7r");
            }
            return builder.toString();
        }
        return (this.bracket.getValue() == TextUtil.Color.NONE ? "" : TextUtil.coloredString("<", this.bracket.getValue())) + TextUtil.coloredString(date, this.timeStamps.getValue()) + (this.bracket.getValue() == TextUtil.Color.NONE ? "" : TextUtil.coloredString(">", this.bracket.getValue())) + (this.space.getValue() != false ? " " : "") + "\u00a7r";
    }



    private boolean shouldSendMessage(EntityPlayer player) {
        if (player.dimension != 1) {
            return false;
        }
        if (!this.timer.passedS(this.qDelay.getValue().intValue())) {
            return false;
        }
        return player.getPosition().equals((Object)new Vec3i(0, 240, 0));
    }

    public static enum Suffix {
        NONE,
        RENOSENSE

    }
}