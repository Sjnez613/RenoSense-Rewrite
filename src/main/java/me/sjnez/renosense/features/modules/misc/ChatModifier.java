package me.sjnez.renosense.features.modules.misc;

import me.sjnez.renosense.event.events.PacketEvent;
import me.sjnez.renosense.features.command.Command;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.modules.client.Managers;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.TextUtil;
import me.sjnez.renosense.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatModifier
        extends Module {
    private static ChatModifier INSTANCE = new ChatModifier();
    private final Timer timer = new Timer();
    public Setting<Suffix> suffix = this.register( new Setting <> ( "Suffix" , Suffix.NONE , "Your Suffix." ));
    public Setting<Boolean> clean = this.register( new Setting <> ( "CleanChat" , Boolean.FALSE , "Cleans your chat" ));
    public Setting<Boolean> infinite = this.register( new Setting <> ( "Infinite" , Boolean.FALSE , "Makes your chat infinite." ));
    public Setting<Boolean> autoQMain = this.register( new Setting <> ( "AutoQMain" , Boolean.FALSE , "Spams AutoQMain" ));
    public Setting<Boolean> qNotification = this.register(new Setting<Object>("QNotification", Boolean.FALSE , v -> this.autoQMain.getValue()));
    public Setting<Integer> qDelay = this.register(new Setting<Object>("QDelay", 9 , 1 , 90 , v -> this.autoQMain.getValue()));
    public Setting<TextUtil.Color> timeStamps = this.register( new Setting <> ( "Time" , TextUtil.Color.NONE ));
    public Setting<Boolean> rainbowTimeStamps = this.register(new Setting<Object>("RainbowTimeStamps", Boolean.FALSE , v -> this.timeStamps.getValue() != TextUtil.Color.NONE));
    public Setting<TextUtil.Color> bracket = this.register(new Setting<Object>("Bracket", TextUtil.Color.WHITE, v -> this.timeStamps.getValue() != TextUtil.Color.NONE));
    public Setting<Boolean> space = this.register(new Setting<Object>("Space", Boolean.TRUE , v -> this.timeStamps.getValue() != TextUtil.Color.NONE));
    public Setting<Boolean> all = this.register(new Setting<Object>("All", Boolean.FALSE , v -> this.timeStamps.getValue() != TextUtil.Color.NONE));
    public Setting<Boolean> shrug = this.register( new Setting <> ( "Shrug" , false ));

    public ChatModifier() {
        super("ChatModifier", "Modifies your chat", Module.Category.MISC, true, false, false);
        this.setInstance();
    }

    public static ChatModifier getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatModifier();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if ( this.shrug.getValue ( ) ) {
            ChatModifier.mc.player.sendChatMessage(TextUtil.shrug);
            this.shrug.setValue(false);
        }
        if ( this.autoQMain.getValue ( ) ) {
            if (!this.shouldSendMessage(ChatModifier.mc.player)) {
                return;
            }
            if ( this.qNotification.getValue ( ) ) {
                Command.sendMessage("<AutoQueueMain> Sending message: /queue main");
            }
            ChatModifier.mc.player.sendChatMessage("/queue main");
            this.timer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketChatMessage) {
            CPacketChatMessage packet = event.getPacket();
            String s = packet.getMessage();
            if (s.startsWith("/") || s.startsWith("!")) {
                return;
            }
            switch (this.suffix.getValue()) {
                case RenoSense: {
                    s = s + " \u23d0 RenoSense";
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
    public void onChatPacketReceive(PacketEvent.Receive event) {
        if (event.getStage() != 0 || event.getPacket() instanceof SPacketChat) {
            // empty if block
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getStage() == 0 && this.timeStamps.getValue() != TextUtil.Color.NONE && event.getPacket() instanceof SPacketChat) {
            if (!((SPacketChat) event.getPacket()).isSystem()) {
                return;
            }
            String originalMessage = ((SPacketChat) event.getPacket()).chatComponent.getFormattedText();
            String message = this.getTimeString(originalMessage) + originalMessage;
            ((SPacketChat) event.getPacket()).chatComponent = new TextComponentString(message);
        }
    }

    public String getTimeString(String message) {
        String date = new SimpleDateFormat("k:mm").format(new Date());
        if ( this.rainbowTimeStamps.getValue ( ) ) {
            String timeString = "<" + date + ">" + ( this.space.getValue ( ) ? " " : "");
            StringBuilder builder = new StringBuilder(timeString);
            builder.insert(0, "\u00a7+");
            if (!message.contains(Managers.getInstance().getRainbowCommandMessage())) {
                builder.append("\u00a7r");
            }
            return builder.toString();
        }
        return (this.bracket.getValue() == TextUtil.Color.NONE ? "" : TextUtil.coloredString("<", this.bracket.getValue())) + TextUtil.coloredString(date, this.timeStamps.getValue()) + (this.bracket.getValue() == TextUtil.Color.NONE ? "" : TextUtil.coloredString(">", this.bracket.getValue())) + ( this.space.getValue ( ) ? " " : "") + "\u00a7r";
    }

    private boolean shouldSendMessage(EntityPlayer player) {
        if (player.dimension != 1) {
            return false;
        }
        if (!this.timer.passedS( this.qDelay.getValue ( ) )) {
            return false;
        }
        return player.getPosition().equals(new Vec3i(0, 240, 0));
    }

    public enum Suffix {
        NONE,
        RenoSense

    }
}

