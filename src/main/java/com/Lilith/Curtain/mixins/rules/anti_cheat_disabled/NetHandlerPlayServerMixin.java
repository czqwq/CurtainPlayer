package com.Lilith.Curtain.mixins.rules.anti_cheat_disabled;

import net.minecraft.network.NetHandlerPlayServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Lilith.Curtain.CurtainRules;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {

    @Shadow
    private int floatingTickCount;

    @Inject(method = "onNetworkTick", at = @At("HEAD"))
    private void curtain$restrictFloatingBits(CallbackInfo ci) {
        if (CurtainRules.antiCheatDisabled) {
            if (this.floatingTickCount > 70) this.floatingTickCount--;
        }
    }
}
