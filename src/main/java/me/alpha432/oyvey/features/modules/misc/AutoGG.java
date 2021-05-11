package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class AutoGG
        extends Module {
    private static AutoGG INSTANCE = new AutoGG();
    public Setting<String> custom = this.register(new Setting<String>("Custom", "Nigga-Hack.me"));
    public Setting<String> test = this.register(new Setting<String>("Test", "null"));
    private ConcurrentHashMap<String, Integer> targetedPlayers = null;

    public AutoGG() {
        super("AutoGG", "Sends msg after you kill someone", Module.Category.MISC, true, false, false);
        this.setInstance();
    }

    public static AutoGG getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new AutoGG();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.targetedPlayers = new ConcurrentHashMap();
    }

    @Override
    public void onDisable() {
        this.targetedPlayers = null;
    }

    @Override
    public void onUpdate() {
        if (AutoGG.nullCheck()) {
            return;
        }
        if (this.targetedPlayers == null) {
            this.targetedPlayers = new ConcurrentHashMap();
        }
        for (Entity entity : AutoGG.mc.world.getLoadedEntityList()) {
            String name2;
            EntityPlayer player;
            if (!(entity instanceof EntityPlayer) || (player = (EntityPlayer) entity).getHealth() > 0.0f || !this.shouldAnnounce(name2 = player.getName()))
                continue;
            this.doAnnounce(name2);
            break;
        }
        this.targetedPlayers.forEach((name, timeout) -> {
            if (timeout <= 0) {
                this.targetedPlayers.remove(name);
            } else {
                this.targetedPlayers.put(name, timeout - 1);
            }
        });
    }

    @SubscribeEvent
    public void onLeavingDeathEvent(LivingDeathEvent event) {
        EntityLivingBase entity;
        if (AutoGG.mc.player == null) {
            return;
        }
        if (this.targetedPlayers == null) {
            this.targetedPlayers = new ConcurrentHashMap();
        }
        if ((entity = event.getEntityLiving()) == null) {
            return;
        }
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        if (player.getHealth() > 0.0f) {
            return;
        }
        String name = player.getName();
        if (this.shouldAnnounce(name)) {
            this.doAnnounce(name);
        }
    }

    private boolean shouldAnnounce(String name) {
        return this.targetedPlayers.containsKey(name);
    }

    private void doAnnounce(String name) {
        this.targetedPlayers.remove(name);
        AutoGG.mc.player.connection.sendPacket(new CPacketChatMessage(this.custom.getValue()));
        int u = 0;
        for (int i = 0; i < 10; ++i) {
            ++u;
        }
        if (!this.test.getValue().equalsIgnoreCase("null")) {
            AutoGG.mc.player.connection.sendPacket(new CPacketChatMessage(this.test.getValue()));
        }
    }

    public void addTargetedPlayer(String name) {
        if (Objects.equals(name, AutoGG.mc.player.getName())) {
            return;
        }
        if (this.targetedPlayers == null) {
            this.targetedPlayers = new ConcurrentHashMap();
        }
        this.targetedPlayers.put(name, 20);
    }
}

