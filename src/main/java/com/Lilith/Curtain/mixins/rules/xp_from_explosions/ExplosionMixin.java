package com.Lilith.Curtain.mixins.rules.xp_from_explosions;

import net.minecraft.block.Block;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Lilith.Curtain.CurtainRules;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Redirect(
        method = "doExplosionB",
        at = @At(
            value = "INVOKE",
            remap = false,
            target = "Lnet/minecraft/block/Block;onBlockExploded(Lnet/minecraft/world/World;IIILnet/minecraft/world/Explosion;)V"))
    private void curtain$spawnXpAfterBreak(Block block, World world, int x, int y, int z, Explosion explosion) {
        if (CurtainRules.xpFromExplosions && !world.isRemote) {
            try {
                int meta = world.getBlockMetadata(x, y, z);
                int xp = block.getExpDrop(world, meta, 0);
                if (xp > 0) {
                    block.dropXpOnBlockBreak(world, x, y, z, xp);
                }
            } catch (RuntimeException ignored) {}
        }
        block.onBlockExploded(world, x, y, z, explosion);
    }
}
