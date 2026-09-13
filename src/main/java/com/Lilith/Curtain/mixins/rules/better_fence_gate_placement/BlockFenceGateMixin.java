package com.Lilith.Curtain.mixins.rules.better_fence_gate_placement;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Lilith.Curtain.CurtainRules;

@Mixin(BlockFenceGate.class)
public abstract class BlockFenceGateMixin {

    /**
     * When a gate is placed into an existing gate, keep the orientation of the existing one.
     */
    @Redirect(
        method = "onBlockPlacedBy",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MathHelper;floor_double(D)I"))
    private int curtain$keepExistingFacing(double value, World world, int x, int y, int z, EntityLivingBase placer,
        ItemStack stack) {
        if (CurtainRules.betterFenceGatePlacement && world.getBlock(x, y, z) instanceof BlockFenceGate) {
            return 0;
        }
        return MathHelper.floor_double(value);
    }
}
