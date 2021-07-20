package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.event.events.BlockEvent;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BlockUtil;
import me.alpha432.oyvey.util.MathUtil;
import me.alpha432.oyvey.util.RenderUtil;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class PacketMine extends Module {
    public Setting<Boolean> tweaks;
    public Setting<Boolean> reset;
    public Setting<Float> range;

    public Setting<Boolean> silent;
    public Setting<Boolean> noBreakAnim;
    public Setting<Boolean> noDelay;
    public Setting<Boolean> noSwing;
    public Setting<Boolean> allow;
    public Setting<Boolean> doubleBreak;
    public Setting<Boolean> render;
    public Setting<Integer> red;
    public Setting<Integer> green;
    public Setting<Integer> blue;
    public Setting<Boolean> box;
    public Setting<Boolean> outline;
    public final Setting<Float> lineWidth;
    public final Setting<Integer> boxAlpha;
    private static PacketMine INSTANCE;
    public BlockPos currentPos;
    public IBlockState currentBlockState;
    public float breakTime;
    public final Timer timer;
    private boolean isMining;
    private BlockPos lastPos;
    private EnumFacing lastFacing;
    private boolean shouldSwitch;

    public PacketMine() {
        super("PacketMine", "Speeds up mining.", Category.PLAYER, true, false, false);
        this.tweaks = (Setting<Boolean>) this.register(new Setting("Tweaks", true));
        this.reset = (Setting<Boolean>) this.register(new Setting("Reset", true));
        this.range = (Setting<Float>) this.register(new Setting("Range", 10.0f, 0.0f, 50.0f));
        this.silent = (Setting<Boolean>) this.register(new Setting("Silent", true));
        this.noBreakAnim = (Setting<Boolean>) this.register(new Setting("NoBreakAnim", false));
        this.noDelay = (Setting<Boolean>) this.register(new Setting("NoDelay", false));
        this.noSwing = (Setting<Boolean>) this.register(new Setting("NoSwing", false));
        this.allow = (Setting<Boolean>) this.register(new Setting("AllowMultiTask", false));
        this.doubleBreak = (Setting<Boolean>) this.register(new Setting("DoubleBreak", false));
        this.render = (Setting<Boolean>) this.register(new Setting("Render", false));
        this.red = (Setting<Integer>) this.register(new Setting("Red", 125, 0, 255, v -> this.render.getValue()));
        this.green = (Setting<Integer>) this.register(new Setting("Green", 105, 0, 255, v -> this.render.getValue()));
        this.blue = (Setting<Integer>) this.register(new Setting("Blue", 255, 0, 255, v -> this.render.getValue()));
        this.box = (Setting<Boolean>) this.register(new Setting("Box", false, v -> this.render.getValue()));
        this.outline = (Setting<Boolean>) this.register(new Setting("Outline", true, v -> this.render.getValue()));
        this.lineWidth = (Setting<Float>) this.register(new Setting("LineWidth", 1.0f, 0.1f, 5.0f, v -> this.outline.getValue() && this.render.getValue()));
        this.boxAlpha = (Setting<Integer>) this.register(new Setting("BoxAlpha", 85, 0, 255, v -> this.box.getValue() && this.render.getValue()));
        this.breakTime = -1.0f;
        this.timer = new Timer();
        this.isMining = false;
        this.lastPos = null;
        this.lastFacing = null;
        this.shouldSwitch = false;

        this.setInstance();
    }

    private void setInstance() {
        PacketMine.INSTANCE = this;
    }

    public static PacketMine getInstance() {
        if (PacketMine.INSTANCE == null) {
            PacketMine.INSTANCE = new PacketMine();
        }
        return PacketMine.INSTANCE;
    }

    @Override
    public void onEnable() {
        shouldSwitch = false;
    }

    @Override
    public void onTick() {
        if (fullNullCheck()) {
            return;
        }
        if (this.currentPos != null) {
            if (mc.player != null && mc.player.getDistanceSq(this.currentPos) > MathUtil.square(this.range.getValue())) {
                this.currentPos = null;
                this.currentBlockState = null;
                PacketMine.mc.playerController.isHittingBlock = false;
                return;
            }
        }

        this.onMine();
    }

    public void onMine() {
        if (this.currentPos != null && (!PacketMine.mc.world.getBlockState(this.currentPos).equals(this.currentBlockState) || PacketMine.mc.world.getBlockState(this.currentPos).getBlock() == Blocks.AIR)) {
            this.currentPos = null;
            this.currentBlockState = null;
            this.shouldSwitch = true;
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (this.noDelay.getValue()) {
            PacketMine.mc.playerController.blockHitDelay = 0;
        }
        if (this.isMining && this.lastPos != null && this.lastFacing != null && this.noBreakAnim.getValue()) {
            PacketMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.lastPos, this.lastFacing));
        }
        if (this.reset.getValue() && PacketMine.mc.gameSettings.keyBindUseItem.isKeyDown() && !this.allow.getValue()) {
            PacketMine.mc.playerController.isHittingBlock = false;
        }
    }

    @Override
    public void onRender3D(final Render3DEvent event) {
        if (this.render.getValue() && this.currentPos != null) {
            final Color color = new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.boxAlpha.getValue());
            RenderUtil.gradientBox(this.currentPos, color, this.lineWidth.getValue(), this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), true);
        }
    }


    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        if (fullNullCheck()) {
            return;
        }


        if (event.getStage() == 0) {
            if (this.noSwing.getValue() && event.getPacket() instanceof CPacketAnimation) {
                event.setCanceled(true);
            }
            if (this.noBreakAnim.getValue() && event.getPacket() instanceof CPacketPlayerDigging) {
                final CPacketPlayerDigging packet = event.getPacket();
                if (packet != null && packet.getPosition() != null) {
                    try {
                        for (final Entity entity : PacketMine.mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(packet.getPosition()))) {
                            if (entity instanceof EntityEnderCrystal) {
                                this.showAnimation();
                                return;
                            }
                        }
                    } catch (Exception ex) {
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

    }

    @SubscribeEvent
    public void onBlockEvent(final BlockEvent event) {
        if (fullNullCheck()) {
            return;
        }
        if (event.getStage() == 3 && this.reset.getValue() && PacketMine.mc.playerController.curBlockDamageMP > 0.1f) {
            PacketMine.mc.playerController.isHittingBlock = true;
        }
        if (event.getStage() == 4 && this.tweaks.getValue()) {


            if (BlockUtil.canBreak(event.pos)) {
                if (this.currentPos == null) {
                    this.currentPos = event.pos;
                    this.currentBlockState = PacketMine.mc.world.getBlockState(this.currentPos);
                    final ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
                    this.breakTime = pick.getDestroySpeed(this.currentBlockState) / 3.71f;
                    this.timer.reset();
                }
                PacketMine.mc.player.swingArm(EnumHand.MAIN_HAND);
                PacketMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.pos, event.facing));
                PacketMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.pos, event.facing));
                event.setCanceled(true);


            }
            if (this.doubleBreak.getValue()) {
                final BlockPos above = event.pos.add(0, 1, 0);
                if (BlockUtil.canBreak(above) && PacketMine.mc.player.getDistance(above.getX(), above.getY(), above.getZ()) <= 5.0) {
                    PacketMine.mc.player.swingArm(EnumHand.MAIN_HAND);
                    PacketMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, above, event.facing));
                    PacketMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, above, event.facing));
                    PacketMine.mc.playerController.onPlayerDestroyBlock(above);
                    PacketMine.mc.world.setBlockToAir(above);
                }
            }
        }
    }

    private void showAnimation(final boolean isMining, final BlockPos lastPos, final EnumFacing lastFacing) {
        this.isMining = isMining;
        this.lastPos = lastPos;
        this.lastFacing = lastFacing;
    }

    public void showAnimation() {
        this.showAnimation(false, null, null);
    }

    static {
        PacketMine.INSTANCE = new PacketMine();
    }
}
