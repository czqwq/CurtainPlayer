package com.Lilith.Curtain.mixins.rules.super_lead;

import net.minecraft.entity.EntityLiving;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.Lilith.Curtain.CurtainRules;

@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin {

    @Shadow
    public abstract boolean getLeashed();

    @Inject(method = "allowLeashing", at = @At("RETURN"), cancellable = true)
    private void curtain$canBeLeashed(CallbackInfoReturnable<Boolean> cir) {
        if (CurtainRules.superLead) {
            cir.setReturnValue(!this.getLeashed());
        }
    }
}
