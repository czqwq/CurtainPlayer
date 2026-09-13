package com.Lilith.Curtain.api.menu;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.Lilith.Curtain.api.menu.control.Button;
import com.Lilith.Curtain.api.menu.control.ButtonList;

/**
 * A container backed inventory whose slots can be wired to {@link Button}s.
 *
 * <p>
 * In 1.20 this was a {@code Container}; 1.7.10 uses {@link IInventory} +
 * a vanilla {@code Container} on top of it.
 * </p>
 */
public abstract class CustomMenu implements IInventory {

    public final List<int[]> buttonSlots = new ArrayList<int[]>();
    public final List<Button> buttons = new ArrayList<Button>();
    public final List<ButtonList> buttonLists = new ArrayList<ButtonList>();

    public void tick() {
        this.checkButton();
    }

    public void addButton(int slot, Button button) {
        if (getSizeInventory() < (slot + 1)) {
            return;
        }
        this.buttons.add(button);
        this.buttonSlots.add(new int[] { slot, this.buttons.size() - 1 });
    }

    public void addButtonList(ButtonList buttonList) {
        this.buttonLists.add(buttonList);
    }

    private void checkButton() {
        for (int[] pair : this.buttonSlots) {
            this.buttons.get(pair[1])
                .checkButton(this, pair[0]);
        }
    }

    /** Toggles the button sitting in the given slot, returns true if there was one. */
    public boolean clickButton(int slot) {
        for (int[] pair : this.buttonSlots) {
            if (pair[0] == slot) {
                this.buttons.get(pair[1])
                    .click();
                return true;
            }
        }
        return false;
    }

    public boolean isButtonSlot(int slot) {
        for (int[] pair : this.buttonSlots) {
            if (pair[0] == slot) return true;
        }
        return false;
    }

    // IInventory boilerplate that subclasses may override
    @Override
    public String getInventoryName() {
        return "Curtain";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return !this.isButtonSlot(slot);
    }
}
