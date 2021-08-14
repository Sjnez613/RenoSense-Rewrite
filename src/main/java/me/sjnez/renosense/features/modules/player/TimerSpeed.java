package me.sjnez.renosense.features.modules.player;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.Timer;

public class TimerSpeed
        extends Module {
    public Setting<Boolean> autoOff = this.register( new Setting <> ( "AutoOff" , false ));
    public Setting<Integer> timeLimit = this.register(new Setting<Object>("Limit", 250 , 1 , 2500 , v -> this.autoOff.getValue()));
    public Setting<TimerMode> mode = this.register( new Setting <> ( "Mode" , TimerMode.NORMAL ));
    public Setting<Float> timerSpeed = this.register( new Setting <> ( "Speed" , 4.0f , 0.1f , 20.0f ));
    public Setting<Float> fastSpeed = this.register(new Setting<Object>("Fast", 10.0f , 0.1f , 100.0f , v -> this.mode.getValue() == TimerMode.SWITCH, "Fast Speed for switch."));
    public Setting<Integer> fastTime = this.register(new Setting<Object>("FastTime", 20, 1, 500, v -> this.mode.getValue() == TimerMode.SWITCH, "How long you want to go fast.(ms * 10)"));
    public Setting<Integer> slowTime = this.register(new Setting<Object>("SlowTime", 20, 1, 500, v -> this.mode.getValue() == TimerMode.SWITCH, "Recover from too fast.(ms * 10)"));
    public Setting<Boolean> startFast = this.register(new Setting<Object>("StartFast", Boolean.FALSE , v -> this.mode.getValue() == TimerMode.SWITCH));
    public float speed = 1.0f;
    private final Timer timer = new Timer();
    private final Timer turnOffTimer = new Timer();
    private boolean fast = false;

    public TimerSpeed() {
        super("Timer", "Will speed up the game.", Module.Category.PLAYER, false, false, false);
    }

    @Override
    public void onEnable() {
        this.turnOffTimer.reset();
        this.speed = this.timerSpeed.getValue ( );
        if (! this.startFast.getValue ( ) ) {
            this.timer.reset();
        }
    }

    @Override
    public void onUpdate() {
        if ( this.autoOff.getValue ( ) && this.turnOffTimer.passedMs( this.timeLimit.getValue ( ) )) {
            this.disable();
            return;
        }
        if (this.mode.getValue() == TimerMode.NORMAL) {
            this.speed = this.timerSpeed.getValue ( );
            return;
        }
        if (!this.fast && this.timer.passedDms( this.slowTime.getValue ( ) )) {
            this.fast = true;
            this.speed = this.fastSpeed.getValue ( );
            this.timer.reset();
        }
        if (this.fast && this.timer.passedDms( this.fastTime.getValue ( ) )) {
            this.fast = false;
            this.speed = this.timerSpeed.getValue ( );
            this.timer.reset();
        }
    }

    @Override
    public void onDisable() {
        this.speed = 1.0f;
        RenoSense.timerManager.reset();
        this.fast = false;
    }

    @Override
    public String getDisplayInfo() {
        return this.timerSpeed.getValueAsString();
    }

    public enum TimerMode {
        NORMAL,
        SWITCH

    }
}

