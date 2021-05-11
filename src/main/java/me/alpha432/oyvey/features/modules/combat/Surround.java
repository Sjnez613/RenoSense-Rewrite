package me.alpha432.oyvey.features.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BlockUtil;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.InventoryUtil;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class Surround
        extends Module {
    public static boolean isPlacing = false;
    private final Setting<Integer> blocksPerTick = this.register(new Setting<Integer>("BlocksPerTick", 12, 1, 20));
    private final Setting<Integer> delay = this.register(new Setting<Integer>("Delay", 0, 0, 250));
    private final Setting<Boolean> noGhost = this.register(new Setting<Boolean>("PacketPlace", false));
    private final Setting<Boolean> center = this.register(new Setting<Boolean>("TPCenter", false));
    private final Setting<Boolean> rotate = this.register(new Setting<Boolean>("Rotate", true));
    private final Timer timer = new Timer();
    private final Timer retryTimer = new Timer();
    private final Set<Vec3d> extendingBlocks = new HashSet<Vec3d>();
    private final Map<BlockPos, Integer> retries = new HashMap<BlockPos, Integer>();
    private int isSafe;
    private BlockPos startPos;
    private boolean didPlace = false;
    private boolean switchedItem;
    private int lastHotbarSlot;
    private boolean isSneaking;
    private int placements = 0;
    private int extenders = 1;
    private int obbySlot = -1;
    private boolean offHand = false;

    public Surround() {
        super("Surround", "Surrounds you with Obsidian", Module.Category.COMBAT, true, false, false);
    }

    @Override
    public void onEnable() {
        if (Surround.fullNullCheck()) {
            this.disable();
        }
        this.lastHotbarSlot = Surround.mc.player.inventory.currentItem;
        this.startPos = EntityUtil.getRoundedBlockPos(Surround.mc.player);
        if (this.center.getValue().booleanValue()) {
            OyVey.positionManager.setPositionPacket((double) this.startPos.getX() + 0.5, this.startPos.getY(), (double) this.startPos.getZ() + 0.5, true, true, true);
        }
        this.retries.clear();
        this.retryTimer.reset();
    }

    @Override
    public void onTick() {
        this.doFeetPlace();
    }

    @Override
    public void onDisable() {
        if (Surround.nullCheck()) {
            return;
        }
        isPlacing = false;
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
    }

    @Override
    public String getDisplayInfo() {
        switch (this.isSafe) {
            case 0: {
                return ChatFormatting.RED + "Unsafe";
            }
            case 1: {
                return ChatFormatting.YELLOW + "Safe";
            }
        }
        return ChatFormatting.GREEN + "Safe";
    }

    private void doFeetPlace() {
        if (this.check()) {
            return;
        }
        if (!EntityUtil.isSafe(Surround.mc.player, 0, true)) {
            this.isSafe = 0;
            this.placeBlocks(Surround.mc.player.getPositionVector(), EntityUtil.getUnsafeBlockArray(Surround.mc.player, 0, true), true, false, false);
        } else if (!EntityUtil.isSafe(Surround.mc.player, -1, false)) {
            this.isSafe = 1;
            this.placeBlocks(Surround.mc.player.getPositionVector(), EntityUtil.getUnsafeBlockArray(Surround.mc.player, -1, false), false, false, true);
        } else {
            this.isSafe = 2;
        }
        this.processExtendingBlocks();
        if (this.didPlace) {
            this.timer.reset();
        }
    }

    private void processExtendingBlocks() {
        if (this.extendingBlocks.size() == 2 && this.extenders < 1) {
            Vec3d[] array = new Vec3d[2];
            int i = 0;
            Iterator<Vec3d> iterator = this.extendingBlocks.iterator();
            while (iterator.hasNext()) {
                Vec3d vec3d;
                array[i] = vec3d = iterator.next();
                ++i;
            }
            int placementsBefore = this.placements;
            if (this.areClose(array) != null) {
                this.placeBlocks(this.areClose(array), EntityUtil.getUnsafeBlockArrayFromVec3d(this.areClose(array), 0, true), true, false, true);
            }
            if (placementsBefore < this.placements) {
                this.extendingBlocks.clear();
            }
        } else if (this.extendingBlocks.size() > 2 || this.extenders >= 1) {
            this.extendingBlocks.clear();
        }
    }

    private Vec3d areClose(Vec3d[] vec3ds) {
        int matches = 0;
        for (Vec3d vec3d : vec3ds) {
            for (Vec3d pos : EntityUtil.getUnsafeBlockArray(Surround.mc.player, 0, true)) {
                if (!vec3d.equals(pos)) continue;
                ++matches;
            }
        }
        if (matches == 2) {
            return Surround.mc.player.getPositionVector().add(vec3ds[0].add(vec3ds[1]));
        }
        return null;
    }

    private boolean placeBlocks(Vec3d pos, Vec3d[] vec3ds, boolean hasHelpingBlocks, boolean isHelping, boolean isExtending) {
        boolean gotHelp = true;
        block5:
        for (Vec3d vec3d : vec3ds) {
            gotHelp = true;
            BlockPos position = new BlockPos(pos).add(vec3d.x, vec3d.y, vec3d.z);
            switch (BlockUtil.isPositionPlaceable(position, false)) {
                case 1: {
                    if (this.retries.get(position) == null || this.retries.get(position) < 4) {
                        this.placeBlock(position);
                        this.retries.put(position, this.retries.get(position) == null ? 1 : this.retries.get(position) + 1);
                        this.retryTimer.reset();
                        continue block5;
                    }
                    if (OyVey.speedManager.getSpeedKpH() != 0.0 || isExtending || this.extenders >= 1) continue block5;
                    this.placeBlocks(Surround.mc.player.getPositionVector().add(vec3d), EntityUtil.getUnsafeBlockArrayFromVec3d(Surround.mc.player.getPositionVector().add(vec3d), 0, true), hasHelpingBlocks, false, true);
                    this.extendingBlocks.add(vec3d);
                    ++this.extenders;
                    continue block5;
                }
                case 2: {
                    if (!hasHelpingBlocks) continue block5;
                    gotHelp = this.placeBlocks(pos, BlockUtil.getHelpingBlocks(vec3d), false, true, true);
                }
                case 3: {
                    if (gotHelp) {
                        this.placeBlock(position);
                    }
                    if (!isHelping) continue block5;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean check() {
        if (Surround.nullCheck()) {
            return true;
        }
        int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
        if (obbySlot == -1 && eChestSot == -1) {
            this.toggle();
        }
        this.offHand = InventoryUtil.isBlock(Surround.mc.player.getHeldItemOffhand().getItem(), BlockObsidian.class);
        isPlacing = false;
        this.didPlace = false;
        this.extenders = 1;
        this.placements = 0;
        this.obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        int echestSlot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
        if (this.isOff()) {
            return true;
        }
        if (this.retryTimer.passedMs(2500L)) {
            this.retries.clear();
            this.retryTimer.reset();
        }
        if (this.obbySlot == -1 && !this.offHand && echestSlot == -1) {
            Command.sendMessage("<" + this.getDisplayName() + "> " + ChatFormatting.RED + "No Obsidian in hotbar disabling...");
            this.disable();
            return true;
        }
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
        if (Surround.mc.player.inventory.currentItem != this.lastHotbarSlot && Surround.mc.player.inventory.currentItem != this.obbySlot && Surround.mc.player.inventory.currentItem != echestSlot) {
            this.lastHotbarSlot = Surround.mc.player.inventory.currentItem;
        }
        if (!this.startPos.equals(EntityUtil.getRoundedBlockPos(Surround.mc.player))) {
            this.disable();
            return true;
        }
        return !this.timer.passedMs(this.delay.getValue().intValue());
    }

    private void placeBlock(BlockPos pos) {
        if (this.placements < this.blocksPerTick.getValue()) {
            int originalSlot = Surround.mc.player.inventory.currentItem;
            int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
            int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
            if (obbySlot == -1 && eChestSot == -1) {
                this.toggle();
            }
            isPlacing = true;
            Surround.mc.player.inventory.currentItem = obbySlot == -1 ? eChestSot : obbySlot;
            Surround.mc.playerController.updateController();
            this.isSneaking = BlockUtil.placeBlock(pos, this.offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, this.rotate.getValue(), this.noGhost.getValue(), this.isSneaking);
            Surround.mc.player.inventory.currentItem = originalSlot;
            Surround.mc.playerController.updateController();
            this.didPlace = true;
            ++this.placements;
        }
    }
}

