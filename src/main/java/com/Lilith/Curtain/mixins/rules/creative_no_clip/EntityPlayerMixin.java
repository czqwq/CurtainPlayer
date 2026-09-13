package com.Lilith.Curtain.mixins.rules.creative_no_clip;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Lilith.Curtain.CurtainRules;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin {

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void curtain$noClip(CallbackInfo ci) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        self.noClip = CurtainRules.isCreativeFlying(self);
    }
}
