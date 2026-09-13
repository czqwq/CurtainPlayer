package com.Lilith.Curtain.mixins;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Lilith.Curtain.CurtainRules;
import com.Lilith.Curtain.features.logging.helper.TNTLogHelper;
import com.Lilith.Curtain.features.rules.fakes.TntEntityInterface;

@Mixin(EntityTNTPrimed.class)
public abstract class EntityTNTPrimedMixin extends Entity implements TntEntityInterface {

    @Shadow
    public int fuse;

    @Shadow
    private EntityLivingBase tntPlacedBy;

    private TNTLogHelper curtain$logHelper;
    private boolean curtain$mergeBool = false;
    private int curtain$mergedTNT = 1;

    public EntityTNTPrimedMixin(World world) {
        super(world);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void curtain$initLogger(World world, CallbackInfo ci) {
        if (!world.isRemote) {
            this.curtain$logHelper = new TNTLogHelper();
        }
    }

    @Inject(
        method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/EntityLivingBase;)V",
        at = @At("RETURN"))
    private void curtain$modifyTntAngle(World world, double x, double y, double z, EntityLivingBase placer,
        CallbackInfo ci) {
        if (CurtainRules.hardcodeTNTAngle != -1.0D) {
            this.motionX = -Math.sin(CurtainRules.hardcodeTNTAngle) * 0.02;
            this.motionY = 0.2;
            this.motionZ = -Math.cos(CurtainRules.hardcodeTNTAngle) * 0.02;
        }
        if (CurtainRules.tntPrimerMomentumRemoved) {
            this.motionX = 0;
            this.motionY = 0.20000000298023224D;
            this.motionZ = 0;
        }
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void curtain$initTracker(CallbackInfo ci) {
        if (this.curtain$logHelper != null && !this.curtain$logHelper.initialized) {
            this.curtain$logHelper.onPrimed(this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ);
        }
    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void curtain$tryMergeTnt(CallbackInfo ci) {
        if (!CurtainRules.mergeTNT) return;
        if (this.worldObj.isRemote) return;
        if (this.motionX != 0 || this.motionY != 0 || this.motionZ != 0) {
            this.curtain$mergeBool = true;
            return;
        }
        if (!this.curtain$mergeBool) return;
        this.curtain$mergeBool = false;
        @SuppressWarnings("unchecked")
        List<?> entities = this.worldObj.getEntitiesWithinAABB(EntityTNTPrimed.class, this.boundingBox);
        for (Object o : entities) {
            Entity entity = (Entity) o;
            if (entity == this || entity.isDead) continue;
            EntityTNTPrimed other = (EntityTNTPrimed) entity;
            if (other.motionX == 0 && other.motionY == 0
                && other.motionZ == 0
                && this.posX == other.posX
                && this.posY == other.posY
                && this.posZ == other.posZ
                && this.fuse == other.fuse) {
                this.curtain$mergedTNT += ((TntEntityInterface) other).getMergedTNT();
                other.setDead();
            }
        }
    }

    @Inject(method = "explode", at = @At("HEAD"))
    private void curtain$onExplode(CallbackInfo ci) {
        if (this.curtain$logHelper != null) {
            this.curtain$logHelper.onExploded(this.posX, this.posY, this.posZ, this.worldObj.getTotalWorldTime());
        }
        if (this.curtain$mergedTNT > 1) {
            for (int i = 0; i < this.curtain$mergedTNT - 1; i++) {
                this.worldObj.createExplosion(
                    this,
                    this.posX,
                    this.posY + (double) (this.height / 16.0F),
                    this.posZ,
                    4.0F,
                    true);
            }
        }
    }

    @Override
    public int getMergedTNT() {
        return this.curtain$mergedTNT;
    }

    @SuppressWarnings("unused")
    private EntityLivingBase placedBy() {
        return this.tntPlacedBy;
    }
}
