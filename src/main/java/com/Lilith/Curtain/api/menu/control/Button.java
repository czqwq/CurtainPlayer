package com.Lilith.Curtain.api.menu.control;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.Lilith.Curtain.api.Function;
import com.Lilith.Curtain.utils.TranslationHelper;

public class Button {

    private boolean init = false;
    private boolean flag;
    private final Item onItem;
    private final Item offItem;
    private final int itemCount;
    private final IChatComponent onText;
    private final IChatComponent offText;
    private final NBTTagCompound compoundTag = new NBTTagCompound();

    private final List<Function> turnOnFunctions = new ArrayList<Function>();
    private final List<Function> turnOffFunctions = new ArrayList<Function>();

    public Button() {
        this(true, Item.getItemFromBlock(Blocks.emerald_block), Item.getItemFromBlock(Blocks.coal_block));
    }

    public Button(boolean defaultState) {
        this(defaultState, Item.getItemFromBlock(Blocks.emerald_block), Item.getItemFromBlock(Blocks.coal_block));
    }

    public Button(int defaultState, int itemCount) {
        this(
            defaultState != 0,
            Item.getItemFromBlock(Blocks.emerald_block),
            Item.getItemFromBlock(Blocks.coal_block),
            itemCount);
    }

    public Button(boolean defaultState, int itemCount) {
        this(
            defaultState,
            Item.getItemFromBlock(Blocks.emerald_block),
            Item.getItemFromBlock(Blocks.coal_block),
            itemCount);
    }

    public Button(boolean defaultState, String key) {
        this(
            defaultState,
            Item.getItemFromBlock(Blocks.emerald_block),
            Item.getItemFromBlock(Blocks.coal_block),
            1,
            new ChatComponentText(TranslationHelper.translateLiteral(key, "on"))
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)),
            new ChatComponentText(TranslationHelper.translateLiteral(key, "off"))
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
    }

    public Button(boolean defaultState, IChatComponent onText, IChatComponent offText) {
        this(
            defaultState,
            Item.getItemFromBlock(Blocks.emerald_block),
            Item.getItemFromBlock(Blocks.coal_block),
            1,
            onText,
            offText);
    }

    public Button(boolean defaultState, int itemCount, IChatComponent onText, IChatComponent offText) {
        this(
            defaultState,
            Item.getItemFromBlock(Blocks.emerald_block),
            Item.getItemFromBlock(Blocks.coal_block),
            itemCount,
            onText,
            offText);
    }

    public Button(boolean defaultState, Item onItem, Item offItem) {
        this(defaultState, onItem, offItem, 1);
    }

    public Button(boolean defaultState, Item onItem, Item offItem, int itemCount) {
        this(
            defaultState,
            onItem,
            offItem,
            itemCount,
            new ChatComponentText("on").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)),
            new ChatComponentText("off").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
    }

    public Button(boolean defaultState, Item onItem, Item offItem, int itemCount, IChatComponent onText,
        IChatComponent offText) {
        this.flag = defaultState;
        this.onText = onText;
        this.offText = offText;
        this.onItem = onItem;
        this.offItem = offItem;
        this.itemCount = itemCount;
        this.compoundTag.setBoolean("CurtainGUIItem", true);
    }

    public void checkButton(IInventory container, int slot) {
        ItemStack onItemStack = new ItemStack(this.onItem, this.itemCount);
        onItemStack.setTagCompound((NBTTagCompound) this.compoundTag.copy());
        onItemStack.setStackDisplayName(this.onText.getUnformattedText());

        ItemStack offItemStack = new ItemStack(this.offItem, this.itemCount);
        offItemStack.setTagCompound((NBTTagCompound) this.compoundTag.copy());
        offItemStack.setStackDisplayName(this.offText.getUnformattedText());

        if (!this.init) {
            updateButton(container, slot, onItemStack, offItemStack);
            this.init = true;
        }
        updateButton(container, slot, onItemStack, offItemStack);
    }

    /** Toggle this button, running the matching callbacks. */
    public void click() {
        if (this.flag) {
            this.turnOff();
        } else {
            this.turnOn();
        }
    }

    public void updateButton(IInventory container, int slot, ItemStack onItemStack, ItemStack offItemStack) {
        ItemStack current = container.getStackInSlot(slot);
        if (current != null && current.getItem() != onItemStack.getItem()
            && current.getItem() != offItemStack.getItem()) {
            return;
        }
        ItemStack target = this.flag ? onItemStack : offItemStack;
        ItemStack existing = container.getStackInSlot(slot);
        if (existing == null || existing.getItem() != target.getItem()
            || !ItemStack.areItemStackTagsEqual(existing, target)) {
            container.setInventorySlotContents(slot, target);
        }
    }

    public void addTurnOnFunction(Function function) {
        this.turnOnFunctions.add(function);
    }

    public void addTurnOffFunction(Function function) {
        this.turnOffFunctions.add(function);
    }

    public void turnOnWithoutFunction() {
        this.flag = true;
    }

    public void turnOffWithoutFunction() {
        this.flag = false;
    }

    public void turnOn() {
        this.flag = true;
        runTurnOnFunction();
    }

    public void turnOff() {
        this.flag = false;
        runTurnOffFunction();
    }

    public void runTurnOnFunction() {
        for (Function turnOnFunction : this.turnOnFunctions) {
            turnOnFunction.accept();
        }
    }

    public void runTurnOffFunction() {
        for (Function turnOffFunction : this.turnOffFunctions) {
            turnOffFunction.accept();
        }
    }

    public boolean getFlag() {
        return flag;
    }

}
