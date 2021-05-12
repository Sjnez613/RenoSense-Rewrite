package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.Timer;

public class TimerSpeed extends Module {

        public Setting<Boolean> autoOff = register(new Setting("AutoOff", false));
        public Setting<Integer> timeLimit = register(new Setting("Limit", 250, 1, 2500, v -> autoOff.getValue()));
        public Setting<TimerMode> mode = register(new Setting("Mode", TimerMode.NORMAL));
        public Setting<Float> timerSpeed = register(new Setting("Speed", 4.0f, 0.1f, 50.0f));
        public Setting<Float> fastSpeed = register(new Setting("Fast", 10.0f, 0.1f, 100.0f, v -> mode.getValue() == TimerMode.SWITCH, "Fast Speed for switch."));
        public Setting<Integer> fastTime = register(new Setting("FastTime", 20, 1, 500, v -> mode.getValue() == TimerMode.SWITCH, "How long you want to go fast.(ms * 10)"));
        public Setting<Integer> slowTime = register(new Setting("SlowTime", 20, 1, 500, v -> mode.getValue() == TimerMode.SWITCH, "Recover from too fast.(ms * 10)"));
        public Setting<Boolean> startFast = register(new Setting("StartFast", false, v -> mode.getValue() == TimerMode.SWITCH));

        public float speed = 1.0f;
        public final Timer timer = new Timer();
        public final Timer turnOffTimer = new Timer();
        private boolean fast = false;

        public TimerSpeed() {
            super("Timer", "Will speed up the game.", Category.PLAYER, false, false, false);
        }

        @Override
        public void onEnable() {
            turnOffTimer.reset();
            this.speed = timerSpeed.getValue();
            if (!startFast.getValue()) {
                timer.reset();
            }
        }

        @Override
        public void onUpdate() {
            if (autoOff.getValue() && turnOffTimer.passedMs(timeLimit.getValue())) {
                this.disable();
                return;
            }

            if (mode.getValue() == TimerMode.NORMAL) {
                this.speed = timerSpeed.getValue();
                return;
            }

            if (!fast && timer.passedDms(slowTime.getValue())) {
                fast = true;
                this.speed = fastSpeed.getValue();
                timer.reset();
            }

            if (fast && timer.passedDms(fastTime.getValue())) {
                fast = false;
                this.speed = timerSpeed.getValue();
                timer.reset();
            }
        }

        @Override
        public void onDisable() {
            this.speed = 1.0f;
            OyVey.timerManager.reset();
            fast = false;
        }

        @Override
        public String getDisplayInfo() {
            return timerSpeed.getValueAsString();
        }

        public enum TimerMode {
            NORMAL,
            SWITCH
        }
    }

