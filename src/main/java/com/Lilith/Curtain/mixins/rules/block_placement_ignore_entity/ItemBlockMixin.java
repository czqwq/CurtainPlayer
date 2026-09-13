package com.Lilith.Curtain.mixins.rules.block_placement_ignore_entity;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Lilith.Curtain.CurtainRules;

/**
 * Covers {@code blockPlacementIgnoreEntity} and the placement part of {@code creativeNoClip}:
 * both allow a creative player to place blocks inside entities / themselves.
 */
@Mixin(ItemBlock.class)
public abstract class ItemBlockMixin {

    @Redirect(
        method = "onItemUse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;canPlaceEntityOnSide(Lnet/minecraft/block/Block;IIIZILnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean curtain$skipCollisionCheck(World world, Block block, int x, int y, int z, boolean skipCollision,
        int side, Entity placer, ItemStack stack, ItemStack stackOuter, EntityPlayer player, World worldOuter,
        int xOuter, int yOuter, int zOuter, int sideOuter, float hitX, float hitY, float hitZ) {
        if (CurtainRules.blockPlacementIgnoreEntity && player != null && player.capabilities.isCreativeMode) {
            return true;
        }
        if (CurtainRules.isCreativeFlying(player)) {
            return true;
        }
        return world.canPlaceEntityOnSide(block, x, y, z, skipCollision, side, placer, stack);
    }
}
