package com.Lilith.Curtain.mixins.rules.creative_no_clip;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntityPiston;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Lilith.Curtain.CurtainRules;

@Mixin(TileEntityPiston.class)
public abstract class TileEntityPistonMixin {

    @Redirect(
        method = "func_145863_a",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveEntity(DDD)V"))
    private void curtain$dontPushNoClipPlayers(Entity entity, double x, double y, double z) {
        if (CurtainRules.isCreativeFlying(entity)) return;
        entity.moveEntity(x, y, z);
    }
}
