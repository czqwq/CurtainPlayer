package com.Lilith.Curtain.mixins.rules.xp_no_cooldown;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Lilith.Curtain.CurtainRules;

@Mixin(EntityXPOrb.class)
public abstract class EntityXPOrbMixin {

    @Redirect(
        method = "onCollideWithPlayer",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/player/EntityPlayer;xpCooldown:I",
            opcode = org.objectweb.asm.Opcodes.GETFIELD))
    private int curtain$noCooldown(EntityPlayer player) {
        return CurtainRules.xpNoCooldown ? 0 : player.xpCooldown;
    }
}
