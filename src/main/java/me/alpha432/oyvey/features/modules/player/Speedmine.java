package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.BlockEvent;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.*;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class Speedmine
        extends Module {
    private static Speedmine INSTANCE = new Speedmine();
    private final Setting<Float> range = this.register(new Setting<Float>("Range", Float.valueOf(10.0f), Float.valueOf(0.0f), Float.valueOf(50.0f)));
    public final Timer timer;
    public Setting<Boolean> tweaks = this.register(new Setting<Boolean>("Tweaks", true));
    public Setting<Mode> mode = this.register(new Setting<Object>("Mode", Mode.PACKET, v -> this.tweaks.getValue()));
    public Setting<Boolean> reset = this.register(new Setting<Boolean>("Reset", true));
    public Setting<Float> damage = this.register(new Setting<Object>("Damage", Float.valueOf(0.7f), Float.valueOf(0.0f), Float.valueOf(1.0f), v -> this.mode.getValue() == Mode.DAMAGE && this.tweaks.getValue() != false));
    public Setting<Boolean> noBreakAnim = this.register(new Setting<Boolean>("NoBreakAnim", false));
    public Setting<Boolean> noDelay = this.register(new Setting<Boolean>("NoDelay", false));
    public Setting<Boolean> noSwing = this.register(new Setting<Boolean>("NoSwing", false));
    public Setting<Boolean> allow = this.register(new Setting<Boolean>("AllowMultiTask", false));
    public Setting<Boolean> doubleBreak = this.register(new Setting<Boolean>("DoubleBreak", false));
    public Setting<Boolean> webSwitch = this.register(new Setting<Boolean>("WebSwitch", false));
    public Setting<Boolean> silentSwitch = this.register(new Setting<Boolean>("SilentSwitch", false));
    public BlockPos currentPos;
    public IBlockState currentBlockState;
    private boolean isMining = false;
    private BlockPos lastPos = null;
    private EnumFacing lastFacing = null;

    public float breakTime;

    public Setting<Boolean> render;
    public Setting<Integer> red;
    public Setting<Integer> green;
    public Setting<Integer> blue;
    public Setting<Boolean> box;
    public Setting<Boolean> outline;
    public final Setting<Float> lineWidth;
    public final Setting<Integer> boxAlpha;

    private int oldSlot;
    private boolean shouldSwap;

    public Speedmine() {
        super("Speedmine", "Speeds up mining.", Module.Category.PLAYER, true, false, false);
        this.setInstance();
        this.timer = new Timer();
        this.render = (Setting<Boolean>) this.register(new Setting("Render", false));
        this.red = (Setting<Integer>) this.register(new Setting("Red", 125, 0, 255, v -> this.render.getValue()));
        this.green = (Setting<Integer>) this.register(new Setting("Green", 105, 0, 255, v -> this.render.getValue()));
        this.blue = (Setting<Integer>) this.register(new Setting("Blue", 255, 0, 255, v -> this.render.getValue()));
        this.box = (Setting<Boolean>) this.register(new Setting("Box", false, v -> this.render.getValue()));
        this.outline = (Setting<Boolean>) this.register(new Setting("Outline", true, v -> this.render.getValue()));
        this.lineWidth = (Setting<Float>) this.register(new Setting("LineWidth", 1.0f, 0.1f, 5.0f, v -> this.outline.getValue() && this.render.getValue()));
        this.boxAlpha = (Setting<Integer>) this.register(new Setting("BoxAlpha", 85, 0, 255, v -> this.box.getValue() && this.render.getValue()));
    }

    public static Speedmine getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Speedmine();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onEnable(){
        oldSlot = -1;
        shouldSwap = false;
    }

    @Override
    public void onTick() {
        if(nullCheck())return;
        if (this.currentPos != null) {
            if (Speedmine.mc.player.getDistanceSq(this.currentPos) > MathUtil.square(this.range.getValue().floatValue())) {
                this.currentPos = null;
                this.currentBlockState = null;
                return;
            }
            int slot = this.getPickSlot();

            if(this.silentSwitch.getValue().booleanValue() && shouldSwap){
                shouldSwap = false;
                InventoryUtil.mc.player.connection.sendPacket((Packet)new CPacketHeldItemChange(oldSlot));
                InventoryUtil.mc.playerController.updateController();
            }

            if (this.silentSwitch.getValue().booleanValue() && this.timer.passedMs((int) (2000.0f * OyVey.serverManager.getTpsFactor())) && slot != -1) {
                oldSlot = mc.playerController.currentPlayerItem;
                InventoryUtil.mc.player.connection.sendPacket((Packet)new CPacketHeldItemChange(slot));
                InventoryUtil.mc.playerController.updateController();
                shouldSwap = true;
            }
            if (!Speedmine.mc.world.getBlockState(this.currentPos).equals(this.currentBlockState) || Speedmine.mc.world.getBlockState(this.currentPos).getBlock() == Blocks.AIR) {
                this.currentPos = null;
                this.currentBlockState = null;
            } else if (this.webSwitch.getValue().booleanValue() && this.currentBlockState.getBlock() == Blocks.WEB && Speedmine.mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
                InventoryUtil.switchToHotbarSlot(ItemSword.class, false);
            }
        }
    }

    @Override
    public void onUpdate() {
        if (Speedmine.fullNullCheck()) {
            return;
        }
        if (this.noDelay.getValue().booleanValue()) {
            Speedmine.mc.playerController.blockHitDelay = 0;
        }
        if (this.isMining && this.lastPos != null && this.lastFacing != null && this.noBreakAnim.getValue().booleanValue()) {
            Speedmine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.lastPos, this.lastFacing));
        }
        if (this.reset.getValue().booleanValue() && Speedmine.mc.gameSettings.keyBindUseItem.isKeyDown() && !this.allow.getValue().booleanValue()) {
            Speedmine.mc.playerController.isHittingBlock = false;
        }
    }

    @Override
    public void onRender3D(final Render3DEvent event) {
        if (this.render.getValue() && this.currentPos != null) {
            Color color = new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.boxAlpha.getValue());
            RenderUtil.gradientBox(this.currentPos, color, this.lineWidth.getValue(), this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), true);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (Speedmine.fullNullCheck()) {
            return;
        }
        if (event.getStage() == 0) {
            CPacketPlayerDigging packet;
            if (this.noSwing.getValue().booleanValue() && event.getPacket() instanceof CPacketAnimation) {
                event.setCanceled(true);
            }
            if (this.noBreakAnim.getValue().booleanValue() && event.getPacket() instanceof CPacketPlayerDigging && (packet = event.getPacket()) != null && packet.getPosition() != null) {
                try {
                    for (Entity entity : Speedmine.mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(packet.getPosition()))) {
                        if (!(entity instanceof EntityEnderCrystal)) continue;
                        this.showAnimation();
                        return;
                    }
                } catch (Exception exception) {
                    // empty catch block
                }
                if (packet.getAction().equals(CPacketPlayerDigging.Action.START_DESTROY_BLOCK)) {
                    this.showAnimation(true, packet.getPosition(), packet.getFacing());
                }
                if (packet.getAction().equals(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK)) {
                    this.showAnimation();
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockEvent(BlockEvent event) {
        if (Speedmine.fullNullCheck()) {
            return;
        }
        if (event.getStage() == 3 && Speedmine.mc.world.getBlockState(event.pos).getBlock() instanceof BlockEndPortalFrame) {
            Speedmine.mc.world.getBlockState(event.pos).getBlock().setHardness(50.0f);
        }
        if (event.getStage() == 3 && this.reset.getValue().booleanValue() && Speedmine.mc.playerController.curBlockDamageMP > 0.1f) {
            Speedmine.mc.playerController.isHittingBlock = true;
        }
        if (event.getStage() == 4 && this.tweaks.getValue().booleanValue()) {
            BlockPos above;
            if (BlockUtil.canBreak(event.pos)) {
                if (this.reset.getValue().booleanValue()) {
                    Speedmine.mc.playerController.isHittingBlock = false;
                }
                switch (this.mode.getValue()) {
                    case PACKET: {
                        if (this.currentPos == null) {
                            this.currentPos = event.pos;
                            this.currentBlockState = Speedmine.mc.world.getBlockState(this.currentPos);
                            this.timer.reset();
                        }
                        Speedmine.mc.player.swingArm(EnumHand.MAIN_HAND);
                        Speedmine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.pos, event.facing));
                        Speedmine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.pos, event.facing));
                        event.setCanceled(true);
                        break;
                    }
                    case DAMAGE: {
                        if (!(Speedmine.mc.playerController.curBlockDamageMP >= this.damage.getValue().floatValue()))
                            break;
                        Speedmine.mc.playerController.curBlockDamageMP = 1.0f;
                        break;
                    }
                    case INSTANT: {
                        Speedmine.mc.player.swingArm(EnumHand.MAIN_HAND);
                        Speedmine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.pos, event.facing));
                        Speedmine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.pos, event.facing));
                        Speedmine.mc.playerController.onPlayerDestroyBlock(event.pos);
                        Speedmine.mc.world.setBlockToAir(event.pos);
                    }
                }
            }
            if (this.doubleBreak.getValue().booleanValue() && BlockUtil.canBreak(above = event.pos.add(0, 1, 0)) && Speedmine.mc.player.getDistance(above.getX(), above.getY(), above.getZ()) <= 5.0) {
                Speedmine.mc.player.swingArm(EnumHand.MAIN_HAND);
                Speedmine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, above, event.facing));
                Speedmine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, above, event.facing));
                Speedmine.mc.playerController.onPlayerDestroyBlock(above);
                Speedmine.mc.world.setBlockToAir(above);
            }
        }
    }

    private int getPickSlot() {
        for (int i = 0; i < 9; ++i) {
            if (Speedmine.mc.player.inventory.getStackInSlot(i).getItem() != Items.DIAMOND_PICKAXE) continue;
            return i;
        }
        return -1;
    }

    private void showAnimation(boolean isMining, BlockPos lastPos, EnumFacing lastFacing) {
        this.isMining = isMining;
        this.lastPos = lastPos;
        this.lastFacing = lastFacing;
    }

    public void showAnimation() {
        this.showAnimation(false, null, null);
    }

    @Override
    public String getDisplayInfo() {
        return this.mode.currentEnumName();
    }

    public enum Mode {
        PACKET,
        DAMAGE,
        INSTANT

    }
}