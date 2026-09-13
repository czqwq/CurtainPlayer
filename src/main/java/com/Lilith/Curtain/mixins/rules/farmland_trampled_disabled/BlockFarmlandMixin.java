package com.Lilith.Curtain.mixins.rules.farmland_trampled_disabled;

import net.minecraft.block.BlockFarmland;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Lilith.Curtain.CurtainRules;

@Mixin(BlockFarmland.class)
public class BlockFarmlandMixin {

    @Inject(method = "onFallenUpon", at = @At("HEAD"), cancellable = true)
    private void curtain$farmlandTrampledDisabled(World worldIn, int x, int y, int z, Entity entityIn,
        float fallDistance, CallbackInfo ci) {
        if (CurtainRules.farmlandTrampledDisabled) ci.cancel();
    }
}
