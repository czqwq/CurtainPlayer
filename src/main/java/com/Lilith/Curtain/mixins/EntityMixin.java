package com.Lilith.Curtain.mixins;

import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.Lilith.Curtain.features.player.fakes.IEntity;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntity {

    @Shadow
    public float rotationYaw;

    @Shadow
    public float prevRotationYaw;

    @Override
    public float getMainYaw(float partialTicks) {
        return partialTicks == 1.0F ? this.rotationYaw
            : this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * partialTicks;
    }
}
