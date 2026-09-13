package com.Lilith.Curtain.mixins.rules.cactus;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.Lilith.Curtain.utils.BlockRotator;

@Mixin(BlockDispenser.class)
public abstract class BlockDispenserMixin {

    @Inject(method = "func_149940_a", at = @At("HEAD"), cancellable = true)
    private void curtain$registerCurtainBehaviors(ItemStack stack, CallbackInfoReturnable<IBehaviorDispenseItem> cir) {
        if (stack != null
            && stack.getItem() == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.cactus)) {
            cir.setReturnValue(new BlockRotator.CactusDispenserBehaviour());
        }
    }
}
