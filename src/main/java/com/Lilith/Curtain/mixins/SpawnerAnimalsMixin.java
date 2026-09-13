package com.Lilith.Curtain.mixins;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.MathHelper;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.WorldServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.Lilith.Curtain.utils.SpawnReporter;

@Mixin(SpawnerAnimals.class)
public abstract class SpawnerAnimalsMixin {

    @Shadow
    private HashMap eligibleChunksForSpawning;

    @Redirect(
        method = "findChunksForSpawning",
        at = @At(
            value = "INVOKE",
            remap = false,
            target = "Lnet/minecraft/world/World;countEntities(Lnet/minecraft/entity/EnumCreatureType;Z)I"))
    private int curtain$captureCounts(WorldServer world, EnumCreatureType type, boolean forSpawnCount) {
        int count = world.countEntities(type, forSpawnCount);
        int dim = world.provider.dimensionId;
        SpawnReporter.chunkCounts.put(dim, this.eligibleChunksForSpawning.size());
        SpawnReporter.liveCounts.put(SpawnReporter.key(dim, type), count);
        if (SpawnReporter.track_spawns > 0L) {
            String key = SpawnReporter.key(dim, type);
            int tries = SpawnReporter.spawn_tries.get(type) == null ? 1 : SpawnReporter.spawn_tries.get(type);
            SpawnReporter.spawn_attempts.put(
                key,
                (SpawnReporter.spawn_attempts.get(key) == null ? 0L : SpawnReporter.spawn_attempts.get(key)) + tries);
            SpawnReporter.spawn_cap_count.put(
                key,
                (SpawnReporter.spawn_cap_count.get(key) == null ? 0L : SpawnReporter.spawn_cap_count.get(key)) + count);
            SpawnReporter.overall_spawn_ticks.put(
                key,
                (SpawnReporter.overall_spawn_ticks.get(key) == null ? 0L : SpawnReporter.overall_spawn_ticks.get(key))
                    + tries);
            if (count > SpawnReporter.getSpawnCap(world, type)) {
                SpawnReporter.spawn_ticks_full.put(
                    key,
                    (SpawnReporter.spawn_ticks_full.get(key) == null ? 0L : SpawnReporter.spawn_ticks_full.get(key))
                        + tries);
            }
        }
        if (SpawnReporter.mock_spawns) return 0;
        return count;
    }

    @Redirect(
        method = "findChunksForSpawning",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/WorldServer;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z"))
    private boolean curtain$spawnEntity(WorldServer world, Entity entity) {
        if (SpawnReporter.track_spawns > 0L && entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            SpawnReporter.registerSpawn(
                living,
                SpawnReporter.getCreatureType(living),
                MathHelper.floor_double(living.posX),
                MathHelper.floor_double(living.posY),
                MathHelper.floor_double(living.posZ));
        }
        if (SpawnReporter.mock_spawns) {
            entity.setDead();
            return false;
        }
        return world.spawnEntityInWorld(entity);
    }

    @Inject(method = "findChunksForSpawning", at = @At("RETURN"))
    private void curtain$onFinishSpawnCycle(WorldServer world, boolean hostile, boolean peaceful,
        boolean spawnOnSetTickRate, CallbackInfoReturnable<Integer> cir) {
        if (SpawnReporter.track_spawns > 0L && SpawnReporter.local_spawns != null) {
            int dim = world.provider.dimensionId;
            for (EnumCreatureType type : EnumCreatureType.values()) {
                String key = SpawnReporter.key(dim, type);
                Long spawned = SpawnReporter.local_spawns.get(type);
                if (spawned == null || spawned == 0L) {
                    SpawnReporter.spawn_ticks_fail.put(
                        key,
                        (SpawnReporter.spawn_ticks_fail.get(key) == null ? 0L : SpawnReporter.spawn_ticks_fail.get(key))
                            + 1);
                } else {
                    SpawnReporter.spawn_ticks_succ.put(
                        key,
                        (SpawnReporter.spawn_ticks_succ.get(key) == null ? 0L : SpawnReporter.spawn_ticks_succ.get(key))
                            + 1);
                    SpawnReporter.spawn_ticks_spawns.put(
                        key,
                        (SpawnReporter.spawn_ticks_spawns.get(key) == null ? 0L
                            : SpawnReporter.spawn_ticks_spawns.get(key)) + spawned);
                }
            }
        }
        SpawnReporter.local_spawns = null;
    }

    @SuppressWarnings("unused")
    private static Map<String, Integer> unusedLiveCounts() {
        return SpawnReporter.liveCounts;
    }
}
