package com.Lilith.Curtain.features.player.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class FakePlayerAutoReplaceTool {

    public static void autoReplaceTool(EntityPlayer fakePlayer) {
        ItemStack mainHand = fakePlayer.getCurrentEquippedItem();
        if (mainHand != null && mainHand.isItemStackDamageable()
            && (mainHand.getMaxDamage() - mainHand.getItemDamage()) <= 10) {
            replaceTool(fakePlayer);
        }
    }

    public static void replaceTool(EntityPlayer fakePlayer) {
        ItemStack itemStack = fakePlayer.getCurrentEquippedItem();
        if (itemStack == null) return;
        for (int i = 0; i < fakePlayer.inventory.mainInventory.length; i++) {
            ItemStack itemStack1 = fakePlayer.inventory.mainInventory[i];
            if (itemStack1 == null || itemStack1 == itemStack) continue;
            if (itemStack1.getItem() == itemStack.getItem()
                && (itemStack1.getMaxDamage() - itemStack1.getItemDamage()) > 10) {
                ItemStack itemStack2 = itemStack1.copy();
                fakePlayer.inventory.mainInventory[i] = itemStack;
                fakePlayer.inventory.mainInventory[fakePlayer.inventory.currentItem] = itemStack2;
                break;
            }
        }
    }
}
