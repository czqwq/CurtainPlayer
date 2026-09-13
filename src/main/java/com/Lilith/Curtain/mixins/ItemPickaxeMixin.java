package com.Lilith.Curtain.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.Lilith.Curtain.CurtainRules;

/**
 * missingTools: pistons, glass and sponges can be broken faster with the proper tool.
 */
@Mixin(ItemPickaxe.class)
public abstract class ItemPickaxeMixin {

    @Inject(method = "func_150893_a", at = @At("RETURN"), cancellable = true)
    private void curtain$missingTools(ItemStack stack, Block block, CallbackInfoReturnable<Float> cir) {
        if (!CurtainRules.missingTools || block == null) return;
        if (cir.getReturnValue() > 1.0F) return;
        Material material = block.getMaterial();
        if (material == Material.glass || material == Material.rock || material == Material.iron) {
            cir.setReturnValue(((ItemToolAccessor) this).getEfficiencyOnProperMaterial());
        }
    }
}
