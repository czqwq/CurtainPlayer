package com.Lilith.Curtain.mixins.rules.placement_rotation_fix;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.Lilith.Curtain.CurtainRules;

/**
 * Makes block placement use the head yaw instead of the body yaw, like the 1.20 rule does with
 * {@code Direction.orderedByNearest}.
 */
@Mixin(ItemBlock.class)
public abstract class ItemBlockMixin {

    @Unique
    private float curtain$originalYaw;

    @Inject(method = "onItemUse", at = @At("HEAD"))
    private void curtain$useHeadYaw(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        if (CurtainRules.placementRotationFix && player != null) {
            this.curtain$originalYaw = player.rotationYaw;
            player.rotationYaw = player.rotationYawHead;
        }
    }

    @Inject(method = "onItemUse", at = @At("RETURN"))
    private void curtain$restoreYaw(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        if (CurtainRules.placementRotationFix && player != null) {
            player.rotationYaw = this.curtain$originalYaw;
        }
    }
}
