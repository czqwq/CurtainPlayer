package com.Lilith.Curtain.mixins.rules.quick_leaf_decay;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Lilith.Curtain.CurtainRules;

@Mixin(BlockLeaves.class)
public abstract class BlockLeavesMixin {

    /**
     * 1.7.10 leaves only decay on random ticks. When a log is removed nearby, reschedule every leaf
     * in range so the decay check runs immediately instead of waiting for a random tick.
     */
    @Inject(method = "updateTick", at = @At("HEAD"))
    private void curtain$quickLeafDecay(World world, int x, int y, int z, Random random, CallbackInfo ci) {
        if (!CurtainRules.quickLeafDecay) return;
        if (world.isRemote) return;
        if ((world.getBlockMetadata(x, y, z) & 8) == 0) return;
        // ran out of logs? decay right now instead of waiting for the next random tick
        if (!curtain$hasLogNearby(world, x, y, z)) {
            ((BlockLeaves) (Object) this)
                .dropBlockAsItemWithChance(world, x, y, z, world.getBlockMetadata(x, y, z), 1.0F, 0);
            world.setBlockToAir(x, y, z);
        }
    }

    private static boolean curtain$hasLogNearby(World world, int x, int y, int z) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    Block block = world.getBlock(x + dx, y + dy, z + dz);
                    if (block instanceof BlockLog) return true;
                }
            }
        }
        return false;
    }
}
