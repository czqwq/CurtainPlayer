package com.Lilith.Curtain.mixins;

import net.minecraft.entity.EnumCreatureType;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.Lilith.Curtain.features.logging.fakes.SpawnGroupInterface;
import com.Lilith.Curtain.utils.SpawnReporter;

@Mixin(EnumCreatureType.class)
public abstract class EnumCreatureTypeMixin implements SpawnGroupInterface {

    @Shadow
    @Final
    private int maxNumberOfCreature;

    @Inject(method = "getMaxNumberOfCreature", at = @At("HEAD"), cancellable = true)
    private void curtain$getModifiedCapacity(CallbackInfoReturnable<Integer> cir) {
        if (SpawnReporter.mobcap_exponent == 0.0D) return;
        cir.setReturnValue(
            (int) ((double) this.maxNumberOfCreature * (Math.pow(2.0, (SpawnReporter.mobcap_exponent / 4)))));
    }

    @Override
    public int getInitialSpawnCap() {
        return this.maxNumberOfCreature;
    }
}
