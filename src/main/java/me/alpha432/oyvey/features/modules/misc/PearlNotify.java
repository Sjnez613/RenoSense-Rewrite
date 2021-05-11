//
// Decompiled by Procyon v0.5.36
//

package me.alpha432.oyvey.features.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.UUID;

public class PearlNotify extends Module {
    private final HashMap<EntityPlayer, UUID> list;
    private Entity enderPearl;
    private boolean flag;

    public PearlNotify() {
        super("PearlNotify", "Notify pearl throws.", Category.MISC, true, false, false);
        this.list = new HashMap<EntityPlayer, UUID>();
    }

    @Override
    public void onEnable() {
        this.flag = true;
    }

    @Override
    public void onUpdate() {
        if (PearlNotify.mc.world == null || PearlNotify.mc.player == null) {
            return;
        }
        this.enderPearl = null;
        for (final Entity e : PearlNotify.mc.world.loadedEntityList) {
            if (e instanceof EntityEnderPearl) {
                this.enderPearl = e;
                break;
            }
        }
        if (this.enderPearl == null) {
            this.flag = true;
            return;
        }
        EntityPlayer closestPlayer = null;
        for (final EntityPlayer entity : PearlNotify.mc.world.playerEntities) {
            if (closestPlayer == null) {
                closestPlayer = entity;
            } else {
                if (closestPlayer.getDistance(this.enderPearl) <= entity.getDistance(this.enderPearl)) {
                    continue;
                }
                closestPlayer = entity;
            }
        }
        if (closestPlayer == PearlNotify.mc.player) {
            this.flag = false;
        }
        if (closestPlayer != null && this.flag) {
            String faceing = this.enderPearl.getHorizontalFacing().toString();
            if (faceing.equals("west")) {
                faceing = "east";
            } else if (faceing.equals("east")) {
                faceing = "west";
            }
            Command.sendMessage(OyVey.friendManager.isFriend(closestPlayer.getName()) ? (ChatFormatting.AQUA + closestPlayer.getName() + ChatFormatting.DARK_GRAY + " has just thrown a pearl heading " + faceing + "!") : (ChatFormatting.RED + closestPlayer.getName() + ChatFormatting.DARK_GRAY + " has just thrown a pearl heading " + faceing + "!"));
            this.flag = false;
        }
    }
}