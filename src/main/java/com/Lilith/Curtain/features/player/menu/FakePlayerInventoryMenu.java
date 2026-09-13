package com.Lilith.Curtain.features.player.menu;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.Lilith.Curtain.api.menu.CustomMenu;
import com.Lilith.Curtain.api.menu.control.AutoResetButton;
import com.Lilith.Curtain.api.menu.control.Button;
import com.Lilith.Curtain.api.menu.control.RadioList;
import com.Lilith.Curtain.features.player.fakes.IServerPlayer;
import com.Lilith.Curtain.features.player.helpers.EntityPlayerActionPack;
import com.Lilith.Curtain.features.player.helpers.EntityPlayerActionPack.Action;
import com.Lilith.Curtain.features.player.helpers.EntityPlayerActionPack.ActionType;
import com.Lilith.Curtain.utils.TranslationHelper;

/**
 * Server side view of a fake player's inventory plus the control buttons of the GUI.
 *
 * <p>
 * Slot layout (54 slots, 6 rows):
 * 
 * <pre>
 * 0        STOP ALL
 * 1..4     armor (helmet, chest, legs, boots)
 * 5..13    hotbar selection 1..9
 * 14       attack every 14gt
 * 15       attack continuous
 * 16       use continuous
 * 17       reserved
 * 18..44   main inventory (9..35)
 * 45..53   hotbar (0..8)
 * </pre>
 */
public class FakePlayerInventoryMenu extends CustomMenu {

    private static final int BUTTON_COUNT = 14;

    public final ItemStack[] items;
    public final ItemStack[] armor;
    private final ItemStack[] buttons = new ItemStack[BUTTON_COUNT];
    private final EntityPlayer player;
    private final EntityPlayerActionPack ap;

    public FakePlayerInventoryMenu(EntityPlayer player) {
        this.player = player;
        this.items = player.inventory.mainInventory;
        this.armor = player.inventory.armorInventory;
        this.ap = player instanceof IServerPlayer ? ((IServerPlayer) player).getActionPack() : null;
        this.createButton();
        if (this.ap != null) this.ap.setSlot(1);
    }

    @Override
    public int getSizeInventory() {
        return this.items.length + this.armor.length + this.buttons.length;
    }

    /** @return {list, index} for the given container slot, or null when out of range */
    public Object[] getItemSlot(int slot) {
        if (slot == 0) return new Object[] { buttons, 0 };
        if (slot >= 1 && slot <= 4) return new Object[] { armor, 4 - slot };
        if (slot >= 5 && slot <= 13) return new Object[] { buttons, slot - 4 };
        if (slot == 14) return new Object[] { buttons, 10 };
        if (slot == 15) return new Object[] { buttons, 11 };
        if (slot == 16) return new Object[] { buttons, 12 };
        if (slot == 17) return new Object[] { buttons, 13 };
        if (slot >= 18 && slot <= 44) return new Object[] { items, slot - 9 };
        if (slot >= 45 && slot <= 53) return new Object[] { items, slot - 45 };
        return null;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        Object[] pair = getItemSlot(slot);
        if (pair == null) return null;
        ItemStack[] list = (ItemStack[]) pair[0];
        return list[(Integer) pair[1]];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        Object[] pair = getItemSlot(slot);
        if (pair == null) return null;
        ItemStack[] list = (ItemStack[]) pair[0];
        int index = (Integer) pair[1];
        if (list[index] == null) return null;
        ItemStack stack = list[index];
        if (stack.stackSize <= amount) {
            list[index] = null;
        } else {
            stack = stack.splitStack(amount);
            if (list[index].stackSize == 0) list[index] = null;
        }
        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        Object[] pair = getItemSlot(slot);
        if (pair == null) return null;
        ItemStack[] list = (ItemStack[]) pair[0];
        int index = (Integer) pair[1];
        ItemStack stack = list[index];
        list[index] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        Object[] pair = getItemSlot(slot);
        if (pair == null) return;
        ItemStack[] list = (ItemStack[]) pair[0];
        list[(Integer) pair[1]] = stack;
    }

    @Override
    public String getInventoryName() {
        return this.player.getCommandSenderName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.player.isEntityAlive() && player.getDistanceSqToEntity(this.player) <= 64.0D;
    }

    @Override
    public void markDirty() {}

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public EntityPlayerActionPack getActionPack() {
        return this.ap;
    }

    private void createButton() {
        List<Button> hotBarList = new ArrayList<Button>();
        for (int i = 0; i < 9; i++) {
            IChatComponent hotBarComponent = TranslationHelper
                .translate("curtain.rules.open_fake_player_inventory.menu.slot", EnumChatFormatting.WHITE, null, i + 1);
            boolean defaultState = i == 0;
            Button button = new Button(defaultState, i + 1, hotBarComponent, hotBarComponent);
            final int slot = i + 1;
            button.addTurnOnFunction(() -> { if (this.ap != null) this.ap.setSlot(slot); });
            this.addButton(5 + i, button);
            hotBarList.add(button);
        }
        this.addButtonList(new RadioList(hotBarList, true));

        final Button stopAll = new AutoResetButton("curtain.rules.open_fake_player_inventory.menu.stop_all");
        final Button attackInterval14 = new Button(
            false,
            "curtain.rules.open_fake_player_inventory.menu.attack_interval_14");
        final Button attackContinuous = new Button(
            false,
            "curtain.rules.open_fake_player_inventory.menu.attack_continuous");
        final Button useContinuous = new Button(false, "curtain.rules.open_fake_player_inventory.menu.use_continuous");

        stopAll.addTurnOnFunction(() -> {
            attackInterval14.turnOffWithoutFunction();
            attackContinuous.turnOffWithoutFunction();
            useContinuous.turnOffWithoutFunction();
            if (this.ap != null) this.ap.stopAll();
        });

        attackInterval14.addTurnOnFunction(() -> {
            if (this.ap != null) this.ap.start(ActionType.ATTACK, Action.interval(14));
            attackContinuous.turnOffWithoutFunction();
        });
        attackInterval14
            .addTurnOffFunction(() -> { if (this.ap != null) this.ap.start(ActionType.ATTACK, Action.once()); });

        attackContinuous.addTurnOnFunction(() -> {
            if (this.ap != null) this.ap.start(ActionType.ATTACK, Action.continuous());
            attackInterval14.turnOffWithoutFunction();
        });
        attackContinuous
            .addTurnOffFunction(() -> { if (this.ap != null) this.ap.start(ActionType.ATTACK, Action.once()); });

        useContinuous
            .addTurnOnFunction(() -> { if (this.ap != null) this.ap.start(ActionType.USE, Action.continuous()); });
        useContinuous.addTurnOffFunction(() -> { if (this.ap != null) this.ap.start(ActionType.USE, Action.once()); });

        this.addButton(0, stopAll);
        this.addButton(14, attackInterval14);
        this.addButton(15, attackContinuous);
        this.addButton(16, useContinuous);
    }

    @Override
    public void tick() {
        if (this.player.worldObj != null && !this.player.worldObj.isRemote && this.player.isEntityAlive()) {
            super.tick();
        }
    }
}
