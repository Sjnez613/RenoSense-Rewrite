package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;





    public class ViewModel extends Module {

        public final Setting<Integer> translateX = this.register(new Setting<>("TranslateX", 0, -200, 200));
        public final Setting<Integer> translateY = this.register(new Setting<>("TranslateY", 0, -200, 200));
        public final Setting<Integer> translateZ = this.register(new Setting<>("TranslateZ", 0, -200, 200));

        public final Setting<Integer> rotateX = this.register(new Setting<>("RotateX", 0, -200, 200));
        public final Setting<Integer> rotateY = this.register(new Setting<>("RotateY", 0, -200, 200));
        public final Setting<Integer> rotateZ = this.register(new Setting<>("RotateZ", 0, -200, 200));

        public final Setting<Integer> scaleX = this.register(new Setting<>("ScaleX", 100, 0, 200));
        public final Setting<Integer> scaleY = this.register(new Setting<>("ScaleY", 100, 0, 200));
        public final Setting<Integer> scaleZ = this.register(new Setting<>("ScaleZ", 100, 0, 200));



        public static ViewModel INSTANCE;
        public ViewModel() {
            super("ViewModel", "Cool", Category.RENDER, true, false, false);
        }
        {
            INSTANCE = this;
        }


    }