package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class QueueSkip extends Module {

    private final Setting<Integer> packets = this.register(new Setting<Integer>("Packets", 0, 0, 4));

    public Setting<Server> server = this.register(new Setting<Server>("Server", Server.NORMAL));

    public Setting<Mode> mode = this.register(new Setting<Object>("Mode", Mode.NORMAL, v -> this.server.getValue() == Server.NORMAL));
    public Setting<Float> prioCount = this.register(new Setting<Object>("PrioPlayerCount", 0f, 0f, 1000f, v -> this.mode.getValue() == Mode.PRIO));
    public Setting<Float> playerCount = this.register(new Setting<Object>("PlayerCount", 0f, 0f, 1000f, v -> this.mode.getValue() == Mode.NORMAL));



    public QueueSkip() {
        super("QueueSkip", "Skips the QUEUE!", Category.MISC, true, false, true);
    }
    @Override
    public void onUpdate() {
    }
    public enum Mode {
        PRIO,
        NORMAL
    }
    public enum Server {
        NORMAL,
        OLDFAG
    }

}
