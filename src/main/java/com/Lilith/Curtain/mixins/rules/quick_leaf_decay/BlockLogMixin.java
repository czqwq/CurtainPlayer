package com.Lilith.Curtain.mixins.rules.quick_leaf_decay;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Lilith.Curtain.CurtainRules;

@Mixin(BlockLog.class)
public abstract class BlockLogMixin {

    @Inject(method = "breakBlock", at = @At("HEAD"))
    private void curtain$scheduleLeafDecay(World world, int x, int y, int z, Block block, int meta, CallbackInfo ci) {
        if (!CurtainRules.quickLeafDecay) return;
        if (world.isRemote) return;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    int bx = x + dx, by = y + dy, bz = z + dz;
                    Block target = world.getBlock(bx, by, bz);
                    if (target instanceof BlockLeaves) {
                        world.scheduleBlockUpdate(bx, by, bz, target, 1 + world.rand.nextInt(2));
                    }
                }
            }
        }
    }
}
