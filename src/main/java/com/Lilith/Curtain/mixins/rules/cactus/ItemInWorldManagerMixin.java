package com.Lilith.Curtain.mixins.rules.cactus;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Lilith.Curtain.utils.BlockRotator;

@Mixin(ItemInWorldManager.class)
public abstract class ItemInWorldManagerMixin {

    @Redirect(
        method = "activateBlockOrUseItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;onBlockActivated(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;IFFF)Z"))
    private boolean curtain$activateWithOptionalCactus(Block block, World world, int x, int y, int z,
        EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (BlockRotator.flipBlockWithCactus(block, world, x, y, z, player, side, hitX, hitY, hitZ)) {
            return true;
        }
        return block.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
    }

    @SuppressWarnings("unused")
    private static ItemStack unused() {
        return null;
    }
}
