package com.Lilith.Curtain.mixins.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.Lilith.Curtain.events.rules.PlayerEventHandler;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "useItemRightClick", at = @At("HEAD"))
    private void curtain$onUse(World world, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        if (player != null) PlayerEventHandler.onItemUse(player);
    }

    @Inject(method = "damageItem", at = @At("HEAD"))
    private void curtain$onHurtAndBreak(int amount, EntityLivingBase entity, CallbackInfo ci) {
        if (entity instanceof EntityPlayer) PlayerEventHandler.onItemDamaged((EntityPlayer) entity);
    }
}
