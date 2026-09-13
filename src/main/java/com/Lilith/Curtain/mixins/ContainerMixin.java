package com.Lilith.Curtain.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.Lilith.Curtain.CurtainRules;
import com.Lilith.Curtain.api.menu.IButtonContainer;

@Mixin(Container.class)
public abstract class ContainerMixin implements IButtonContainer {

    @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true)
    private void curtain$onSlotClick(int slotId, int clickedButton, int mode, EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir) {
        Container self = (Container) (Object) this;
        if (slotId < 0 || slotId >= self.inventorySlots.size()) return;
        Slot slot = self.inventorySlots.get(slotId);
        ItemStack stack = slot.getStack();

        // Curtain GUI buttons are never moved around, clicking them toggles their rule
        if (stack != null && stack.hasTagCompound()
            && stack.getTagCompound()
                .getBoolean("CurtainGUIItem")) {
            if (!player.worldObj.isRemote) {
                this.curtain$clickButton(slotId, player);
            }
            cir.setReturnValue(null);
            return;
        }

        // ctrl + q on the crafting result slot drops the whole craftable stack
        if (mode == 4 && CurtainRules.ctrlQCraftingFix
            && player.inventory.getItemStack() == null
            && slotId == 0
            && clickedButton == 1
            && slot.getHasStack()) {
            ItemStack crafted = slot.getStack()
                .copy();
            int guard = 0;
            while (slot.getHasStack() && guard++ < 64) {
                ItemStack before = slot.getStack() == null ? null
                    : slot.getStack()
                        .copy();
                self.slotClick(0, 0, 4, player);
                ItemStack after = slot.getStack();
                if (after == null || before == null
                    || after.getItem() != crafted.getItem()
                    || after.getItemDamage() != crafted.getItemDamage()) {
                    break;
                }
            }
            self.detectAndSendChanges();
            cir.setReturnValue(null);
        }
    }

    @Override
    public boolean curtain$clickButton(int slotId, EntityPlayer player) {
        return false;
    }

}
