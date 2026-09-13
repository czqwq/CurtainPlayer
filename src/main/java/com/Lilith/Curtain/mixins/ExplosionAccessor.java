package com.Lilith.Curtain.mixins;

import java.util.Map;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Explosion.class)
public interface ExplosionAccessor {

    @Accessor("worldObj")
    World getWorld();

    @Accessor("explosionRNG")
    Random getRandom();

    @Accessor("field_77288_k")
    Map<EntityPlayer, Vec3> getHitPlayers();
}
