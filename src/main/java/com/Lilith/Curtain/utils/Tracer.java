package com.Lilith.Curtain.utils;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class Tracer {

    public static MovingObjectPosition rayTrace(EntityPlayer source, float partialTicks, double reach, boolean fluids) {
        MovingObjectPosition blockHit = rayTraceBlocks(source, partialTicks, reach, fluids);
        double maxSqDist = reach * reach;
        Vec3 eye = getEyePosition(source, partialTicks);
        if (blockHit != null) {
            maxSqDist = blockHit.hitVec.squareDistanceTo(eye);
        }
        MovingObjectPosition entityHit = rayTraceEntities(source, partialTicks, reach, maxSqDist);
        return entityHit == null ? blockHit : entityHit;
    }

    public static Vec3 getEyePosition(Entity source, float partialTicks) {
        if (partialTicks == 1.0F) {
            return Vec3.createVectorHelper(source.posX, source.posY + source.getEyeHeight(), source.posZ);
        }
        double x = source.prevPosX + (source.posX - source.prevPosX) * partialTicks;
        double y = source.prevPosY + (source.posY - source.prevPosY) * partialTicks + source.getEyeHeight();
        double z = source.prevPosZ + (source.posZ - source.prevPosZ) * partialTicks;
        return Vec3.createVectorHelper(x, y, z);
    }

    /** 1.7.10 has no Entity#getLook, derive it from the interpolated rotations. */
    public static Vec3 getLook(Entity source, float partialTicks) {
        float yaw;
        float pitch;
        if (partialTicks == 1.0F) {
            yaw = source.rotationYaw;
            pitch = source.rotationPitch;
        } else {
            yaw = source.prevRotationYaw + (source.rotationYaw - source.prevRotationYaw) * partialTicks;
            pitch = source.prevRotationPitch + (source.rotationPitch - source.prevRotationPitch) * partialTicks;
        }
        float f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = -MathHelper.cos(-pitch * 0.017453292F);
        float f4 = MathHelper.sin(-pitch * 0.017453292F);
        return Vec3.createVectorHelper(f2 * f3, f4, f1 * f3);
    }

    public static MovingObjectPosition rayTraceBlocks(Entity source, float partialTicks, double reach, boolean fluids) {
        Vec3 pos = getEyePosition(source, partialTicks);
        Vec3 look = getLook(source, partialTicks);
        Vec3 end = pos.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        return source.worldObj.func_147447_a(pos, end, fluids, false, true);
    }

    public static MovingObjectPosition rayTraceEntities(Entity source, float partialTicks, double reach,
        double maxSqDist) {
        Vec3 pos = getEyePosition(source, partialTicks);
        Vec3 look = getLook(source, partialTicks);
        Vec3 end = pos.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        AxisAlignedBB box = source.boundingBox.addCoord(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach)
            .expand(1.0D, 1.0D, 1.0D);
        return rayTraceEntities(source, pos, end, box, maxSqDist);
    }

    public static MovingObjectPosition rayTraceEntities(Entity source, Vec3 start, Vec3 end, AxisAlignedBB box,
        double maxSqDistance) {
        double targetDistance = maxSqDistance;
        Entity target = null;
        Vec3 targetHitPos = null;
        List<?> candidates = source.worldObj.getEntitiesWithinAABBExcludingEntity(source, box);
        for (Object o : candidates) {
            if (!(o instanceof Entity)) continue;
            Entity current = (Entity) o;
            if (!current.canBeCollidedWith()) continue;
            AxisAlignedBB currentBox = current.boundingBox.expand(0.3D, 0.3D, 0.3D);
            MovingObjectPosition intercept = currentBox.calculateIntercept(start, end);
            Vec3 currentHit = intercept == null ? null : intercept.hitVec;
            if (currentBox.isVecInside(start)) {
                if (targetDistance >= 0) {
                    target = current;
                    targetHitPos = currentHit == null ? start : currentHit;
                    targetDistance = 0;
                }
            } else if (currentHit != null) {
                double currentDistance = start.squareDistanceTo(currentHit);
                if (currentDistance < targetDistance || targetDistance == 0) {
                    if (current.ridingEntity != null && current.ridingEntity == source.ridingEntity) {
                        if (targetDistance == 0) {
                            target = current;
                            targetHitPos = currentHit;
                        }
                    } else {
                        target = current;
                        targetHitPos = currentHit;
                        targetDistance = currentDistance;
                    }
                }
            }
        }
        if (target == null) return null;
        return new MovingObjectPosition(target, targetHitPos);
    }
}
