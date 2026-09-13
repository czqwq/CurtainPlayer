package com.Lilith.Curtain.mixins;

import net.minecraft.entity.player.EntityPlayerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Lilith.Curtain.features.player.fakes.IServerPlayer;
import com.Lilith.Curtain.features.player.helpers.EntityPlayerActionPack;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin implements IServerPlayer {

    @Unique
    private EntityPlayerActionPack curtain$actionPack;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void curtain$initActionPack(CallbackInfo ci) {
        this.curtain$actionPack = new EntityPlayerActionPack((EntityPlayerMP) (Object) this);
    }

    @Override
    public EntityPlayerActionPack getActionPack() {
        if (this.curtain$actionPack == null) {
            this.curtain$actionPack = new EntityPlayerActionPack((EntityPlayerMP) (Object) this);
        }
        return this.curtain$actionPack;
    }
}
