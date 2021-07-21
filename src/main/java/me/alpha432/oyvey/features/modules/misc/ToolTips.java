package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.event.events.Render2DEvent;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Bind;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.ColorUtil;
import me.alpha432.oyvey.util.RenderUtil;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ToolTips extends Module {
    public Setting<Boolean> maps;
    public Setting<Boolean> shulkers;
    public Setting<Bind> peek;
    public Setting<Boolean> shulkerSpy;
    public Setting<Boolean> render;
    public Setting<Boolean> own;
    public Setting<Integer> cooldown;
    public Setting<Boolean> textColor;
    private final Setting<Integer> red;
    private final Setting<Integer> green;
    private final Setting<Integer> blue;
    private final Setting<Integer> alpha;
    public Setting<Boolean> offsets;
    private final Setting<Integer> yPerPlayer;
    private final Setting<Integer> xOffset;
    private final Setting<Integer> yOffset;
    private final Setting<Integer> trOffset;
    public Setting<Integer> invH;
    private static ToolTips INSTANCE;
    public Map<EntityPlayer, ItemStack> spiedPlayers;
    public Map<EntityPlayer, Timer> playerTimers;
    private int textRadarY;

    public ToolTips() {
        super("ToolTips", "Several tweaks for tooltips.", Category.MISC, true, false, false);
        this.maps = (Setting<Boolean>) this.register(new Setting<>("Maps", true));
        this.shulkers = (Setting<Boolean>) this.register(new Setting<>("ShulkerViewer", true));
        this.peek = (Setting<Bind>) this.register(new Setting<>("Peek", new Bind(-1)));
        this.shulkerSpy = (Setting<Boolean>) this.register(new Setting<>("ShulkerSpy", true));
        this.render = (Setting<Boolean>) this.register(new Setting("Render", true, v -> this.shulkerSpy.getValue()));
        this.own = (Setting<Boolean>) this.register(new Setting("OwnShulker", true, v -> this.shulkerSpy.getValue()));
        this.cooldown = (Setting<Integer>) this.register(new Setting("ShowForS", 2, 0, 5, v -> this.shulkerSpy.getValue()));
        this.textColor = (Setting<Boolean>) this.register(new Setting("TextColor", false, v -> this.shulkers.getValue()));
        this.red = (Setting<Integer>) this.register(new Setting("Red", 255, 0, 255, v -> this.textColor.getValue()));
        this.green = (Setting<Integer>) this.register(new Setting("Green", 0, 0, 255, v -> this.textColor.getValue()));
        this.blue = (Setting<Integer>) this.register(new Setting("Blue", 0, 0, 255, v -> this.textColor.getValue()));
        this.alpha = (Setting<Integer>) this.register(new Setting("Alpha", 255, 0, 255, v -> this.textColor.getValue()));
        this.offsets = (Setting<Boolean>) this.register(new Setting<>("Offsets", false));
        this.yPerPlayer = (Setting<Integer>) this.register(new Setting("Y/Player", 18, v -> this.offsets.getValue()));
        this.xOffset = (Setting<Integer>) this.register(new Setting("XOffset", 4, v -> this.offsets.getValue()));
        this.yOffset = (Setting<Integer>) this.register(new Setting("YOffset", 2, v -> this.offsets.getValue()));
        this.trOffset = (Setting<Integer>) this.register(new Setting("TROffset", 2, v -> this.offsets.getValue()));
        this.invH = (Setting<Integer>) this.register(new Setting("InvH", 3, v -> this.offsets.getValue()));
        this.spiedPlayers = new ConcurrentHashMap<EntityPlayer, ItemStack>();
        this.playerTimers = new ConcurrentHashMap<EntityPlayer, Timer>();
        this.textRadarY = 0;
        this.setInstance();
    }

    private void setInstance() {
        ToolTips.INSTANCE = this;
    }

    public static ToolTips getInstance() {
        if (ToolTips.INSTANCE == null) {
            ToolTips.INSTANCE = new ToolTips();
        }
        return ToolTips.INSTANCE;
    }

    @Override
    public void onUpdate() {
        if (Feature.fullNullCheck() || !this.shulkerSpy.getValue()) {
            return;
        }
        if (this.peek.getValue().getKey() != -1 && ToolTips.mc.currentScreen instanceof GuiContainer && Keyboard.isKeyDown(this.peek.getValue().getKey())) {
            final Slot slot = ((GuiContainer) ToolTips.mc.currentScreen).getSlotUnderMouse();
            if (slot != null) {
                final ItemStack stack = slot.getStack();
                if (stack != null && stack.getItem() instanceof ItemShulkerBox) {
                    displayInv(stack, null);
                }
            }
        }
        for (final EntityPlayer player : ToolTips.mc.world.playerEntities) {
            if (player != null && player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox && (this.own.getValue() || !ToolTips.mc.player.equals((EntityPlayer) player))) {
                final ItemStack stack2 = player.getHeldItemMainhand();
                this.spiedPlayers.put(player, stack2);
            }
        }
    }

    @Override
    public void onRender2D(final Render2DEvent event) {
        if (Feature.fullNullCheck() || !this.shulkerSpy.getValue() || !this.render.getValue()) {
            return;
        }
        final int x = -4 + this.xOffset.getValue();
        int y = 10 + this.yOffset.getValue();
        this.textRadarY = 0;
        for (final EntityPlayer player : ToolTips.mc.world.playerEntities) {
            if (this.spiedPlayers.get(player) != null) {
                if (player.getHeldItemMainhand() == null || !(player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox)) {
                    final Timer playerTimer = this.playerTimers.get(player);
                    if (playerTimer == null) {
                        final Timer timer = new Timer();
                        timer.reset();
                        this.playerTimers.put(player, timer);
                    } else if (playerTimer.passedS(this.cooldown.getValue())) {
                        continue;
                    }
                } else if (player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox) {
                    final Timer playerTimer = this.playerTimers.get(player);
                    if (playerTimer != null) {
                        playerTimer.reset();
                        this.playerTimers.put(player, playerTimer);
                    }
                }
                final ItemStack stack = this.spiedPlayers.get(player);
                this.renderShulkerToolTip(stack, x, y, player.getName());
                y += this.yPerPlayer.getValue() + 60;
                this.textRadarY = y - 10 - this.yOffset.getValue() + this.trOffset.getValue();
            }
        }
    }

    public int getTextRadarY() {
        return this.textRadarY;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void makeTooltip(final ItemTooltipEvent event) {
    }

    @SubscribeEvent
    public void renderTooltip(final RenderTooltipEvent.PostText event) {
        if (this.maps.getValue() && !event.getStack().isEmpty() && event.getStack().getItem() instanceof ItemMap) {
            final MapData mapData = Items.FILLED_MAP.getMapData(event.getStack(), (World) ToolTips.mc.world);
            if (mapData != null) {
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                RenderHelper.disableStandardItemLighting();
                final Tessellator instance = Tessellator.getInstance();
                final BufferBuilder buffer = instance.getBuffer();
                final int n = 7;
                final float n2 = 135.0f;
                final float n3 = 0.5f;
                GlStateManager.translate((float) event.getX(), event.getY() - n2 * n3 - 5.0f, 0.0f);
                GlStateManager.scale(n3, n3, n3);
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                buffer.pos((double) (-n), (double) n2, 0.0).tex(0.0, 1.0).endVertex();
                buffer.pos((double) n2, (double) n2, 0.0).tex(1.0, 1.0).endVertex();
                buffer.pos((double) n2, (double) (-n), 0.0).tex(1.0, 0.0).endVertex();
                buffer.pos((double) (-n), (double) (-n), 0.0).tex(0.0, 0.0).endVertex();
                instance.draw();
                ToolTips.mc.entityRenderer.getMapItemRenderer().renderMap(mapData, false);
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }
        }
    }

    public void renderShulkerToolTip(final ItemStack stack, final int x, final int y, final String name) {
        final NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10)) {
            final NBTTagCompound blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag");
            if (blockEntityTag.hasKey("Items", 9)) {
                GlStateManager.enableTexture2D();
                GlStateManager.disableLighting();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderUtil.drawTexturedRect(x, y, 0, 0, 176, 16, 500);
                RenderUtil.drawTexturedRect(x, y + 16, 0, 16, 176, 54 + this.invH.getValue(), 500);
                RenderUtil.drawTexturedRect(x, y + 16 + 54, 0, 160, 176, 8, 500);
                GlStateManager.disableDepth();
                Color color = new Color(0, 0, 0, 255);
                if (this.textColor.getValue()) {
                    color = new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), this.alpha.getValue());
                }
                this.renderer.drawStringWithShadow((name == null) ? stack.getDisplayName() : name, (float) (x + 8), (float) (y + 6), ColorUtil.toRGBA(color));
                GlStateManager.enableDepth();
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableColorMaterial();
                GlStateManager.enableLighting();
                final NonNullList<ItemStack> nonnulllist = (NonNullList<ItemStack>) NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(blockEntityTag, (NonNullList<ItemStack>) nonnulllist);
                for (int i = 0; i < nonnulllist.size(); ++i) {
                    final int iX = x + i % 9 * 18 + 8;
                    final int iY = y + i / 9 * 18 + 18;
                    final ItemStack itemStack = (ItemStack) nonnulllist.get(i);
                    ToolTips.mc.getRenderItem().zLevel = 501.0f;
                    RenderUtil.itemRender.renderItemAndEffectIntoGUI(itemStack, iX, iY);
                    RenderUtil.itemRender.renderItemOverlayIntoGUI(ToolTips.mc.fontRenderer, itemStack, iX, iY, (String) null);
                    ToolTips.mc.getRenderItem().zLevel = 0.0f;
                }
                GlStateManager.disableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    public static void displayInv(final ItemStack stack, final String name) {
        /*try {
            final Item item = stack.getItem();
            final TileEntityShulkerBox entityBox = new TileEntityShulkerBox();
            final ItemShulkerBox shulker = (ItemShulkerBox)item;
            entityBox.blockType = shulker.getBlock();
            entityBox.setWorld((World)ToolTips.mc.world);
            ItemStackHelper.loadAllItems(stack.getTagCompound().getCompoundTag("BlockEntityTag"), entityBox.getItems());
            entityBox.readFromNBT(stack.getTagCompound().getCompoundTag("BlockEntityTag"));
            entityBox.setCustomName((name == null) ? stack.getDisplayName() : name);
            final IInventory inventory;
            new Thread(() -> {
                try {
                    Thread.sleep(200L);
                }
                catch (InterruptedException ex) {}
                ToolTips.mc.player.displayGUIChest(inventory);
            }).start();
        }
        catch (Exception ex2) {}*/
    }
}