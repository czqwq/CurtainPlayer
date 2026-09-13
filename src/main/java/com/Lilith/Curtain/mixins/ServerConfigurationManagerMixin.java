package com.Lilith.Curtain.mixins;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ServerConfigurationManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Lilith.Curtain.features.player.patches.EntityPlayerMPFake;

/**
 * Applies the requested spawn position of a fake player right before the player is added to the world.
 * Without this the entity tracker broadcasts the (wrong) world spawn position first and only corrects it
 * on a later tracker tick.
 */
@Mixin(ServerConfigurationManager.class)
public abstract class ServerConfigurationManagerMixin {

    @Inject(method = "playerLoggedIn", at = @At("HEAD"))
    private void curtain$placeFakePlayerBeforeTracking(EntityPlayerMP player, CallbackInfo ci) {
        if (player instanceof EntityPlayerMPFake) {
            EntityPlayerMPFake fake = (EntityPlayerMPFake) player;
            if (fake.fixStartingPosition != null) {
                fake.fixStartingPosition.run();
            }
        }
    }
}
