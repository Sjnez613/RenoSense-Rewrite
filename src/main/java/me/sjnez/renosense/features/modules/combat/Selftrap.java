package me.sjnez.renosense.features.modules.combat;

import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.event.events.UpdateWalkingPlayerEvent;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.modules.player.Freecam;
import me.sjnez.renosense.features.setting.Bind;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.BlockUtil;
import me.sjnez.renosense.util.EntityUtil;
import me.sjnez.renosense.util.InventoryUtil;
import me.sjnez.renosense.util.Timer;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class Selftrap
        extends Module {
    private final Setting<Boolean> smart = this.register( new Setting <> ( "Smart" , false ));
    private final Setting<Double> smartRange = this.register( new Setting <> ( "SmartRange" , 6.0 , 0.0 , 10.0 ));
    private final Setting<Integer> delay = this.register( new Setting <> ( "Delay/Place" , 50 , 0 , 250 ));
    private final Setting<Integer> blocksPerTick = this.register( new Setting <> ( "Block/Place" , 8 , 1 , 20 ));
    private final Setting<Boolean> rotate = this.register( new Setting <> ( "Rotate" , true ));
    private final Setting<Boolean> disable = this.register( new Setting <> ( "Disable" , true ));
    private final Setting<Integer> disableTime = this.register( new Setting <> ( "Ms/Disable" , 200 , 1 , 250 ));
    private final Setting<Boolean> offhand = this.register( new Setting <> ( "OffHand" , true ));
    private final Setting<InventoryUtil.Switch> switchMode = this.register( new Setting <> ( "Switch" , InventoryUtil.Switch.NORMAL ));
    private final Setting<Boolean> onlySafe = this.register(new Setting<Object>("OnlySafe", Boolean.TRUE , v -> this.offhand.getValue()));
    private final Setting<Boolean> highWeb = this.register( new Setting <> ( "HighWeb" , false ));
    private final Setting<Boolean> freecam = this.register( new Setting <> ( "Freecam" , false ));
    private final Setting<Boolean> packet = this.register( new Setting <> ( "Packet" , false ));
    private final me.sjnez.renosense.util.Timer offTimer = new me.sjnez.renosense.util.Timer();
    private final me.sjnez.renosense.util.Timer timer = new me.sjnez.renosense.util.Timer();
    private final Map<BlockPos, Integer> retries = new HashMap <> ( );
    private final me.sjnez.renosense.util.Timer retryTimer = new Timer();
    public Setting<Mode> mode = this.register( new Setting <> ( "Mode" , Mode.OBSIDIAN ));
    public Setting<PlaceMode> placeMode = this.register(new Setting<Object>("PlaceMode", PlaceMode.NORMAL, v -> this.mode.getValue() == Mode.OBSIDIAN));
    public Setting<Bind> obbyBind = this.register( new Setting <> ( "Obsidian" , new Bind ( - 1 ) ));
    public Setting<Bind> webBind = this.register( new Setting <> ( "Webs" , new Bind ( - 1 ) ));
    public Mode currentMode = Mode.OBSIDIAN;
    private boolean accessedViaBind = false;
    private int blocksThisTick = 0;
    private Offhand.Mode offhandMode = Offhand.Mode.CRYSTALS;
    private Offhand.Mode2 offhandMode2 = Offhand.Mode2.CRYSTALS;
    private boolean isSneaking;
    private boolean hasOffhand = false;
    private boolean placeHighWeb = false;
    private int lastHotbarSlot = -1;
    private boolean switchedItem = false;

    public Selftrap() {
        super("Selftrap", "Lure your enemies in!", Module.Category.COMBAT, true, false, true);
    }

    @Override
    public void onEnable() {
        if (Selftrap.fullNullCheck()) {
            this.disable();
        }
        this.lastHotbarSlot = Selftrap.mc.player.inventory.currentItem;
        if (!this.accessedViaBind) {
            this.currentMode = this.mode.getValue();
        }
        Offhand module = RenoSense.moduleManager.getModuleByClass(Offhand.class);
        this.offhandMode = module.mode;
        this.offhandMode2 = module.currentMode;
        if ( this.offhand.getValue ( ) && (EntityUtil.isSafe(Selftrap.mc.player) || ! this.onlySafe.getValue ( ) )) {
            if (module.type.getValue() == Offhand.Type.OLD) {
                if (this.currentMode == Mode.WEBS) {
                    module.setMode(Offhand.Mode2.WEBS);
                } else {
                    module.setMode(Offhand.Mode2.OBSIDIAN);
                }
            } else if (this.currentMode == Mode.WEBS) {
                module.setSwapToTotem(false);
                module.setMode(Offhand.Mode.WEBS);
            } else {
                module.setSwapToTotem(false);
                module.setMode(Offhand.Mode.OBSIDIAN);
            }
        }
        RenoSense.holeManager.update();
        this.offTimer.reset();
    }

    @Override
    public void onTick() {
        if (this.isOn() && (this.blocksPerTick.getValue() != 1 || ! this.rotate.getValue ( ) )) {
            this.doHoleFill();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (this.isOn() && event.getStage() == 0 && this.blocksPerTick.getValue() == 1 && this.rotate.getValue ( ) ) {
            this.doHoleFill();
        }
    }

    @Override
    public void onDisable() {
        if ( this.offhand.getValue ( ) ) {
            RenoSense.moduleManager.getModuleByClass(Offhand.class).setMode(this.offhandMode);
            RenoSense.moduleManager.getModuleByClass(Offhand.class).setMode(this.offhandMode2);
        }
        this.switchItem(true);
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
        this.retries.clear();
        this.accessedViaBind = false;
        this.hasOffhand = false;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            if (this.obbyBind.getValue().getKey() == Keyboard.getEventKey()) {
                this.accessedViaBind = true;
                this.currentMode = Mode.OBSIDIAN;
                this.toggle();
            }
            if (this.webBind.getValue().getKey() == Keyboard.getEventKey()) {
                this.accessedViaBind = true;
                this.currentMode = Mode.WEBS;
                this.toggle();
            }
        }
    }

    private void doHoleFill() {
        if (this.check()) {
            return;
        }
        if (this.placeHighWeb) {
            BlockPos pos = new BlockPos(Selftrap.mc.player.posX, Selftrap.mc.player.posY + 1.0, Selftrap.mc.player.posZ);
            this.placeBlock(pos);
            this.placeHighWeb = false;
        }
        for (BlockPos position : this.getPositions()) {
            if ( this.smart.getValue ( ) && !this.isPlayerInRange()) continue;
            int placeability = BlockUtil.isPositionPlaceable(position, false);
            if (placeability == 1) {
                switch (this.currentMode) {
                    case WEBS: {
                        this.placeBlock(position);
                        break;
                    }
                    case OBSIDIAN: {
                        if (this.switchMode.getValue() != InventoryUtil.Switch.SILENT || this.retries.get(position) != null && this.retries.get(position) >= 4)
                            break;
                        this.placeBlock(position);
                        this.retries.put(position, this.retries.get(position) == null ? 1 : this.retries.get(position) + 1);
                    }
                }
            }
            if (placeability != 3) continue;
            this.placeBlock(position);
        }
    }

    private boolean isPlayerInRange() {
        for (EntityPlayer player : Selftrap.mc.world.playerEntities) {
            if (EntityUtil.isntValid(player, this.smartRange.getValue())) continue;
            return true;
        }
        return false;
    }

    private List<BlockPos> getPositions() {
        ArrayList<BlockPos> positions = new ArrayList <> ( );
        block0:
        switch (this.currentMode) {
            case WEBS: {
                positions.add(new BlockPos(Selftrap.mc.player.posX, Selftrap.mc.player.posY, Selftrap.mc.player.posZ));
                if (! this.highWeb.getValue ( ) ) break;
                positions.add(new BlockPos(Selftrap.mc.player.posX, Selftrap.mc.player.posY + 1.0, Selftrap.mc.player.posZ));
                break;
            }
            case OBSIDIAN: {
                if (this.placeMode.getValue() == PlaceMode.NORMAL) {
                    positions.add(new BlockPos(Selftrap.mc.player.posX, Selftrap.mc.player.posY + 2.0, Selftrap.mc.player.posZ));
                    int placeability = BlockUtil.isPositionPlaceable(positions.get(0), false);
                    switch (placeability) {
                        case 0: {
                            return new ArrayList <> ( );
                        }
                        case 3: {
                            return positions;
                        }
                        case 1: {
                            if (BlockUtil.isPositionPlaceable(positions.get(0), false, false) == 3) {
                                return positions;
                            }
                        }
                        case 2: {
                            positions.add(new BlockPos(Selftrap.mc.player.posX + 1.0, Selftrap.mc.player.posY + 1.0, Selftrap.mc.player.posZ));
                            positions.add(new BlockPos(Selftrap.mc.player.posX + 1.0, Selftrap.mc.player.posY + 2.0, Selftrap.mc.player.posZ));
                            break block0;
                        }
                    }
                    break;
                }
                positions.add(new BlockPos(Selftrap.mc.player.posX, Selftrap.mc.player.posY, Selftrap.mc.player.posZ));
                if (this.placeMode.getValue() == PlaceMode.SELFHIGH) {
                    positions.add(new BlockPos(Selftrap.mc.player.posX, Selftrap.mc.player.posY + 1.0, Selftrap.mc.player.posZ));
                }
                int placeability = BlockUtil.isPositionPlaceable(positions.get(0), false);
                switch (placeability) {
                    case 0: {
                        return new ArrayList <> ( );
                    }
                    case 3: {
                        return positions;
                    }
                    case 1: {
                        if (BlockUtil.isPositionPlaceable(positions.get(0), false, false) == 3) {
                            return positions;
                        }
                    }
                    case 2: {
                        break block0;
                    }
                }
            }
        }
        positions.sort(Comparator.comparingDouble(Vec3i::getY));
        return positions;
    }

    private void placeBlock(BlockPos pos) {
        if (this.blocksThisTick < this.blocksPerTick.getValue() && this.switchItem(false)) {
            boolean smartRotate;
            boolean bl = smartRotate = this.blocksPerTick.getValue() == 1 && this.rotate.getValue ( );
            this.isSneaking = smartRotate ? BlockUtil.placeBlockSmartRotate(pos, this.hasOffhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, true, this.packet.getValue(), this.isSneaking) : BlockUtil.placeBlock(pos, this.hasOffhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), this.isSneaking);
            this.timer.reset();
            ++this.blocksThisTick;
        }
    }

    private boolean check() {
        if (Selftrap.fullNullCheck() || this.disable.getValue ( ) && this.offTimer.passedMs( this.disableTime.getValue ( ) )) {
            this.disable();
            return true;
        }
        if (Selftrap.mc.player.inventory.currentItem != this.lastHotbarSlot && Selftrap.mc.player.inventory.currentItem != InventoryUtil.findHotbarBlock(this.currentMode == Mode.WEBS ? BlockWeb.class : BlockObsidian.class)) {
            this.lastHotbarSlot = Selftrap.mc.player.inventory.currentItem;
        }
        this.switchItem(true);
        if (! this.freecam.getValue ( ) && RenoSense.moduleManager.isModuleEnabled(Freecam.class)) {
            return true;
        }
        this.blocksThisTick = 0;
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
        if (this.retryTimer.passedMs(2000L)) {
            this.retries.clear();
            this.retryTimer.reset();
        }
        int targetSlot = -1;
        switch (this.currentMode) {
            case WEBS: {
                this.hasOffhand = InventoryUtil.isBlock(Selftrap.mc.player.getHeldItemOffhand().getItem(), BlockWeb.class);
                targetSlot = InventoryUtil.findHotbarBlock(BlockWeb.class);
                break;
            }
            case OBSIDIAN: {
                this.hasOffhand = InventoryUtil.isBlock(Selftrap.mc.player.getHeldItemOffhand().getItem(), BlockObsidian.class);
                targetSlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
                break;
            }
        }
        if ( this.onlySafe.getValue ( ) && !EntityUtil.isSafe(Selftrap.mc.player)) {
            this.disable();
            return true;
        }
        if (!this.hasOffhand && targetSlot == -1 && (! this.offhand.getValue ( ) || !EntityUtil.isSafe(Selftrap.mc.player) && this.onlySafe.getValue ( ) )) {
            return true;
        }
        if ( this.offhand.getValue ( ) && !this.hasOffhand) {
            return true;
        }
        return !this.timer.passedMs( this.delay.getValue ( ) );
    }

    private boolean switchItem(boolean back) {
        if ( this.offhand.getValue ( ) ) {
            return true;
        }
        boolean[] value = InventoryUtil.switchItem(back, this.lastHotbarSlot, this.switchedItem, this.switchMode.getValue(), this.currentMode == Mode.WEBS ? BlockWeb.class : BlockObsidian.class);
        this.switchedItem = value[0];
        return value[1];
    }

    public enum PlaceMode {
        NORMAL,
        SELF,
        SELFHIGH

    }

    public enum Mode {
        WEBS,
        OBSIDIAN

    }
}

