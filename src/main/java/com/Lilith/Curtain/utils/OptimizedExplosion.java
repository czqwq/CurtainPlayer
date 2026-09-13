package com.Lilith.Curtain.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import com.Lilith.Curtain.CurtainRules;
import com.Lilith.Curtain.features.logging.helper.ExplosionLogHelper;
import com.Lilith.Curtain.mixins.ExplosionAccessor;

/**
 * 1.7.10 port of the carpet "optimizedTNT" explosion.
 */
@SuppressWarnings("DuplicatedCode")
public class OptimizedExplosion {

    private static List<Entity> entitylist;
    private static Vec3 vec3dmem;
    private static long tickmem;
    /** Used to throttle the particles and sound of explosions that happen in the same spot. */
    public static int explosionSound = 0;

    private static final HashSet<ChunkPosition> affectedBlockPositionsSet = new HashSet<ChunkPosition>();
    private static boolean firstRay;
    private static boolean rayCalcDone;
    private static int blastX = Integer.MIN_VALUE, blastY, blastZ;

    public static void doExplosionA(Explosion e, ExplosionLogHelper eLogger) {
        ExplosionAccessor access = (ExplosionAccessor) e;
        World world = access.getWorld();
        float originalSize = e.explosionSize;

        affectedBlockPositionsSet.clear();
        blastCalc(e);

        if (!CurtainRules.explosionNoBlockDamage) {
            rayCalcDone = false;
            firstRay = true;
            getAffectedPositionsOnPlaneY(e, 0, 0, 15, 0, 15);
            getAffectedPositionsOnPlaneY(e, 15, 0, 15, 0, 15);
            getAffectedPositionsOnPlaneX(e, 0, 1, 14, 0, 15);
            getAffectedPositionsOnPlaneX(e, 15, 1, 14, 0, 15);
            getAffectedPositionsOnPlaneZ(e, 0, 1, 14, 1, 14);
            getAffectedPositionsOnPlaneZ(e, 15, 1, 14, 1, 14);
            e.affectedBlockPositions.addAll(affectedBlockPositionsSet);
            affectedBlockPositionsSet.clear();
        }

        float f3 = e.explosionSize * 2.0F;
        int k1 = MathHelper.floor_double(e.explosionX - (double) f3 - 1.0D);
        int l1 = MathHelper.floor_double(e.explosionX + (double) f3 + 1.0D);
        int i2 = MathHelper.floor_double(e.explosionY - (double) f3 - 1.0D);
        int i1 = MathHelper.floor_double(e.explosionY + (double) f3 + 1.0D);
        int j2 = MathHelper.floor_double(e.explosionZ - (double) f3 - 1.0D);
        int j1 = MathHelper.floor_double(e.explosionZ + (double) f3 + 1.0D);
        Vec3 vec3d = Vec3.createVectorHelper(e.explosionX, e.explosionY, e.explosionZ);

        if (vec3dmem == null || !vec3dmem.equals(vec3d) || tickmem != world.getTotalWorldTime()) {
            vec3dmem = vec3d;
            tickmem = world.getTotalWorldTime();
            entitylist = world.getEntitiesWithinAABBExcludingEntity(
                e.exploder,
                AxisAlignedBB
                    .getBoundingBox((double) k1, (double) i2, (double) j2, (double) l1, (double) i1, (double) j1));
            explosionSound = 0;
        }

        explosionSound++;

        List<Entity> entities = entitylist;
        if (entities == null) {
            entities = world.getEntitiesWithinAABBExcludingEntity(
                e.exploder,
                AxisAlignedBB
                    .getBoundingBox((double) k1, (double) i2, (double) j2, (double) l1, (double) i1, (double) j1));
        }

        e.explosionSize = f3;

        for (int index = 0; index < entities.size(); ++index) {
            Entity entity = entities.get(index);

            if (entity == e.exploder) {
                continue;
            }

            if (entity instanceof EntityTNTPrimed && e.exploder != null
                && entity.posX == e.exploder.posX
                && entity.posY == e.exploder.posY
                && entity.posZ == e.exploder.posZ) {
                if (eLogger != null) eLogger.onEntityImpacted(entity, 0, -0.9923437498509884d, 0);
                continue;
            }

            {
                double d12 = entity.getDistance(e.explosionX, e.explosionY, e.explosionZ) / (double) e.explosionSize;

                if (d12 <= 1.0D) {
                    double d5 = entity.posX - e.explosionX;
                    double d7 = (entity instanceof EntityTNTPrimed ? entity.posY
                        : entity.posY + (double) entity.getEyeHeight()) - e.explosionY;
                    double d9 = entity.posZ - e.explosionZ;
                    double d13 = (double) MathHelper.sqrt_double(d5 * d5 + d7 * d7 + d9 * d9);

                    if (d13 != 0.0D) {
                        d5 /= d13;
                        d7 /= d13;
                        d9 /= d13;
                        double density = world.getBlockDensity(vec3d, entity.boundingBox);
                        double d10 = (1.0D - d12) * density;
                        entity.attackEntityFrom(
                            DamageSource.setExplosionSource(e),
                            (float) ((int) ((d10 * d10 + d10) / 2.0D * 8.0D * (double) e.explosionSize + 1.0D)));
                        double d11 = EnchantmentProtection.func_92092_a(entity, d10);

                        if (eLogger != null) {
                            eLogger.onEntityImpacted(entity, d5 * d11, d7 * d11, d9 * d11);
                        }

                        entity.motionX += d5 * d11;
                        entity.motionY += d7 * d11;
                        entity.motionZ += d9 * d11;

                        if (entity instanceof EntityPlayer) {
                            EntityPlayer player = (EntityPlayer) entity;
                            if (!player.capabilities.isCreativeMode || !player.capabilities.isFlying) {
                                e.func_77277_b()
                                    .put(player, Vec3.createVectorHelper(d5 * d10, d7 * d10, d9 * d10));
                            }
                        }
                    }
                }
            }
        }
        e.explosionSize = originalSize;
    }

