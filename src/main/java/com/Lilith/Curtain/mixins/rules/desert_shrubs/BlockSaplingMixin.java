package com.Lilith.Curtain.mixins.rules.desert_shrubs;

import java.util.Random;

import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Lilith.Curtain.CurtainRules;

@Mixin(BlockSapling.class)
public abstract class BlockSaplingMixin {

    @Inject(method = "func_149878_d", at = @At("HEAD"), cancellable = true)
    private void curtain$onGenerate(World world, int x, int y, int z, Random random, CallbackInfo ci) {
        if (!CurtainRules.desertShrubs) return;
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        if (biome == null) return;
        boolean hot = biome.temperature >= 1.0F || biome.getTempCategory() == BiomeGenBase.TempCategory.WARM;
        if (hot && !curtain$nearWater(world, x, y, z)) {
            world.setBlock(x, y, z, Blocks.deadbush);
            ci.cancel();
        }
    }

    private static boolean curtain$nearWater(World world, int x, int y, int z) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 1; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    if (world.getBlock(x + dx, y + dy, z + dz)
                        .getMaterial() == Material.water) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
