package com.Lilith.Curtain.features.player.menu;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.Lilith.Curtain.api.menu.IButtonContainer;

public class ContainerFakePlayerInventory extends Container implements IButtonContainer {

    private static final int ROWS = 6;
    public final IInventory inventory;

    public ContainerFakePlayerInventory(EntityPlayer player) {
        IInventory inv;
        FakePlayerInventoryMenu menu = player.worldObj != null && player.worldObj.isRemote ? null
            : MenuHashMap.getViewed(player);
        if (menu == null) {
            inv = new InventoryBasic("FakePlayer", true, ROWS * 9);
        } else {
            inv = menu;
        }
        this.inventory = inv;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(inv, row * 9 + col, 8 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(player.inventory, col, 8 + col * 18, 198));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public boolean curtain$clickButton(int slotId, EntityPlayer player) {
        if (this.inventory instanceof FakePlayerInventoryMenu) {
            return ((FakePlayerInventoryMenu) this.inventory).clickButton(slotId);
        }
        return false;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = null;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < ROWS * 9) {
                if (!this.mergeItemStack(itemstack1, ROWS * 9, this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, ROWS * 9, false)) {
                return null;
            }
            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }
}
