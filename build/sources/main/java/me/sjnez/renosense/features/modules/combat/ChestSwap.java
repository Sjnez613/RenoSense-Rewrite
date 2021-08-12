package me.sjnez.renosense.features.modules.combat;

import me.sjnez.renosense.features.modules.Module;
import me.sjnez.renosense.features.setting.Setting;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;

public class ChestSwap extends Module
{
    public final Setting<Boolean> PreferElytra = new Setting<Boolean>("PreferElytra",true);
    public final Setting<Boolean> Curse = new Setting<Boolean>("Curse", false);

    public ChestSwap()
    {
        super("ChestSwap","Will attempt to instantly swap your chestplate with an elytra or vice versa, depending on what is already equipped", Category.COMBAT, true, false, false);
    }

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (mc.player == null)
            return;

        ItemStack l_ChestSlot = mc.player.inventoryContainer.getSlot(6).getStack();

        if (l_ChestSlot.isEmpty())
        {
            int l_Slot = FindChestItem(PreferElytra.getValue());

            if (!PreferElytra.getValue() && l_Slot == -1)
                l_Slot = FindChestItem(true);

            if (l_Slot != -1)
            {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0, ClickType.PICKUP, mc.player);
            }

            toggle();
            return;
        }

        int l_Slot = FindChestItem(l_ChestSlot.getItem() instanceof ItemArmor);

        if (l_Slot != -1)
        {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0, ClickType.PICKUP, mc.player);
        }

        toggle();
    }

    private int FindChestItem(boolean p_Elytra)
    {
        int slot = -1;
        float damage = 0;

        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i)
        {
            /// @see: https://wiki.vg/Inventory, 0 is crafting slot, and 5,6,7,8 are Armor slots
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);
            if (s != null && s.getItem() != Items.AIR)
            {
                if (s.getItem() instanceof ItemArmor)
                {
                    final ItemArmor armor = (ItemArmor) s.getItem();
                    if (armor.armorType == EntityEquipmentSlot.CHEST)
                    {
                        final float currentDamage = (armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, s));

                        final boolean cursed = Curse.getValue() ? (EnchantmentHelper.hasBindingCurse(s)) : false;

                        if (currentDamage > damage && !cursed)
                        {
                            damage = currentDamage;
                            slot = i;
                        }
                    }
                }
                else if (p_Elytra && s.getItem() instanceof ItemElytra)
                    return i;
            }
        }

        return slot;
    }

}