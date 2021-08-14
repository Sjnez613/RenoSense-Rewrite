package me.sjnez.renosense.features.modules.combat;

import com.mojang.authlib.GameProfile;
import io.netty.util.internal.ConcurrentSet;
import me.sjnez.renosense.RenoSense;
import me.sjnez.renosense.event.events.ClientEvent;
import me.sjnez.renosense.event.events.PacketEvent;
import me.sjnez.renosense.event.events.Render3DEvent;
import me.sjnez.renosense.event.events.UpdateWalkingPlayerEvent;
import me.sjnez.renosense.features.command.Command;
import me.sjnez.renosense.features.gui.PhobosGui;
import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.modules.client.Colors;
import me.sjnez.renosense.features.setting.Bind;
import me.sjnez.renosense.features.setting.Setting;
import me.sjnez.renosense.util.Timer;
import me.sjnez.renosense.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoCrystal
        extends Module {
    public static EntityPlayer target = null;
    public static Set<BlockPos> lowDmgPos = new ConcurrentSet();
    public static Set<BlockPos> placedPos = new HashSet <> ( );
    public static Set<BlockPos> brokenPos = new HashSet <> ( );
    private static AutoCrystal instance;
    public final me.sjnez.renosense.util.Timer threadTimer = new me.sjnez.renosense.util.Timer();
    private final Setting<Settings> setting = this.register( new Setting <> ( "Settings" , Settings.PLACE ));
    public final Setting<Boolean> attackOppositeHand = this.register(new Setting<Object>("OppositeHand", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV));
    public final Setting<Boolean> removeAfterAttack = this.register(new Setting<Object>("AttackRemove", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV));
    public final Setting<Boolean> antiBlock = this.register(new Setting<Object>("AntiFeetPlace", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV));
    private final Setting<Integer> switchCooldown = this.register(new Setting<Object>("Cooldown", 500 , 0 , 1000 , v -> this.setting.getValue() == Settings.MISC));
    private final Setting<Integer> eventMode = this.register(new Setting<Object>("Updates", 3 , 1 , 3 , v -> this.setting.getValue() == Settings.DEV));
    private final me.sjnez.renosense.util.Timer switchTimer = new me.sjnez.renosense.util.Timer();
    private final me.sjnez.renosense.util.Timer manualTimer = new me.sjnez.renosense.util.Timer();
    private final me.sjnez.renosense.util.Timer breakTimer = new me.sjnez.renosense.util.Timer();
    private final me.sjnez.renosense.util.Timer placeTimer = new me.sjnez.renosense.util.Timer();
    private final me.sjnez.renosense.util.Timer syncTimer = new me.sjnez.renosense.util.Timer();
    private final me.sjnez.renosense.util.Timer predictTimer = new me.sjnez.renosense.util.Timer();
    private final me.sjnez.renosense.util.Timer renderTimer = new me.sjnez.renosense.util.Timer();
    private final AtomicBoolean shouldInterrupt = new AtomicBoolean(false);
    private final me.sjnez.renosense.util.Timer syncroTimer = new me.sjnez.renosense.util.Timer();
    private final Map<EntityPlayer, me.sjnez.renosense.util.Timer> totemPops = new ConcurrentHashMap <> ( );
    private final Queue<CPacketUseEntity> packetUseEntities = new LinkedList <> ( );
    private final AtomicBoolean threadOngoing = new AtomicBoolean(false);
    public Setting<Raytrace> raytrace = this.register(new Setting<Object>("Raytrace", Raytrace.NONE, v -> this.setting.getValue() == Settings.MISC));
    public Setting<Boolean> place = this.register(new Setting<Object>("Place", Boolean.TRUE , v -> this.setting.getValue() == Settings.PLACE));
    public Setting<Integer> placeDelay = this.register(new Setting<Object>("PlaceDelay", 25 , 0 , 500 , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) ));
    public Setting<Float> placeRange = this.register(new Setting<Object>("PlaceRange", 6.0f , 0.0f , 10.0f , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) ));
    public Setting<Float> minDamage = this.register(new Setting<Object>("MinDamage", 7.0f , 0.1f , 20.0f , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) ));
    public Setting<Float> maxSelfPlace = this.register(new Setting<Object>("MaxSelfPlace", 10.0f , 0.1f , 36.0f , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) ));
    public Setting<Integer> wasteAmount = this.register(new Setting<Object>("WasteAmount", 2 , 1 , 5 , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) ));
    public Setting<Boolean> wasteMinDmgCount = this.register(new Setting<Object>("CountMinDmg", Boolean.TRUE , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) ));
    public Setting<Float> facePlace = this.register(new Setting<Object>("FacePlace", 8.0f , 0.1f , 20.0f , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) ));
    public Setting<Float> placetrace = this.register(new Setting<Object>("Placetrace", 4.5f , 0.0f , 10.0f , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) && this.raytrace.getValue() != Raytrace.NONE && this.raytrace.getValue() != Raytrace.BREAK));
    public Setting<Boolean> antiSurround = this.register(new Setting<Object>("AntiSurround", Boolean.TRUE , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) ));
    public Setting<Boolean> limitFacePlace = this.register(new Setting<Object>("LimitFacePlace", Boolean.TRUE , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) ));
    public Setting<Boolean> oneDot15 = this.register(new Setting<Object>("1.15", Boolean.FALSE , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) ));
    public Setting<Boolean> doublePop = this.register(new Setting<Object>("AntiTotem", Boolean.FALSE , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) ));
    public Setting<Double> popHealth = this.register(new Setting<Object>("PopHealth", 1.0 , 0.0 , 3.0 , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) && this.doublePop.getValue ( ) ));
    public Setting<Float> popDamage = this.register(new Setting<Object>("PopDamage", 4.0f , 0.0f , 6.0f , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) && this.doublePop.getValue ( ) ));
    public Setting<Integer> popTime = this.register(new Setting<Object>("PopTime", 500 , 0 , 1000 , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) && this.doublePop.getValue ( ) ));
    public Setting<Boolean> explode = this.register(new Setting<Object>("Break", Boolean.TRUE , v -> this.setting.getValue() == Settings.BREAK));
    public Setting<Switch> switchMode = this.register(new Setting<Object>("Attack", Switch.BREAKSLOT, v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) ));
    public Setting<Integer> breakDelay = this.register(new Setting<Object>("BreakDelay", 50 , 0 , 500 , v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) ));
    public Setting<Float> breakRange = this.register(new Setting<Object>("BreakRange", 6.0f , 0.0f , 10.0f , v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) ));
    public Setting<Integer> packets = this.register(new Setting<Object>("Packets", 1 , 1 , 6 , v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) ));
    public Setting<Float> maxSelfBreak = this.register(new Setting<Object>("MaxSelfBreak", 10.0f , 0.1f , 36.0f , v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) ));
    public Setting<Float> breaktrace = this.register(new Setting<Object>("Breaktrace", 4.5f , 0.0f , 10.0f , v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) && this.raytrace.getValue() != Raytrace.NONE && this.raytrace.getValue() != Raytrace.PLACE));
    public Setting<Boolean> manual = this.register(new Setting<Object>("Manual", Boolean.TRUE , v -> this.setting.getValue() == Settings.BREAK));
    public Setting<Boolean> manualMinDmg = this.register(new Setting<Object>("ManMinDmg", Boolean.TRUE , v -> this.setting.getValue() == Settings.BREAK && this.manual.getValue ( ) ));
    public Setting<Integer> manualBreak = this.register(new Setting<Object>("ManualDelay", 500 , 0 , 500 , v -> this.setting.getValue() == Settings.BREAK && this.manual.getValue ( ) ));
    public Setting<Boolean> sync = this.register(new Setting<Object>("Sync", Boolean.TRUE , v -> this.setting.getValue() == Settings.BREAK && ( this.explode.getValue ( ) || this.manual.getValue ( ) )));
    public Setting<Boolean> instant = this.register(new Setting<Object>("Predict", Boolean.TRUE , v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) && this.place.getValue ( ) ));
    public Setting<PredictTimer> instantTimer = this.register(new Setting<Object>("PredictTimer", PredictTimer.NONE, v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) && this.place.getValue ( ) && this.instant.getValue ( ) ));
    public Setting<Boolean> resetBreakTimer = this.register(new Setting<Object>("ResetBreakTimer", Boolean.TRUE , v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) && this.place.getValue ( ) && this.instant.getValue ( ) ));
    public Setting<Integer> predictDelay = this.register(new Setting<Object>("PredictDelay", 12 , 0 , 500 , v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) && this.place.getValue ( ) && this.instant.getValue ( ) && this.instantTimer.getValue() == PredictTimer.PREDICT));
    public Setting<Boolean> predictCalc = this.register(new Setting<Object>("PredictCalc", Boolean.TRUE , v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) && this.place.getValue ( ) && this.instant.getValue ( ) ));
    public Setting<Boolean> superSafe = this.register(new Setting<Object>("SuperSafe", Boolean.TRUE , v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) && this.place.getValue ( ) && this.instant.getValue ( ) ));
    public Setting<Boolean> antiCommit = this.register(new Setting<Object>("AntiOverCommit", Boolean.TRUE , v -> this.setting.getValue() == Settings.BREAK && this.explode.getValue ( ) && this.place.getValue ( ) && this.instant.getValue ( ) ));
    public Setting<Boolean> render = this.register(new Setting<Object>("Render", Boolean.TRUE , v -> this.setting.getValue() == Settings.RENDER));
    private final Setting<Integer> red = this.register(new Setting<Object>("Red", 255 , 0 , 255 , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) ));
    private final Setting<Integer> green = this.register(new Setting<Object>("Green", 255 , 0 , 255 , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) ));
    private final Setting<Integer> blue = this.register(new Setting<Object>("Blue", 255 , 0 , 255 , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) ));
    private final Setting<Integer> alpha = this.register(new Setting<Object>("Alpha", 255 , 0 , 255 , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) ));
    public Setting<Boolean> colorSync = this.register(new Setting<Object>("CSync", Boolean.FALSE , v -> this.setting.getValue() == Settings.RENDER));
    public Setting<Boolean> box = this.register(new Setting<Object>("Box", Boolean.TRUE , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) ));
    private final Setting<Integer> boxAlpha = this.register(new Setting<Object>("BoxAlpha", 125 , 0 , 255 , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) && this.box.getValue ( ) ));
    public Setting<Boolean> outline = this.register(new Setting<Object>("Outline", Boolean.TRUE , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) ));
    private final Setting<Float> lineWidth = this.register(new Setting<Object>("LineWidth", 1.5f , 0.1f , 5.0f , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) && this.outline.getValue ( ) ));
    public Setting<Boolean> text = this.register(new Setting<Object>("Text", Boolean.FALSE , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) ));
    public Setting<Boolean> customOutline = this.register(new Setting<Object>("CustomLine", Boolean.FALSE , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cRed = this.register(new Setting<Object>("OL-Red", 255 , 0 , 255 , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) && this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cGreen = this.register(new Setting<Object>("OL-Green", 255 , 0 , 255 , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) && this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cBlue = this.register(new Setting<Object>("OL-Blue", 255 , 0 , 255 , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) && this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    private final Setting<Integer> cAlpha = this.register(new Setting<Object>("OL-Alpha", 255 , 0 , 255 , v -> this.setting.getValue() == Settings.RENDER && this.render.getValue ( ) && this.customOutline.getValue ( ) && this.outline.getValue ( ) ));
    public Setting<Boolean> holdFacePlace = this.register(new Setting<Object>("HoldFacePlace", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Boolean> holdFaceBreak = this.register(new Setting<Object>("HoldSlowBreak", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC && this.holdFacePlace.getValue ( ) ));
    public Setting<Boolean> slowFaceBreak = this.register(new Setting<Object>("SlowFaceBreak", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Boolean> actualSlowBreak = this.register(new Setting<Object>("ActuallySlow", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Integer> facePlaceSpeed = this.register(new Setting<Object>("FaceSpeed", 500 , 0 , 500 , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Boolean> antiNaked = this.register(new Setting<Object>("AntiNaked", Boolean.TRUE , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Float> range = this.register(new Setting<Object>("Range", 12.0f , 0.1f , 20.0f , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Target> targetMode = this.register(new Setting<Object>("Target", Target.CLOSEST, v -> this.setting.getValue() == Settings.MISC));
    public Setting<Integer> minArmor = this.register(new Setting<Object>("MinArmor", 5 , 0 , 125 , v -> this.setting.getValue() == Settings.MISC));
    public Setting<AutoSwitch> autoSwitch = this.register(new Setting<Object>("Switch", AutoSwitch.TOGGLE, v -> this.setting.getValue() == Settings.MISC));
    public Setting<Bind> switchBind = this.register(new Setting<Object>("SwitchBind", new Bind(-1), v -> this.setting.getValue() == Settings.MISC && this.autoSwitch.getValue() == AutoSwitch.TOGGLE));
    public Setting<Boolean> offhandSwitch = this.register(new Setting<Object>("Offhand", Boolean.TRUE , v -> this.setting.getValue() == Settings.MISC && this.autoSwitch.getValue() != AutoSwitch.NONE));
    public Setting<Boolean> switchBack = this.register(new Setting<Object>("Switchback", Boolean.TRUE , v -> this.setting.getValue() == Settings.MISC && this.autoSwitch.getValue() != AutoSwitch.NONE && this.offhandSwitch.getValue ( ) ));
    public Setting<Boolean> lethalSwitch = this.register(new Setting<Object>("LethalSwitch", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC && this.autoSwitch.getValue() != AutoSwitch.NONE));
    public Setting<Boolean> mineSwitch = this.register(new Setting<Object>("MineSwitch", Boolean.TRUE , v -> this.setting.getValue() == Settings.MISC && this.autoSwitch.getValue() != AutoSwitch.NONE));
    public Setting<Rotate> rotate = this.register(new Setting<Object>("Rotate", Rotate.OFF, v -> this.setting.getValue() == Settings.MISC));
    public Setting<Boolean> suicide = this.register(new Setting<Object>("Suicide", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Boolean> webAttack = this.register(new Setting<Object>("WebAttack", Boolean.TRUE , v -> this.setting.getValue() == Settings.MISC && this.targetMode.getValue() != Target.DAMAGE));
    public Setting<Boolean> fullCalc = this.register(new Setting<Object>("ExtraCalc", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Boolean> sound = this.register(new Setting<Object>("Sound", Boolean.TRUE , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Float> soundPlayer = this.register(new Setting<Object>("SoundPlayer", 6.0f , 0.0f , 12.0f , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Boolean> soundConfirm = this.register(new Setting<Object>("SoundConfirm", Boolean.TRUE , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Boolean> extraSelfCalc = this.register(new Setting<Object>("MinSelfDmg", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC));
    public Setting<AntiFriendPop> antiFriendPop = this.register(new Setting<Object>("FriendPop", AntiFriendPop.NONE, v -> this.setting.getValue() == Settings.MISC));
    public Setting<Boolean> noCount = this.register(new Setting<Object>("AntiCount", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC && (this.antiFriendPop.getValue() == AntiFriendPop.ALL || this.antiFriendPop.getValue() == AntiFriendPop.BREAK)));
    public Setting<Boolean> calcEvenIfNoDamage = this.register(new Setting<Object>("BigFriendCalc", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC && (this.antiFriendPop.getValue() == AntiFriendPop.ALL || this.antiFriendPop.getValue() == AntiFriendPop.BREAK) && this.targetMode.getValue() != Target.DAMAGE));
    public Setting<Boolean> predictFriendDmg = this.register(new Setting<Object>("PredictFriend", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC && (this.antiFriendPop.getValue() == AntiFriendPop.ALL || this.antiFriendPop.getValue() == AntiFriendPop.BREAK) && this.instant.getValue ( ) ));
    public Setting<Float> minMinDmg = this.register(new Setting<Object>("MinMinDmg", 0.0f , 0.0f , 3.0f , v -> this.setting.getValue() == Settings.DEV && this.place.getValue ( ) ));
    public Setting<Boolean> breakSwing = this.register(new Setting<Object>("BreakSwing", Boolean.TRUE , v -> this.setting.getValue() == Settings.DEV));
    public Setting<Boolean> placeSwing = this.register(new Setting<Object>("PlaceSwing", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV));
    public Setting<Boolean> exactHand = this.register(new Setting<Object>("ExactHand", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV && this.placeSwing.getValue ( ) ));
    public Setting<Boolean> justRender = this.register(new Setting<Object>("JustRender", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV));
    public Setting<Boolean> fakeSwing = this.register(new Setting<Object>("FakeSwing", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV && this.justRender.getValue ( ) ));
    public Setting<Logic> logic = this.register(new Setting<Object>("Logic", Logic.BREAKPLACE, v -> this.setting.getValue() == Settings.DEV));
    public Setting<DamageSync> damageSync = this.register(new Setting<Object>("DamageSync", DamageSync.NONE, v -> this.setting.getValue() == Settings.DEV));
    public Setting<Integer> damageSyncTime = this.register(new Setting<Object>("SyncDelay", 500 , 0 , 500 , v -> this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE));
    public Setting<Float> dropOff = this.register(new Setting<Object>("DropOff", 5.0f , 0.0f , 10.0f , v -> this.setting.getValue() == Settings.DEV && this.damageSync.getValue() == DamageSync.BREAK));
    public Setting<Integer> confirm = this.register(new Setting<Object>("Confirm", 250 , 0 , 1000 , v -> this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE));
    public Setting<Boolean> syncedFeetPlace = this.register(new Setting<Object>("FeetSync", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE));
    public Setting<Boolean> fullSync = this.register(new Setting<Object>("FullSync", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue ( ) ));
    public Setting<Boolean> syncCount = this.register(new Setting<Object>("SyncCount", Boolean.TRUE , v -> this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue ( ) ));
    public Setting<Boolean> hyperSync = this.register(new Setting<Object>("HyperSync", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue ( ) ));
    public Setting<Boolean> gigaSync = this.register(new Setting<Object>("GigaSync", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue ( ) ));
    public Setting<Boolean> syncySync = this.register(new Setting<Object>("SyncySync", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue ( ) ));
    public Setting<Boolean> enormousSync = this.register(new Setting<Object>("EnormousSync", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue ( ) ));
    public Setting<Boolean> holySync = this.register(new Setting<Object>("UnbelievableSync", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue ( ) ));
    public Setting<Boolean> rotateFirst = this.register(new Setting<Object>("FirstRotation", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV && this.rotate.getValue() != Rotate.OFF && this.eventMode.getValue() == 2));
    public Setting<ThreadMode> threadMode = this.register(new Setting<Object>("Thread", ThreadMode.NONE, v -> this.setting.getValue() == Settings.DEV));
    public Setting<Integer> threadDelay = this.register(new Setting<Object>("ThreadDelay", 50 , 1 , 1000 , v -> this.setting.getValue() == Settings.DEV && this.threadMode.getValue() != ThreadMode.NONE));
    public Setting<Boolean> syncThreadBool = this.register(new Setting<Object>("ThreadSync", Boolean.TRUE , v -> this.setting.getValue() == Settings.DEV && this.threadMode.getValue() != ThreadMode.NONE));
    public Setting<Integer> syncThreads = this.register(new Setting<Object>("SyncThreads", 1000 , 1 , 10000 , v -> this.setting.getValue() == Settings.DEV && this.threadMode.getValue() != ThreadMode.NONE && this.syncThreadBool.getValue ( ) ));
    public Setting<Boolean> predictPos = this.register(new Setting<Object>("PredictPos", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV));
    public Setting<Integer> predictTicks = this.register(new Setting<Object>("ExtrapolationTicks", 2 , 1 , 20 , v -> this.setting.getValue() == Settings.DEV && this.predictPos.getValue ( ) ));
    public Setting<Integer> rotations = this.register(new Setting<Object>("Spoofs", 1 , 1 , 20 , v -> this.setting.getValue() == Settings.DEV));
    public Setting<Boolean> predictRotate = this.register(new Setting<Object>("PredictRotate", Boolean.FALSE , v -> this.setting.getValue() == Settings.DEV));
    public Setting<Float> predictOffset = this.register(new Setting<Object>("PredictOffset", 0.0f , 0.0f , 4.0f , v -> this.setting.getValue() == Settings.DEV));
    public Setting<Boolean> brownZombie = this.register(new Setting<Object>("BrownZombieMode", Boolean.FALSE , v -> this.setting.getValue() == Settings.MISC));
    public Setting<Boolean> doublePopOnDamage = this.register(new Setting<Object>("DamagePop", Boolean.FALSE , v -> this.setting.getValue() == Settings.PLACE && this.place.getValue ( ) && this.doublePop.getValue ( ) && this.targetMode.getValue() == Target.DAMAGE));
    public boolean rotating = false;
    private Queue<Entity> attackList = new ConcurrentLinkedQueue <> ( );
    private Map<Entity, Float> crystalMap = new HashMap <> ( );
    private Entity efficientTarget = null;
    private double currentDamage = 0.0;
    private double renderDamage = 0.0;
    private double lastDamage = 0.0;
    private boolean didRotation = false;
    private boolean switching = false;
    private BlockPos placePos = null;
    private BlockPos renderPos = null;
    private boolean mainHand = false;
    private boolean offHand = false;
    private int crystalCount = 0;
    private int minDmgCount = 0;
    private int lastSlot = -1;
    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private BlockPos webPos = null;
    private BlockPos lastPos = null;
    private boolean posConfirmed = false;
    private boolean foundDoublePop = false;
    private int rotationPacketsSpoofed = 0;
    private ScheduledExecutorService executor;
    private Thread thread;
    private EntityPlayer currentSyncTarget;
    private BlockPos syncedPlayerPos;
    private BlockPos syncedCrystalPos;
    private PlaceInfo placeInfo;
    private boolean addTolowDmg;

    public AutoCrystal() {
        super("AutoCrystal", "Best CA on the market", Module.Category.COMBAT, true, false, false);
        instance = this;
    }

    public static AutoCrystal getInstance() {
        if (instance == null) {
            instance = new AutoCrystal();
        }
        return instance;
    }

    @Override
    public void onTick() {
        if (this.threadMode.getValue() == ThreadMode.NONE && this.eventMode.getValue() == 3) {
            this.doAutoCrystal();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getStage() == 1) {
            this.postProcessing();
        }
        if (event.getStage() != 0) {
            return;
        }
        if (this.eventMode.getValue() == 2) {
            this.doAutoCrystal();
        }
    }

    public void postTick() {
        if (this.threadMode.getValue() != ThreadMode.NONE) {
            this.processMultiThreading();
        }
    }

    @Override
    public void onUpdate() {
        if (this.threadMode.getValue() == ThreadMode.NONE && this.eventMode.getValue() == 1) {
            this.doAutoCrystal();
        }
    }

    @Override
    public void onToggle() {
        brokenPos.clear();
        placedPos.clear();
        this.totemPops.clear();
        this.rotating = false;
    }

    @Override
    public void onDisable() {
        if (this.thread != null) {
            this.shouldInterrupt.set(true);
        }
        if (this.executor != null) {
            this.executor.shutdown();
        }
    }

    @Override
    public void onEnable() {
        if (this.threadMode.getValue() != ThreadMode.NONE) {
            this.processMultiThreading();
        }
    }

    @Override
    public String getDisplayInfo() {
        if (this.switching) {
            return "\u00a7aSwitch";
        }
        if (target != null) {
            return target.getName();
        }
        return null;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        CPacketUseEntity packet;
        if (event.getStage() == 0 && this.rotate.getValue() != Rotate.OFF && this.rotating && this.eventMode.getValue() != 2 && event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet2 = event.getPacket();
            packet2.yaw = this.yaw;
            packet2.pitch = this.pitch;
            ++this.rotationPacketsSpoofed;
            if (this.rotationPacketsSpoofed >= this.rotations.getValue()) {
                this.rotating = false;
                this.rotationPacketsSpoofed = 0;
            }
        }
        BlockPos pos = null;
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketUseEntity && (packet = event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK && packet.getEntityFromWorld(AutoCrystal.mc.world) instanceof EntityEnderCrystal) {
            pos = Objects.requireNonNull ( packet.getEntityFromWorld ( AutoCrystal.mc.world ) ).getPosition();
            if ( this.removeAfterAttack.getValue ( ) ) {
                Objects.requireNonNull(packet.getEntityFromWorld(AutoCrystal.mc.world)).setDead();
                AutoCrystal.mc.world.removeEntityFromWorld(packet.entityId);
            }
        }
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketUseEntity && (packet = event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK && packet.getEntityFromWorld(AutoCrystal.mc.world) instanceof EntityEnderCrystal) {
            EntityEnderCrystal crystal = (EntityEnderCrystal) packet.getEntityFromWorld(AutoCrystal.mc.world);
            if ( this.antiBlock.getValue ( ) && EntityUtil.isCrystalAtFeet(crystal, this.range.getValue ( ) ) && pos != null) {
                this.rotateToPos(pos);
                BlockUtil.placeCrystalOnBlock(this.placePos, this.offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, this.placeSwing.getValue(), this.exactHand.getValue());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
    public void onPacketReceive(PacketEvent.Receive event) {
        SPacketSoundEffect packet;
        if (AutoCrystal.fullNullCheck()) {
            return;
        }
        if (! this.justRender.getValue ( ) && this.switchTimer.passedMs( this.switchCooldown.getValue ( ) ) && this.explode.getValue ( ) && this.instant.getValue ( ) && event.getPacket() instanceof SPacketSpawnObject && (this.syncedCrystalPos == null || ! this.syncedFeetPlace.getValue ( ) || this.damageSync.getValue() == DamageSync.NONE)) {
            BlockPos pos;
            SPacketSpawnObject packet2 = event.getPacket();
            if (packet2.getType() == 51 && AutoCrystal.mc.player.getDistanceSq(pos = new BlockPos(packet2.getX(), packet2.getY(), packet2.getZ())) + (double) this.predictOffset.getValue ( ) <= MathUtil.square( this.breakRange.getValue ( ) ) && (this.instantTimer.getValue() == PredictTimer.NONE || this.instantTimer.getValue() == PredictTimer.BREAK && this.breakTimer.passedMs( this.breakDelay.getValue ( ) ) || this.instantTimer.getValue() == PredictTimer.PREDICT && this.predictTimer.passedMs( this.predictDelay.getValue ( ) ))) {
                if (this.predictSlowBreak(pos.down())) {
                    return;
                }
                if ( this.predictFriendDmg.getValue ( ) && (this.antiFriendPop.getValue() == AntiFriendPop.BREAK || this.antiFriendPop.getValue() == AntiFriendPop.ALL) && this.isRightThread()) {
                    for (EntityPlayer friend : AutoCrystal.mc.world.playerEntities) {
                        if (friend == null || AutoCrystal.mc.player.equals(friend) || friend.getDistanceSq(pos) > MathUtil.square( this.range.getValue ( ) + this.placeRange.getValue ( ) ) || !RenoSense.friendManager.isFriend(friend) || !((double) DamageUtil.calculateDamage(pos, friend) > (double) EntityUtil.getHealth(friend) + 0.5))
                            continue;
                        return;
                    }
                }
                if (placedPos.contains(pos.down())) {
                    float selfDamage;
                    if (this.isRightThread() && this.superSafe.getValue ( ) ? DamageUtil.canTakeDamage(this.suicide.getValue()) && ((double) (selfDamage = DamageUtil.calculateDamage(pos, AutoCrystal.mc.player)) - 0.5 > (double) EntityUtil.getHealth(AutoCrystal.mc.player) || selfDamage > this.maxSelfBreak.getValue ( ) ) : this.superSafe.getValue ( ) ) {
                        return;
                    }
                    this.attackCrystalPredict(packet2.getEntityID(), pos);
                } else if ( this.predictCalc.getValue ( ) && this.isRightThread()) {
                    float selfDamage = -1.0f;
                    if (DamageUtil.canTakeDamage(this.suicide.getValue())) {
                        selfDamage = DamageUtil.calculateDamage(pos, AutoCrystal.mc.player);
                    }
                    if ((double) selfDamage + 0.5 < (double) EntityUtil.getHealth(AutoCrystal.mc.player) && selfDamage <= this.maxSelfBreak.getValue ( ) ) {
                        for (EntityPlayer player : AutoCrystal.mc.world.playerEntities) {
                            float damage;
                            if (!(player.getDistanceSq(pos) <= MathUtil.square( this.range.getValue ( ) )) || !EntityUtil.isValid(player, this.range.getValue ( ) + this.breakRange.getValue ( ) ) || this.antiNaked.getValue ( ) && DamageUtil.isNaked(player) || !((damage = DamageUtil.calculateDamage(pos, player)) > selfDamage || damage > this.minDamage.getValue ( ) && !DamageUtil.canTakeDamage(this.suicide.getValue())) && !(damage > EntityUtil.getHealth(player)))
                                continue;
                            if ( this.predictRotate.getValue ( ) && this.eventMode.getValue() != 2 && (this.rotate.getValue() == Rotate.BREAK || this.rotate.getValue() == Rotate.ALL)) {
                                this.rotateToPos(pos);
                            }
                            this.attackCrystalPredict(packet2.getEntityID(), pos);
                            break;
                        }
                    }
                }
            }
        } else if (! this.soundConfirm.getValue ( ) && event.getPacket() instanceof SPacketExplosion) {
            SPacketExplosion packet3 = event.getPacket();
            BlockPos pos = new BlockPos(packet3.getX(), packet3.getY(), packet3.getZ()).down();
            this.removePos(pos);
        } else if (event.getPacket() instanceof SPacketDestroyEntities) {
            SPacketDestroyEntities packet4 = event.getPacket();
            for (int id : packet4.getEntityIDs()) {
                Entity entity = AutoCrystal.mc.world.getEntityByID(id);
                if (!(entity instanceof EntityEnderCrystal)) continue;
                brokenPos.remove(new BlockPos(entity.getPositionVector()).down());
                placedPos.remove(new BlockPos(entity.getPositionVector()).down());
            }
        } else if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet5 = event.getPacket();
            if (packet5.getOpCode() == 35 && packet5.getEntity(AutoCrystal.mc.world) instanceof EntityPlayer) {
                this.totemPops.put((EntityPlayer) packet5.getEntity(AutoCrystal.mc.world), new me.sjnez.renosense.util.Timer().reset());
            }
        } else if (event.getPacket() instanceof SPacketSoundEffect && (packet = event.getPacket()).getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
            if ( this.soundConfirm.getValue ( ) ) {
                this.removePos(pos);
            }
            if (this.threadMode.getValue() == ThreadMode.SOUND && this.isRightThread() && AutoCrystal.mc.player != null && AutoCrystal.mc.player.getDistanceSq(pos) < MathUtil.square( this.soundPlayer.getValue ( ) )) {
                this.handlePool(true);
            }
        }
    }

    private boolean predictSlowBreak(BlockPos pos) {
        if ( this.antiCommit.getValue ( ) && lowDmgPos.remove(pos)) {
            return this.shouldSlowBreak(false);
        }
        return false;
    }

    private boolean isRightThread() {
        return Util.mc.isCallingFromMinecraftThread() || !RenoSense.eventManager.ticksOngoing() && !this.threadOngoing.get();
    }

    private void attackCrystalPredict(int entityID, BlockPos pos) {
        if (!(! this.predictRotate.getValue ( ) || this.eventMode.getValue() == 2 && this.threadMode.getValue() == ThreadMode.NONE || this.rotate.getValue() != Rotate.BREAK && this.rotate.getValue() != Rotate.ALL)) {
            this.rotateToPos(pos);
        }
        CPacketUseEntity attackPacket = new CPacketUseEntity();
        attackPacket.entityId = entityID;
        attackPacket.action = CPacketUseEntity.Action.ATTACK;
        AutoCrystal.mc.player.connection.sendPacket(attackPacket);
        if ( this.breakSwing.getValue ( ) ) {
            AutoCrystal.mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        }
        if ( this.resetBreakTimer.getValue ( ) ) {
            this.breakTimer.reset();
        }
        this.predictTimer.reset();
    }

    private void removePos(BlockPos pos) {
        if (this.damageSync.getValue() == DamageSync.PLACE) {
            if (placedPos.remove(pos)) {
                this.posConfirmed = true;
            }
        } else if (this.damageSync.getValue() == DamageSync.BREAK && brokenPos.remove(pos)) {
            this.posConfirmed = true;
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if ((this.offHand || this.mainHand || this.switchMode.getValue() == Switch.CALC) && this.renderPos != null && this.render.getValue ( ) && ( this.box.getValue ( ) || this.text.getValue ( ) || this.outline.getValue ( ) )) {
            RenderUtil.drawBoxESP(this.renderPos, this.colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor() : new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue()), this.customOutline.getValue(), this.colorSync.getValue ( ) ? Colors.INSTANCE.getCurrentColor() : new Color(this.cRed.getValue(), this.cGreen.getValue(), this.cBlue.getValue(), this.cAlpha.getValue()), this.lineWidth.getValue ( ) , this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), false);
            if ( this.text.getValue ( ) ) {
                RenderUtil.drawText(this.renderPos, (Math.floor(this.renderDamage) == this.renderDamage ? Integer.valueOf((int) this.renderDamage) : String.format("%.1f", this.renderDamage)) + "");
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState() && !(AutoCrystal.mc.currentScreen instanceof PhobosGui) && this.switchBind.getValue().getKey() == Keyboard.getEventKey()) {
            if ( this.switchBack.getValue ( ) && this.offhandSwitch.getValue ( ) && this.offHand) {
                Offhand module = RenoSense.moduleManager.getModuleByClass(Offhand.class);
                if (module.isOff()) {
                    Command.sendMessage("<" + this.getDisplayName() + "> " + "\u00a7c" + "Switch failed. Enable the Offhand module.");
                } else if (module.type.getValue() == Offhand.Type.NEW) {
                    module.setSwapToTotem(true);
                    module.doOffhand();
                } else {
                    module.setMode(Offhand.Mode2.TOTEMS);
                    module.doSwitch();
                }
                return;
            }
            this.switching = !this.switching;
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting() != null && event.getSetting().getFeature() != null && event.getSetting().getFeature().equals(this) && this.isEnabled() && (event.getSetting().equals(this.threadDelay) || event.getSetting().equals(this.threadMode))) {
            if (this.executor != null) {
                this.executor.shutdown();
            }
            if (this.thread != null) {
                this.shouldInterrupt.set(true);
            }
        }
    }

    private void postProcessing() {
        if (this.threadMode.getValue() != ThreadMode.NONE || this.eventMode.getValue() != 2 || this.rotate.getValue() == Rotate.OFF || ! this.rotateFirst.getValue ( ) ) {
            return;
        }
        switch (this.logic.getValue()) {
            case BREAKPLACE: {
                this.postProcessBreak();
                this.postProcessPlace();
                break;
            }
            case PLACEBREAK: {
                this.postProcessPlace();
                this.postProcessBreak();
            }
        }
    }

    private void postProcessBreak() {
        while (!this.packetUseEntities.isEmpty()) {
            CPacketUseEntity packet = this.packetUseEntities.poll();
            AutoCrystal.mc.player.connection.sendPacket(packet);
            if ( this.breakSwing.getValue ( ) ) {
                AutoCrystal.mc.player.swingArm(EnumHand.MAIN_HAND);
            }
            this.breakTimer.reset();
        }
    }

    private void postProcessPlace() {
        if (this.placeInfo != null) {
            this.placeInfo.runPlace();
            this.placeTimer.reset();
            this.placeInfo = null;
        }
    }

    private void processMultiThreading() {
        if (this.isOff()) {
            return;
        }
        if (this.threadMode.getValue() == ThreadMode.WHILE) {
            this.handleWhile();
        } else if (this.threadMode.getValue() != ThreadMode.NONE) {
            this.handlePool(false);
        }
    }

    private void handlePool(boolean justDoIt) {
        if (justDoIt || this.executor == null || this.executor.isTerminated() || this.executor.isShutdown() || this.syncroTimer.passedMs( this.syncThreads.getValue ( ) ) && this.syncThreadBool.getValue ( ) ) {
            if (this.executor != null) {
                this.executor.shutdown();
            }
            this.executor = this.getExecutor();
            this.syncroTimer.reset();
        }
    }

    private void handleWhile() {
        if (this.thread == null || this.thread.isInterrupted() || !this.thread.isAlive() || this.syncroTimer.passedMs( this.syncThreads.getValue ( ) ) && this.syncThreadBool.getValue ( ) ) {
            if (this.thread == null) {
                this.thread = new Thread(RAutoCrystal.getInstance(this));
            } else if (this.syncroTimer.passedMs( this.syncThreads.getValue ( ) ) && !this.shouldInterrupt.get() && this.syncThreadBool.getValue ( ) ) {
                this.shouldInterrupt.set(true);
                this.syncroTimer.reset();
                return;
            }
            if (this.thread != null && (this.thread.isInterrupted() || !this.thread.isAlive())) {
                this.thread = new Thread(RAutoCrystal.getInstance(this));
            }
            if (this.thread != null && this.thread.getState() == Thread.State.NEW) {
                try {
                    this.thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.syncroTimer.reset();
            }
        }
    }

    private ScheduledExecutorService getExecutor() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(RAutoCrystal.getInstance(this), 0L, this.threadDelay.getValue ( ) , TimeUnit.MILLISECONDS);
        return service;
    }

    public void doAutoCrystal() {
        if ( this.brownZombie.getValue ( ) ) {
            return;
        }
        if (this.check()) {
            switch (this.logic.getValue()) {
                case PLACEBREAK: {
                    this.placeCrystal();
                    this.breakCrystal();
                    break;
                }
                case BREAKPLACE: {
                    this.breakCrystal();
                    this.placeCrystal();
                    break;
                }
            }
            this.manualBreaker();
        }
    }

    private boolean check() {
        if (AutoCrystal.fullNullCheck()) {
            return false;
        }
        if (this.syncTimer.passedMs( this.damageSyncTime.getValue ( ) )) {
            this.currentSyncTarget = null;
            this.syncedCrystalPos = null;
            this.syncedPlayerPos = null;
        } else if ( this.syncySync.getValue ( ) && this.syncedCrystalPos != null) {
            this.posConfirmed = true;
        }
        this.foundDoublePop = false;
        if (this.renderTimer.passedMs(500L)) {
            this.renderPos = null;
            this.renderTimer.reset();
        }
        this.mainHand = AutoCrystal.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL;
        this.offHand = AutoCrystal.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
        this.currentDamage = 0.0;
        this.placePos = null;
        if (this.lastSlot != AutoCrystal.mc.player.inventory.currentItem || AutoTrap.isPlacing || Surround.isPlacing) {
            this.lastSlot = AutoCrystal.mc.player.inventory.currentItem;
            this.switchTimer.reset();
        }
        if (!this.offHand && !this.mainHand) {
            this.placeInfo = null;
            this.packetUseEntities.clear();
        }
        if (this.offHand || this.mainHand) {
            this.switching = false;
        }
        if (!((this.offHand || this.mainHand || this.switchMode.getValue() != Switch.BREAKSLOT || this.switching) && DamageUtil.canBreakWeakness(AutoCrystal.mc.player) && this.switchTimer.passedMs( this.switchCooldown.getValue ( ) ))) {
            this.renderPos = null;
            target = null;
            this.rotating = false;
            return false;
        }
        if ( this.mineSwitch.getValue ( ) && Mouse.isButtonDown(0) && (this.switching || this.autoSwitch.getValue() == AutoSwitch.ALWAYS) && Mouse.isButtonDown(1) && AutoCrystal.mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
            this.switchItem();
        }
        this.mapCrystals();
        if (!this.posConfirmed && this.damageSync.getValue() != DamageSync.NONE && this.syncTimer.passedMs( this.confirm.getValue ( ) )) {
            this.syncTimer.setMs(this.damageSyncTime.getValue() + 1);
        }
        return true;
    }

    private void mapCrystals() {
        this.efficientTarget = null;
        if (this.packets.getValue() != 1) {
            this.attackList = new ConcurrentLinkedQueue <> ( );
            this.crystalMap = new HashMap <> ( );
        }
        this.crystalCount = 0;
        this.minDmgCount = 0;
        Entity maxCrystal = null;
        float maxDamage = 0.5f;
        for (Entity entity : AutoCrystal.mc.world.loadedEntityList) {
            if (entity.isDead || !(entity instanceof EntityEnderCrystal) || !this.isValid(entity)) continue;
            if ( this.syncedFeetPlace.getValue ( ) && entity.getPosition().down().equals(this.syncedCrystalPos) && this.damageSync.getValue() != DamageSync.NONE) {
                ++this.minDmgCount;
                ++this.crystalCount;
                if ( this.syncCount.getValue ( ) ) {
                    this.minDmgCount = this.wasteAmount.getValue() + 1;
                    this.crystalCount = this.wasteAmount.getValue() + 1;
                }
                if (! this.hyperSync.getValue ( ) ) continue;
                maxCrystal = null;
                break;
            }
            boolean count = false;
            boolean countMin = false;
            float selfDamage = -1.0f;
            if (DamageUtil.canTakeDamage(this.suicide.getValue())) {
                selfDamage = DamageUtil.calculateDamage(entity, AutoCrystal.mc.player);
            }
            if ((double) selfDamage + 0.5 < (double) EntityUtil.getHealth(AutoCrystal.mc.player) && selfDamage <= this.maxSelfBreak.getValue ( ) ) {
                Entity beforeCrystal = maxCrystal;
                float beforeDamage = maxDamage;
                for (EntityPlayer player : AutoCrystal.mc.world.playerEntities) {
                    float damage;
                    if (!(player.getDistanceSq(entity) <= MathUtil.square( this.range.getValue ( ) )))
                        continue;
                    if (EntityUtil.isValid(player, this.range.getValue ( ) + this.breakRange.getValue ( ) )) {
                        if ( this.antiNaked.getValue ( ) && DamageUtil.isNaked(player) || !((damage = DamageUtil.calculateDamage(entity, player)) > selfDamage || damage > this.minDamage.getValue ( ) && !DamageUtil.canTakeDamage(this.suicide.getValue())) && !(damage > EntityUtil.getHealth(player)))
                            continue;
                        if (damage > maxDamage) {
                            maxDamage = damage;
                            maxCrystal = entity;
                        }
                        if (this.packets.getValue() == 1) {
                            if (damage >= this.minDamage.getValue ( ) || ! this.wasteMinDmgCount.getValue ( ) ) {
                                count = true;
                            }
                            countMin = true;
                            continue;
                        }
                        if (this.crystalMap.get(entity) != null && !( this.crystalMap.get ( entity ) < damage))
                            continue;
                        this.crystalMap.put(entity, damage );
                        continue;
                    }
                    if (this.antiFriendPop.getValue() != AntiFriendPop.BREAK && this.antiFriendPop.getValue() != AntiFriendPop.ALL || !RenoSense.friendManager.isFriend(player.getName()) || !((double) DamageUtil.calculateDamage(entity, player) > (double) EntityUtil.getHealth(player) + 0.5))
                        continue;
                    maxCrystal = beforeCrystal;
                    maxDamage = beforeDamage;
                    this.crystalMap.remove(entity);
                    if (! this.noCount.getValue ( ) ) break;
                    count = false;
                    countMin = false;
                    break;
                }
            }
            if (!countMin) continue;
            ++this.minDmgCount;
            if (!count) continue;
            ++this.crystalCount;
        }
        if (this.damageSync.getValue() == DamageSync.BREAK && ((double) maxDamage > this.lastDamage || this.syncTimer.passedMs( this.damageSyncTime.getValue ( ) ) || this.damageSync.getValue() == DamageSync.NONE)) {
            this.lastDamage = maxDamage;
        }
        if ( this.enormousSync.getValue ( ) && this.syncedFeetPlace.getValue ( ) && this.damageSync.getValue() != DamageSync.NONE && this.syncedCrystalPos != null) {
            if ( this.syncCount.getValue ( ) ) {
                this.minDmgCount = this.wasteAmount.getValue() + 1;
                this.crystalCount = this.wasteAmount.getValue() + 1;
            }
            return;
        }
        if ( this.webAttack.getValue ( ) && this.webPos != null) {
            if (AutoCrystal.mc.player.getDistanceSq(this.webPos.up()) > MathUtil.square( this.breakRange.getValue ( ) )) {
                this.webPos = null;
            } else {
                for (Entity entity : AutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.webPos.up()))) {
                    if (!(entity instanceof EntityEnderCrystal)) continue;
                    this.attackList.add(entity);
                    this.efficientTarget = entity;
                    this.webPos = null;
                    this.lastDamage = 0.5;
                    return;
                }
            }
        }
        if (this.shouldSlowBreak(true) && maxDamage < this.minDamage.getValue ( ) && (target == null || !(EntityUtil.getHealth(target) <= this.facePlace.getValue ( ) ) || !this.breakTimer.passedMs( this.facePlaceSpeed.getValue ( ) ) && this.slowFaceBreak.getValue ( ) && Mouse.isButtonDown(0) && this.holdFacePlace.getValue ( ) && this.holdFaceBreak.getValue ( ) )) {
            this.efficientTarget = null;
            return;
        }
        if (this.packets.getValue() == 1) {
            this.efficientTarget = maxCrystal;
        } else {
            this.crystalMap = MathUtil.sortByValue(this.crystalMap, true);
            for (Map.Entry entry : this.crystalMap.entrySet()) {
                Entity crystal = (Entity) entry.getKey();
                float damage = (Float) entry.getValue ( );
                if (damage >= this.minDamage.getValue ( ) || ! this.wasteMinDmgCount.getValue ( ) ) {
                    ++this.crystalCount;
                }
                this.attackList.add(crystal);
                ++this.minDmgCount;
            }
        }
    }

    private boolean shouldSlowBreak(boolean withManual) {
        return withManual && this.manual.getValue ( ) && this.manualMinDmg.getValue ( ) && Mouse.isButtonDown(1) && (!Mouse.isButtonDown(0) || ! this.holdFacePlace.getValue ( ) ) || this.holdFacePlace.getValue ( ) && this.holdFaceBreak.getValue ( ) && Mouse.isButtonDown(0) && !this.breakTimer.passedMs( this.facePlaceSpeed.getValue ( ) ) || this.slowFaceBreak.getValue ( ) && !this.breakTimer.passedMs( this.facePlaceSpeed.getValue ( ) );
    }

    private void placeCrystal() {
        int crystalLimit = this.wasteAmount.getValue();
        if (this.placeTimer.passedMs( this.placeDelay.getValue ( ) ) && this.place.getValue ( ) && (this.offHand || this.mainHand || this.switchMode.getValue() == Switch.CALC || this.switchMode.getValue() == Switch.BREAKSLOT && this.switching)) {
            if (!(!this.offHand && !this.mainHand && (this.switchMode.getValue() == Switch.ALWAYS || this.switching) || this.crystalCount < crystalLimit || this.antiSurround.getValue ( ) && this.lastPos != null && this.lastPos.equals(this.placePos))) {
                return;
            }
            this.calculateDamage(this.getTarget(this.targetMode.getValue() == Target.UNSAFE));
            if (target != null && this.placePos != null) {
                if (!this.offHand && !this.mainHand && this.autoSwitch.getValue() != AutoSwitch.NONE && (this.currentDamage > (double) this.minDamage.getValue ( ) || this.lethalSwitch.getValue ( ) && EntityUtil.getHealth(target) <= this.facePlace.getValue ( ) ) && !this.switchItem()) {
                    return;
                }
                if (this.currentDamage < (double) this.minDamage.getValue ( ) && this.limitFacePlace.getValue ( ) ) {
                    crystalLimit = 1;
                }
                if (this.currentDamage >= (double) this.minMinDmg.getValue ( ) && (this.offHand || this.mainHand || this.autoSwitch.getValue() != AutoSwitch.NONE) && (this.crystalCount < crystalLimit || this.antiSurround.getValue ( ) && this.lastPos != null && this.lastPos.equals(this.placePos)) && (this.currentDamage > (double) this.minDamage.getValue ( ) || this.minDmgCount < crystalLimit) && this.currentDamage >= 1.0 && (DamageUtil.isArmorLow(target, this.minArmor.getValue()) || EntityUtil.getHealth(target) <= this.facePlace.getValue ( ) || this.currentDamage > (double) this.minDamage.getValue ( ) || this.shouldHoldFacePlace())) {
                    float damageOffset = this.damageSync.getValue() == DamageSync.BREAK ? this.dropOff.getValue ( ) - 5.0f : 0.0f;
                    boolean syncflag = false;
                    if ( this.syncedFeetPlace.getValue ( ) && this.placePos.equals(this.lastPos) && this.isEligableForFeetSync(target, this.placePos) && !this.syncTimer.passedMs( this.damageSyncTime.getValue ( ) ) && target.equals(this.currentSyncTarget) && target.getPosition().equals(this.syncedPlayerPos) && this.damageSync.getValue() != DamageSync.NONE) {
                        this.syncedCrystalPos = this.placePos;
                        this.lastDamage = this.currentDamage;
                        if ( this.fullSync.getValue ( ) ) {
                            this.lastDamage = 100.0;
                        }
                        syncflag = true;
                    }
                    if (syncflag || this.currentDamage - (double) damageOffset > this.lastDamage || this.syncTimer.passedMs( this.damageSyncTime.getValue ( ) ) || this.damageSync.getValue() == DamageSync.NONE) {
                        if (!syncflag && this.damageSync.getValue() != DamageSync.BREAK) {
                            this.lastDamage = this.currentDamage;
                        }
                        this.renderPos = this.placePos;
                        this.renderDamage = this.currentDamage;
                        if (this.switchItem()) {
                            this.currentSyncTarget = target;
                            this.syncedPlayerPos = target.getPosition();
                            if (this.foundDoublePop) {
                                this.totemPops.put(target, new me.sjnez.renosense.util.Timer().reset());
                            }
                            this.rotateToPos(this.placePos);
                            if (this.addTolowDmg || this.actualSlowBreak.getValue ( ) && this.currentDamage < (double) this.minDamage.getValue ( ) ) {
                                lowDmgPos.add(this.placePos);
                            }
                            placedPos.add(this.placePos);
                            if (! this.justRender.getValue ( ) ) {
                                if (this.eventMode.getValue() == 2 && this.threadMode.getValue() == ThreadMode.NONE && this.rotateFirst.getValue ( ) && this.rotate.getValue() != Rotate.OFF) {
                                    this.placeInfo = new PlaceInfo(this.placePos, this.offHand, this.placeSwing.getValue(), this.exactHand.getValue());
                                } else {
                                    BlockUtil.placeCrystalOnBlock(this.placePos, this.offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, this.placeSwing.getValue(), this.exactHand.getValue());
                                }
                            }
                            this.lastPos = this.placePos;
                            this.placeTimer.reset();
                            this.posConfirmed = false;
                            if (this.syncTimer.passedMs( this.damageSyncTime.getValue ( ) )) {
                                this.syncedCrystalPos = null;
                                this.syncTimer.reset();
                            }
                        }
                    }
                }
            } else {
                this.renderPos = null;
            }
        }
    }

    private boolean shouldHoldFacePlace() {
        this.addTolowDmg = false;
        if ( this.holdFacePlace.getValue ( ) && Mouse.isButtonDown(0)) {
            this.addTolowDmg = true;
            return true;
        }
        return false;
    }

    private boolean switchItem() {
        if (this.offHand || this.mainHand) {
            return true;
        }
        switch (this.autoSwitch.getValue()) {
            case NONE: {
                return false;
            }
            case TOGGLE: {
                if (!this.switching) {
                    return false;
                }
            }
            case ALWAYS: {
                if (!this.doSwitch()) break;
                return true;
            }
        }
        return false;
    }

    private boolean doSwitch() {
        if ( this.offhandSwitch.getValue ( ) ) {
            Offhand module = RenoSense.moduleManager.getModuleByClass(Offhand.class);
            if (module.isOff()) {
                Command.sendMessage("<" + this.getDisplayName() + "> " + "\u00a7c" + "Switch failed. Enable the Offhand module.");
                this.switching = false;
                return false;
            }
            if (module.type.getValue() == Offhand.Type.NEW) {
                module.setSwapToTotem(false);
                module.setMode(Offhand.Mode.CRYSTALS);
                module.doOffhand();
            } else {
                module.setMode(Offhand.Mode2.CRYSTALS);
                module.doSwitch();
            }
            this.switching = false;
            return true;
        }
        if (AutoCrystal.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            this.mainHand = false;
        } else {
            InventoryUtil.switchToHotbarSlot(ItemEndCrystal.class, false);
            this.mainHand = true;
        }
        this.switching = false;
        return true;
    }

    private void calculateDamage(EntityPlayer targettedPlayer) {
        BlockPos playerPos;
        if (targettedPlayer == null && this.targetMode.getValue() != Target.DAMAGE && ! this.fullCalc.getValue ( ) ) {
            return;
        }
        float maxDamage = 0.5f;
        EntityPlayer currentTarget = null;
        BlockPos currentPos = null;
        float maxSelfDamage = 0.0f;
        this.foundDoublePop = false;
        BlockPos setToAir = null;
        IBlockState state = null;
        if ( this.webAttack.getValue ( ) && targettedPlayer != null && AutoCrystal.mc.world.getBlockState(playerPos = new BlockPos(targettedPlayer.getPositionVector())).getBlock() == Blocks.WEB) {
            setToAir = playerPos;
            state = AutoCrystal.mc.world.getBlockState(playerPos);
            AutoCrystal.mc.world.setBlockToAir(playerPos);
        }
        block0:
        for (BlockPos pos : BlockUtil.possiblePlacePositions( this.placeRange.getValue ( ) , this.antiSurround.getValue(), this.oneDot15.getValue())) {
            if (!BlockUtil.rayTracePlaceCheck(pos, (this.raytrace.getValue() == Raytrace.PLACE || this.raytrace.getValue() == Raytrace.FULL) && AutoCrystal.mc.player.getDistanceSq(pos) > MathUtil.square( this.placetrace.getValue ( ) ), 1.0f))
                continue;
            float selfDamage = -1.0f;
            if (DamageUtil.canTakeDamage(this.suicide.getValue())) {
                selfDamage = DamageUtil.calculateDamage(pos, AutoCrystal.mc.player);
            }
            if (!((double) selfDamage + 0.5 < (double) EntityUtil.getHealth(AutoCrystal.mc.player)) || !(selfDamage <= this.maxSelfPlace.getValue ( ) ))
                continue;
            if (targettedPlayer != null) {
                float playerDamage = DamageUtil.calculateDamage(pos, targettedPlayer);
                if ( this.calcEvenIfNoDamage.getValue ( ) && (this.antiFriendPop.getValue() == AntiFriendPop.ALL || this.antiFriendPop.getValue() == AntiFriendPop.PLACE)) {
                    boolean friendPop = false;
                    for (EntityPlayer friend : AutoCrystal.mc.world.playerEntities) {
                        if (friend == null || AutoCrystal.mc.player.equals(friend) || friend.getDistanceSq(pos) > MathUtil.square( this.range.getValue ( ) + this.placeRange.getValue ( ) ) || !RenoSense.friendManager.isFriend(friend) || !((double) DamageUtil.calculateDamage(pos, friend) > (double) EntityUtil.getHealth(friend) + 0.5))
                            continue;
                        friendPop = true;
                        break;
                    }
                    if (friendPop) continue;
                }
                if (this.isDoublePoppable(targettedPlayer, playerDamage) && (currentPos == null || targettedPlayer.getDistanceSq(pos) < targettedPlayer.getDistanceSq(currentPos))) {
                    currentTarget = targettedPlayer;
                    maxDamage = playerDamage;
                    currentPos = pos;
                    this.foundDoublePop = true;
                    continue;
                }
                if (this.foundDoublePop || !(playerDamage > maxDamage) && (! this.extraSelfCalc.getValue ( ) || !(playerDamage >= maxDamage) || !(selfDamage < maxSelfDamage)) || !(playerDamage > selfDamage || playerDamage > this.minDamage.getValue ( ) && !DamageUtil.canTakeDamage(this.suicide.getValue())) && !(playerDamage > EntityUtil.getHealth(targettedPlayer)))
                    continue;
                maxDamage = playerDamage;
                currentTarget = targettedPlayer;
                currentPos = pos;
                maxSelfDamage = selfDamage;
                continue;
            }
            float maxDamageBefore = maxDamage;
            EntityPlayer currentTargetBefore = currentTarget;
            BlockPos currentPosBefore = currentPos;
            float maxSelfDamageBefore = maxSelfDamage;
            for (EntityPlayer player : AutoCrystal.mc.world.playerEntities) {
                if (EntityUtil.isValid(player, this.placeRange.getValue ( ) + this.range.getValue ( ) )) {
                    if ( this.antiNaked.getValue ( ) && DamageUtil.isNaked(player)) continue;
                    float playerDamage = DamageUtil.calculateDamage(pos, player);
                    if ( this.doublePopOnDamage.getValue ( ) && this.isDoublePoppable(player, playerDamage) && (currentPos == null || player.getDistanceSq(pos) < player.getDistanceSq(currentPos))) {
                        currentTarget = player;
                        maxDamage = playerDamage;
                        currentPos = pos;
                        maxSelfDamage = selfDamage;
                        this.foundDoublePop = true;
                        if (this.antiFriendPop.getValue() != AntiFriendPop.BREAK && this.antiFriendPop.getValue() != AntiFriendPop.PLACE)
                            continue;
                        continue block0;
                    }
                    if (this.foundDoublePop || !(playerDamage > maxDamage) && (! this.extraSelfCalc.getValue ( ) || !(playerDamage >= maxDamage) || !(selfDamage < maxSelfDamage)) || !(playerDamage > selfDamage || playerDamage > this.minDamage.getValue ( ) && !DamageUtil.canTakeDamage(this.suicide.getValue())) && !(playerDamage > EntityUtil.getHealth(player)))
                        continue;
                    maxDamage = playerDamage;
                    currentTarget = player;
                    currentPos = pos;
                    maxSelfDamage = selfDamage;
                    continue;
                }
                if (this.antiFriendPop.getValue() != AntiFriendPop.ALL && this.antiFriendPop.getValue() != AntiFriendPop.PLACE || player == null || !(player.getDistanceSq(pos) <= MathUtil.square( this.range.getValue ( ) + this.placeRange.getValue ( ) )) || !RenoSense.friendManager.isFriend(player) || !((double) DamageUtil.calculateDamage(pos, player) > (double) EntityUtil.getHealth(player) + 0.5))
                    continue;
                maxDamage = maxDamageBefore;
                currentTarget = currentTargetBefore;
                currentPos = currentPosBefore;
                maxSelfDamage = maxSelfDamageBefore;
                continue block0;
            }
        }
        if (setToAir != null) {
            AutoCrystal.mc.world.setBlockState(setToAir, state);
            this.webPos = currentPos;
        }
        target = currentTarget;
        this.currentDamage = maxDamage;
        this.placePos = currentPos;
    }

    private EntityPlayer getTarget(boolean unsafe) {
        if (this.targetMode.getValue() == Target.DAMAGE) {
            return null;
        }
        EntityPlayer currentTarget = null;
        for (EntityPlayer player : AutoCrystal.mc.world.playerEntities) {
            if (EntityUtil.isntValid(player, this.placeRange.getValue ( ) + this.range.getValue ( ) ) || this.antiNaked.getValue ( ) && DamageUtil.isNaked(player) || unsafe && EntityUtil.isSafe(player))
                continue;
            if (this.minArmor.getValue() > 0 && DamageUtil.isArmorLow(player, this.minArmor.getValue())) {
                currentTarget = player;
                break;
            }
            if (currentTarget == null) {
                currentTarget = player;
                continue;
            }
            if (!(AutoCrystal.mc.player.getDistanceSq(player) < AutoCrystal.mc.player.getDistanceSq(currentTarget)))
                continue;
            currentTarget = player;
        }
        if (unsafe && currentTarget == null) {
            return this.getTarget(false);
        }
        if ( this.predictPos.getValue ( ) && currentTarget != null) {
            currentTarget.getUniqueID ( );
            GameProfile profile = new GameProfile( currentTarget.getUniqueID() , currentTarget.getName());
            EntityOtherPlayerMP newTarget = new EntityOtherPlayerMP(AutoCrystal.mc.world, profile);
            Vec3d extrapolatePosition = MathUtil.extrapolatePlayerPosition(currentTarget, this.predictTicks.getValue());
            newTarget.copyLocationAndAnglesFrom(currentTarget);
            newTarget.posX = extrapolatePosition.x;
            newTarget.posY = extrapolatePosition.y;
            newTarget.posZ = extrapolatePosition.z;
            newTarget.setHealth(EntityUtil.getHealth(currentTarget));
            newTarget.inventory.copyInventory(currentTarget.inventory);
            currentTarget = newTarget;
        }
        return currentTarget;
    }

    private void breakCrystal() {
        if ( this.explode.getValue ( ) && this.breakTimer.passedMs( this.breakDelay.getValue ( ) ) && (this.switchMode.getValue() == Switch.ALWAYS || this.mainHand || this.offHand)) {
            if (this.packets.getValue() == 1 && this.efficientTarget != null) {
                if ( this.justRender.getValue ( ) ) {
                    this.doFakeSwing();
                    return;
                }
                if ( this.syncedFeetPlace.getValue ( ) && this.gigaSync.getValue ( ) && this.syncedCrystalPos != null && this.damageSync.getValue() != DamageSync.NONE) {
                    return;
                }
                this.rotateTo(this.efficientTarget);
                this.attackEntity(this.efficientTarget);
                this.breakTimer.reset();
            } else if (!this.attackList.isEmpty()) {
                if ( this.justRender.getValue ( ) ) {
                    this.doFakeSwing();
                    return;
                }
                if ( this.syncedFeetPlace.getValue ( ) && this.gigaSync.getValue ( ) && this.syncedCrystalPos != null && this.damageSync.getValue() != DamageSync.NONE) {
                    return;
                }
                for (int i = 0; i < this.packets.getValue(); ++i) {
                    Entity entity = this.attackList.poll();
                    if (entity == null) continue;
                    this.rotateTo(entity);
                    this.attackEntity(entity);
                }
                this.breakTimer.reset();
            }
        }
    }

    private void attackEntity(Entity entity) {
        if (entity != null) {
            if (this.eventMode.getValue() == 2 && this.threadMode.getValue() == ThreadMode.NONE && this.rotateFirst.getValue ( ) && this.rotate.getValue() != Rotate.OFF) {
                this.packetUseEntities.add(new CPacketUseEntity(entity));
            } else {
                EntityUtil.attackEntity(entity, this.sync.getValue(), this.breakSwing.getValue());
                brokenPos.add(new BlockPos(entity.getPositionVector()).down());
            }
        }
    }

    private void doFakeSwing() {
        if ( this.fakeSwing.getValue ( ) ) {
            EntityUtil.swingArmNoPacket(EnumHand.MAIN_HAND, AutoCrystal.mc.player);
        }
    }

    private void manualBreaker() {
        RayTraceResult result;
        if (this.rotate.getValue() != Rotate.OFF && this.eventMode.getValue() != 2 && this.rotating) {
            if (this.didRotation) {
                AutoCrystal.mc.player.rotationPitch = (float) ((double) AutoCrystal.mc.player.rotationPitch + 4.0E-4);
                this.didRotation = false;
            } else {
                AutoCrystal.mc.player.rotationPitch = (float) ((double) AutoCrystal.mc.player.rotationPitch - 4.0E-4);
                this.didRotation = true;
            }
        }
        if ((this.offHand || this.mainHand) && this.manual.getValue ( ) && this.manualTimer.passedMs( this.manualBreak.getValue ( ) ) && Mouse.isButtonDown(1) && AutoCrystal.mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE && AutoCrystal.mc.player.inventory.getCurrentItem().getItem() != Items.GOLDEN_APPLE && AutoCrystal.mc.player.inventory.getCurrentItem().getItem() != Items.BOW && AutoCrystal.mc.player.inventory.getCurrentItem().getItem() != Items.EXPERIENCE_BOTTLE && (result = AutoCrystal.mc.objectMouseOver) != null) {
            switch (result.typeOfHit) {
                case ENTITY: {
                    Entity entity = result.entityHit;
                    if (!(entity instanceof EntityEnderCrystal)) break;
                    EntityUtil.attackEntity(entity, this.sync.getValue(), this.breakSwing.getValue());
                    this.manualTimer.reset();
                    break;
                }
                case BLOCK: {
                    BlockPos mousePos = AutoCrystal.mc.objectMouseOver.getBlockPos().up();
                    for (Entity target : AutoCrystal.mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(mousePos))) {
                        if (!(target instanceof EntityEnderCrystal)) continue;
                        EntityUtil.attackEntity(target, this.sync.getValue(), this.breakSwing.getValue());
                        this.manualTimer.reset();
                    }
                    break;
                }
            }
        }
    }

    private void rotateTo(Entity entity) {
        switch (this.rotate.getValue()) {
            case OFF: {
                this.rotating = false;
            }
            case PLACE: {
                break;
            }
            case BREAK:
            case ALL: {
                float[] angle = MathUtil.calcAngle(AutoCrystal.mc.player.getPositionEyes(Util.mc.getRenderPartialTicks()), entity.getPositionVector());
                if (this.eventMode.getValue() == 2 && this.threadMode.getValue() == ThreadMode.NONE) {
                    RenoSense.rotationManager.setPlayerRotations(angle[0], angle[1]);
                    break;
                }
                this.yaw = angle[0];
                this.pitch = angle[1];
                this.rotating = true;
            }
        }
    }

    private void rotateToPos(BlockPos pos) {
        switch (this.rotate.getValue()) {
            case OFF: {
                this.rotating = false;
            }
            case BREAK: {
                break;
            }
            case PLACE:
            case ALL: {
                float[] angle = MathUtil.calcAngle(AutoCrystal.mc.player.getPositionEyes(Util.mc.getRenderPartialTicks()), new Vec3d((float) pos.getX() + 0.5f, (float) pos.getY() - 0.5f, (float) pos.getZ() + 0.5f));
                if (this.eventMode.getValue() == 2 && this.threadMode.getValue() == ThreadMode.NONE) {
                    RenoSense.rotationManager.setPlayerRotations(angle[0], angle[1]);
                    break;
                }
                this.yaw = angle[0];
                this.pitch = angle[1];
                this.rotating = true;
            }
        }
    }

    private boolean isDoublePoppable(EntityPlayer player, float damage) {
        float health;
        if ( this.doublePop.getValue ( ) && (double) (health = EntityUtil.getHealth(player)) <= this.popHealth.getValue() && (double) damage > (double) health + 0.5 && damage <= this.popDamage.getValue ( ) ) {
            Timer timer = this.totemPops.get(player);
            return timer == null || timer.passedMs( this.popTime.getValue ( ) );
        }
        return false;
    }

    private boolean isValid(Entity entity) {
        return entity != null && AutoCrystal.mc.player.getDistanceSq(entity) <= MathUtil.square( this.breakRange.getValue ( ) ) && (this.raytrace.getValue() == Raytrace.NONE || this.raytrace.getValue() == Raytrace.PLACE || AutoCrystal.mc.player.canEntityBeSeen(entity) || !AutoCrystal.mc.player.canEntityBeSeen(entity) && AutoCrystal.mc.player.getDistanceSq(entity) <= MathUtil.square( this.breaktrace.getValue ( ) ));
    }

    private boolean isEligableForFeetSync(EntityPlayer player, BlockPos pos) {
        if ( this.holySync.getValue ( ) ) {
            BlockPos playerPos = new BlockPos(player.getPositionVector());
            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == EnumFacing.DOWN || facing == EnumFacing.UP || !pos.equals( playerPos.down().offset(facing) ))
                    continue;
                return true;
            }
            return false;
        }
        return true;
    }

    public enum PredictTimer {
        NONE,
        BREAK,
        PREDICT

    }

    public enum AntiFriendPop {
        NONE,
        PLACE,
        BREAK,
        ALL

    }

    public enum ThreadMode {
        NONE,
        POOL,
        SOUND,
        WHILE

    }

    public enum AutoSwitch {
        NONE,
        TOGGLE,
        ALWAYS

    }

    public enum Raytrace {
        NONE,
        PLACE,
        BREAK,
        FULL

    }

    public enum Switch {
        ALWAYS,
        BREAKSLOT,
        CALC

    }

    public enum Logic {
        BREAKPLACE,
        PLACEBREAK

    }

    public enum Target {
        CLOSEST,
        UNSAFE,
        DAMAGE

    }

    public enum Rotate {
        OFF,
        PLACE,
        BREAK,
        ALL

    }

    public enum DamageSync {
        NONE,
        PLACE,
        BREAK

    }

    public enum Settings {
        PLACE,
        BREAK,
        RENDER,
        MISC,
        DEV

    }

    public static class PlaceInfo {
        private final BlockPos pos;
        private final boolean offhand;
        private final boolean placeSwing;
        private final boolean exactHand;

        public PlaceInfo(BlockPos pos, boolean offhand, boolean placeSwing, boolean exactHand) {
            this.pos = pos;
            this.offhand = offhand;
            this.placeSwing = placeSwing;
            this.exactHand = exactHand;
        }

        public void runPlace() {
            BlockUtil.placeCrystalOnBlock(this.pos, this.offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, this.placeSwing, this.exactHand);
        }
    }

    private static class RAutoCrystal
            implements Runnable {
        private static RAutoCrystal instance;
        private AutoCrystal autoCrystal;

        private RAutoCrystal() {
        }

        public static RAutoCrystal getInstance(AutoCrystal autoCrystal) {
            if (instance == null) {
                instance = new RAutoCrystal();
                RAutoCrystal.instance.autoCrystal = autoCrystal;
            }
            return instance;
        }

        @Override
        public void run() {
            if (this.autoCrystal.threadMode.getValue() == ThreadMode.WHILE) {
                while (this.autoCrystal.isOn() && this.autoCrystal.threadMode.getValue() == ThreadMode.WHILE) {
                    while (RenoSense.eventManager.ticksOngoing()) {
                    }
                    if (this.autoCrystal.shouldInterrupt.get()) {
                        this.autoCrystal.shouldInterrupt.set(false);
                        this.autoCrystal.syncroTimer.reset();
                        this.autoCrystal.thread.interrupt();
                        break;
                    }
                    this.autoCrystal.threadOngoing.set(true);
                    RenoSense.safetyManager.doSafetyCheck();
                    this.autoCrystal.doAutoCrystal();
                    this.autoCrystal.threadOngoing.set(false);
                    try {
                        Thread.sleep( this.autoCrystal.threadDelay.getValue ( ) );
                    } catch (InterruptedException e) {
                        this.autoCrystal.thread.interrupt();
                        e.printStackTrace();
                    }
                }
            } else if (this.autoCrystal.threadMode.getValue() != ThreadMode.NONE && this.autoCrystal.isOn()) {
                while (RenoSense.eventManager.ticksOngoing()) {
                }
                this.autoCrystal.threadOngoing.set(true);
                RenoSense.safetyManager.doSafetyCheck();
                this.autoCrystal.doAutoCrystal();
                this.autoCrystal.threadOngoing.set(false);
            }
        }
    }
}