    public static void doExplosionB(Explosion e, boolean spawnParticles) {
        ExplosionAccessor access = (ExplosionAccessor) e;
        World world = access.getWorld();
        double posX = e.explosionX;
        double posY = e.explosionY;
        double posZ = e.explosionZ;

        if (explosionSound < 100 || explosionSound % 100 == 0) {
            world.playSoundEffect(
                posX,
                posY,
                posZ,
                "random.explode",
                4.0F,
                (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);

            if (spawnParticles) {
                if (e.explosionSize >= 2.0F && e.isSmoking) {
                    world.spawnParticle("hugeexplosion", posX, posY, posZ, 1.0D, 0.0D, 0.0D);
                } else {
                    world.spawnParticle("largeexplode", posX, posY, posZ, 1.0D, 0.0D, 0.0D);
                }
            }
        }

        if (e.isSmoking) {
            Iterator<ChunkPosition> iterator = e.affectedBlockPositions.iterator();

            while (iterator.hasNext()) {
                ChunkPosition chunkposition = iterator.next();
                int i = chunkposition.chunkPosX;
                int j = chunkposition.chunkPosY;
                int k = chunkposition.chunkPosZ;
                Block block = world.getBlock(i, j, k);

                if (spawnParticles) {
                    double d0 = (double) ((float) i + world.rand.nextFloat());
                    double d1 = (double) ((float) j + world.rand.nextFloat());
                    double d2 = (double) ((float) k + world.rand.nextFloat());
                    double d3 = d0 - posX;
                    double d4 = d1 - posY;
                    double d5 = d2 - posZ;
                    double d6 = (double) MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 /= d6;
                    d4 /= d6;
                    d5 /= d6;
                    double d7 = 0.5D / (d6 / (double) e.explosionSize + 0.1D);
                    d7 *= (double) (world.rand.nextFloat() * world.rand.nextFloat() + 0.3F);
                    d3 *= d7;
                    d4 *= d7;
                    d5 *= d7;
                    world.spawnParticle(
                        "explode",
                        (d0 + posX * 1.0D) / 2.0D,
                        (d1 + posY * 1.0D) / 2.0D,
                        (d2 + posZ * 1.0D) / 2.0D,
                        d3,
                        d4,
                        d5);
                    world.spawnParticle("smoke", d0, d1, d2, d3, d4, d5);
                }

                if (block.getMaterial() != Material.air) {
                    if (block.canDropFromExplosion(e)) {
                        block.dropBlockAsItemWithChance(
                            world,
                            i,
                            j,
                            k,
                            world.getBlockMetadata(i, j, k),
                            1.0F / e.explosionSize,
                            0);
                    }
                    if (CurtainRules.xpFromExplosions) {
                        try {
                            int xp = block.getExpDrop(world, world.getBlockMetadata(i, j, k), 0);
                            if (xp > 0) block.dropXpOnBlockBreak(world, i, j, k, xp);
                        } catch (RuntimeException ignored) {}
                    }
                    block.onBlockExploded(world, i, j, k, e);
                }
            }
        }

        if (e.isFlaming) {
            Iterator<ChunkPosition> iterator = e.affectedBlockPositions.iterator();

            while (iterator.hasNext()) {
                ChunkPosition chunkposition = iterator.next();
                int i = chunkposition.chunkPosX;
                int j = chunkposition.chunkPosY;
                int k = chunkposition.chunkPosZ;
                Block block = world.getBlock(i, j, k);
                Block block1 = world.getBlock(i, j - 1, k);

                if (block.getMaterial() == Material.air && block1.func_149730_j() && world.rand.nextInt(3) == 0) {
                    world.setBlock(i, j, k, Blocks.fire);
                }
            }
        }
    }

    private static void getAffectedPositionsOnPlaneX(Explosion e, int x, int yStart, int yEnd, int zStart, int zEnd) {
        if (rayCalcDone) return;
        final double xRel = (double) x / 15.0D * 2.0D - 1.0D;
        for (int z = zStart; z <= zEnd; ++z) {
            double zRel = (double) z / 15.0D * 2.0D - 1.0D;
            for (int y = yStart; y <= yEnd; ++y) {
                double yRel = (double) y / 15.0D * 2.0D - 1.0D;
                if (checkAffectedPosition(e, xRel, yRel, zRel)) return;
            }
        }
    }

    private static void getAffectedPositionsOnPlaneY(Explosion e, int y, int xStart, int xEnd, int zStart, int zEnd) {
        if (rayCalcDone) return;
        final double yRel = (double) y / 15.0D * 2.0D - 1.0D;
        for (int z = zStart; z <= zEnd; ++z) {
            double zRel = (double) z / 15.0D * 2.0D - 1.0D;
            for (int x = xStart; x <= xEnd; ++x) {
                double xRel = (double) x / 15.0D * 2.0D - 1.0D;
                if (checkAffectedPosition(e, xRel, yRel, zRel)) return;
            }
        }
    }

    private static void getAffectedPositionsOnPlaneZ(Explosion e, int z, int xStart, int xEnd, int yStart, int yEnd) {
        if (rayCalcDone) return;
        final double zRel = (double) z / 15.0D * 2.0D - 1.0D;
        for (int x = xStart; x <= xEnd; ++x) {
            double xRel = (double) x / 15.0D * 2.0D - 1.0D;
            for (int y = yStart; y <= yEnd; ++y) {
                double yRel = (double) y / 15.0D * 2.0D - 1.0D;
                if (checkAffectedPosition(e, xRel, yRel, zRel)) return;
            }
        }
    }

    private static boolean checkAffectedPosition(Explosion e, double xRel, double yRel, double zRel) {
        ExplosionAccessor access = (ExplosionAccessor) e;
        World world = access.getWorld();
        double len = Math.sqrt(xRel * xRel + yRel * yRel + zRel * zRel);
        double xInc = (xRel / len) * 0.3D;
        double yInc = (yRel / len) * 0.3D;
        double zInc = (zRel / len) * 0.3D;
        float rand = world.rand.nextFloat();
        float sizeRand = (CurtainRules.tntRandomRange >= 0 ? (float) CurtainRules.tntRandomRange : rand);
        float size = e.explosionSize * (0.7F + sizeRand * 0.6F);
        double posX = e.explosionX;
        double posY = e.explosionY;
        double posZ = e.explosionZ;

        for (float f1 = 0.3F; size > 0.0F; size -= 0.22500001F) {
            int bx = MathHelper.floor_double(posX);
            int by = MathHelper.floor_double(posY);
            int bz = MathHelper.floor_double(posZ);
            Block block = world.getBlock(bx, by, bz);

            if (block.getMaterial() != Material.air) {
                float resistance = e.exploder != null ? e.exploder.func_145772_a(e, world, bx, by, bz, block)
                    : block.getExplosionResistance(
                        e.exploder,
                        world,
                        bx,
                        by,
                        bz,
                        e.explosionX,
                        e.explosionY,
                        e.explosionZ);
                size -= (resistance + 0.3F) * f1;
            }

            if (size > 0.0F && (e.exploder == null || e.exploder.func_145774_a(e, world, bx, by, bz, block, size))) {
                affectedBlockPositionsSet.add(new ChunkPosition(bx, by, bz));
            } else if (firstRay) {
                rayCalcDone = true;
                return true;
            }

            firstRay = false;

            posX += xInc;
            posY += yInc;
            posZ += zInc;
        }

        return false;
    }

    private static void blastCalc(Explosion e) {
        if (blastX == Integer.MIN_VALUE) return;
        double dx = blastX - e.explosionX;
        double dy = blastY - e.explosionY;
        double dz = blastZ - e.explosionZ;
        if (dx * dx + dy * dy + dz * dz > 200) return;
    }

    public static void setBlastChanceLocation(int x, int y, int z) {
        blastX = x;
        blastY = y;
        blastZ = z;
    }
}
