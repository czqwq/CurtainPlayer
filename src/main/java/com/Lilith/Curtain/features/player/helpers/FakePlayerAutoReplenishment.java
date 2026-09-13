package com.Lilith.Curtain.features.player.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class FakePlayerAutoReplenishment {

    public static void autoReplenishment(EntityPlayer fakePlayer) {
        ItemStack[] itemStackList = fakePlayer.inventory.mainInventory;
        replenishment(fakePlayer.getCurrentEquippedItem(), itemStackList);
    }

    public static void replenishment(ItemStack itemStack, ItemStack[] itemStackList) {
        if (itemStack == null) return;
        int count = itemStack.getMaxStackSize() / 2;
        if (itemStack.stackSize <= 8 && count > 8) {
            for (ItemStack itemStack1 : itemStackList) {
                if (itemStack1 == null || itemStack1 == itemStack) continue;
                if (itemStack1.getItem() == itemStack.getItem()
                    && ItemStack.areItemStackTagsEqual(itemStack1, itemStack)
                    && itemStack1.getItemDamage() == itemStack.getItemDamage()) {
                    if (itemStack1.stackSize > count) {
                        itemStack.stackSize += count;
                        itemStack1.stackSize -= count;
                    } else {
                        itemStack.stackSize += itemStack1.stackSize;
                        itemStack1.stackSize = 0;
                    }
                    break;
                }
            }
        }
    }
}
