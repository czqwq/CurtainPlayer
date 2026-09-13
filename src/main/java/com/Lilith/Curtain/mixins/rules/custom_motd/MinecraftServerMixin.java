package com.Lilith.Curtain.mixins.rules.custom_motd;

import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.Lilith.Curtain.CurtainRules;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "getMOTD", at = @At("HEAD"), cancellable = true)
    private void curtain$customMotd(CallbackInfoReturnable<String> cir) {
        if (CurtainRules.customMOTD != null && !CurtainRules.customMOTD.contentEquals("none")) {
            cir.setReturnValue(CurtainRules.customMOTD);
        }
    }
}
