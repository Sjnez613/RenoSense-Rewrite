package me.sjnez.renosense.features.modules.misc;

import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.FileUtil;
import me.sjnez.renosense.util.Timer;
import me.sjnez.renosense.util.Util;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Spammer
        extends Module {
    private static final String fileName = "phobos/util/Spammer.txt";
    private static final String defaultMessage = "gg";
    private static final List<String> spamMessages = new ArrayList <> ( );
    private static final Random rnd = new Random();
    private final Timer timer = new Timer();
    private final List<String> sendPlayers = new ArrayList <> ( );
    public Setting<Mode> mode = this.register( new Setting <> ( "Mode" , Mode.PWORD ));
    public Setting<PwordMode> type = this.register(new Setting<Object>("Pword", PwordMode.CHAT, v -> this.mode.getValue() == Mode.PWORD));
    public Setting<DelayType> delayType = this.register( new Setting <> ( "DelayType" , DelayType.S ));
    public Setting<Integer> delay = this.register(new Setting<Object>("DelayS", 10 , 1 , 1000 , v -> this.delayType.getValue() == DelayType.S));
    public Setting<Integer> delayDS = this.register(new Setting<Object>("DelayDS", 10 , 1 , 500 , v -> this.delayType.getValue() == DelayType.DS));
    public Setting<Integer> delayMS = this.register(new Setting<Object>("DelayDS", 10 , 1 , 1000 , v -> this.delayType.getValue() == DelayType.MS));
    public Setting<String> msgTarget = this.register(new Setting<Object>("MsgTarget", "Target...", v -> this.mode.getValue() == Mode.PWORD && this.type.getValue() == PwordMode.MSG));
    public Setting<Boolean> greentext = this.register(new Setting<Object>("Greentext", Boolean.FALSE , v -> this.mode.getValue() == Mode.FILE));
    public Setting<Boolean> random = this.register(new Setting<Object>("Random", Boolean.FALSE , v -> this.mode.getValue() == Mode.FILE));
    public Setting<Boolean> loadFile = this.register(new Setting<Object>("LoadFile", Boolean.FALSE , v -> this.mode.getValue() == Mode.FILE));

    public Spammer() {
        super("Spammer", "Spams stuff.", Module.Category.MISC, true, false, false);
    }

    @Override
    public void onLoad() {
        this.readSpamFile();
        this.disable();
    }

    @Override
    public void onEnable() {
        if (Spammer.fullNullCheck()) {
            this.disable();
            return;
        }
        this.readSpamFile();
    }

    @Override
    public void onLogin() {
        this.disable();
    }

    @Override
    public void onLogout() {
        this.disable();
    }

    @Override
    public void onDisable() {
        spamMessages.clear();
        this.timer.reset();
    }

    @Override
    public void onUpdate() {
        if (Spammer.fullNullCheck()) {
            this.disable();
            return;
        }
        if ( this.loadFile.getValue ( ) ) {
            this.readSpamFile();
            this.loadFile.setValue(false);
        }
        switch (this.delayType.getValue()) {
            case MS: {
                if (this.timer.passedMs( this.delayMS.getValue ( ) )) break;
                return;
            }
            case S: {
                if (this.timer.passedS( this.delay.getValue ( ) )) break;
                return;
            }
            case DS: {
                if (this.timer.passedDs( this.delayDS.getValue ( ) )) break;
                return;
            }
        }
        if (this.mode.getValue() == Mode.PWORD) {
            String msg = "  \u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\n \u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\n \u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2592\n \u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\n \u2588\u2592\u2592\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2592\u2592\u2588\n \u2588\u2592\u2592\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\n \u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592";
            switch (this.type.getValue()) {
                case MSG: {
                    msg = "/msg " + this.msgTarget.getValue() + msg;
                    break;
                }
                case EVERYONE: {
                    String target = null;
                    if ( Util.mc.getConnection ( ) != null ) {
                        Util.mc.getConnection ( ).getPlayerInfoMap ( );
                        for (NetworkPlayerInfo info : Util.mc.getConnection ( ).getPlayerInfoMap ( )) {
                            if ( info == null || info.getDisplayName ( ) == null ) continue;
                            try {
                                String str = info.getDisplayName ( ).getFormattedText ( );
                                String name = StringUtils.stripControlCodes ( str );
                                if ( name.equals ( Spammer.mc.player.getName ( ) ) || this.sendPlayers.contains ( name ) )
                                    continue;
                                target = name;
                                this.sendPlayers.add ( name );
                                break;
                            } catch ( Exception ignored ) {
                            }
                        }
                        if ( target == null ) {
                            this.sendPlayers.clear ( );
                            return;
                        }
                        msg = "/msg " + target + msg;
                        break;
                    }
                    return;
                }
            }
            Spammer.mc.player.sendChatMessage(msg);
        } else if (spamMessages.size() > 0) {
            String messageOut;
            if ( this.random.getValue ( ) ) {
                int index = rnd.nextInt(spamMessages.size());
                messageOut = spamMessages.get(index);
                spamMessages.remove(index);
            } else {
                messageOut = spamMessages.get(0);
                spamMessages.remove(0);
            }
            spamMessages.add(messageOut);
            if ( this.greentext.getValue ( ) ) {
                messageOut = "> " + messageOut;
            }
            Spammer.mc.player.connection.sendPacket(new CPacketChatMessage(messageOut.replaceAll("\u00a7", "")));
        }
        this.timer.reset();
    }

    private void readSpamFile() {
        List<String> fileInput = FileUtil.readTextFileAllLines(fileName);
        Iterator<String> i = fileInput.iterator();
        spamMessages.clear();
        while (i.hasNext()) {
            String s = i.next();
            if (s.replaceAll("\\s", "").isEmpty()) continue;
            spamMessages.add(s);
        }
        if (spamMessages.size() == 0) {
            spamMessages.add(defaultMessage);
        }
    }

    public enum DelayType {
        MS,
        DS,
        S

    }

    public enum PwordMode {
        MSG,
        EVERYONE,
        CHAT

    }

    public enum Mode {
        FILE,
        PWORD

    }
}

