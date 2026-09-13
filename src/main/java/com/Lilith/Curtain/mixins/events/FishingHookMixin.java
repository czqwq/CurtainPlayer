package com.Lilith.Curtain.mixins.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Lilith.Curtain.events.rules.fake_player_auto_fish.FishingHookEventHandler;
import com.Lilith.Curtain.features.player.patches.EntityPlayerMPFake;

@Mixin(EntityFishHook.class)
public abstract class FishingHookMixin {

    @Shadow
    private int field_146045_ax;

    @Shadow
    public EntityPlayer field_146042_b;

    @Unique
    private boolean curtain$wasBiting = false;

    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void curtain$checkBite(CallbackInfo ci) {
        boolean biting = this.field_146045_ax > 0;
        if (biting && !this.curtain$wasBiting) {
            EntityFishHook self = (EntityFishHook) (Object) this;
            if (this.field_146042_b instanceof EntityPlayerMPFake) {
                FishingHookEventHandler.onCatching(self, this.field_146042_b);
            }
        }
        this.curtain$wasBiting = biting;
    }
}
