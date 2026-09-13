package com.Lilith.Curtain.mixins.rules.optimized_tnt;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Lilith.Curtain.CurtainRules;
import com.Lilith.Curtain.features.logging.helper.ExplosionLogHelper;
import com.Lilith.Curtain.utils.OptimizedExplosion;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow
    public List<ChunkPosition> affectedBlockPositions;

    private ExplosionLogHelper curtain$eLogger;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void curtain$onExplosionCreated(World world, Entity source, double x, double y, double z, float size,
        CallbackInfo ci) {
        if (!world.isRemote) {
            this.curtain$eLogger = new ExplosionLogHelper(x, y, z, size, ((Explosion) (Object) this).isFlaming);
        }
    }

    @Inject(method = "doExplosionA", at = @At("HEAD"), cancellable = true)
    private void curtain$onExplosionA(CallbackInfo ci) {
        if (CurtainRules.optimizedTNT) {
            OptimizedExplosion.doExplosionA((Explosion) (Object) this, this.curtain$eLogger);
            ci.cancel();
        }
    }

    @Inject(method = "doExplosionB", at = @At("HEAD"), cancellable = true)
    private void curtain$onExplosionB(boolean spawnParticles, CallbackInfo ci) {
        if (this.curtain$eLogger != null) {
            this.curtain$eLogger.setAffectBlocks(!this.affectedBlockPositions.isEmpty());
            this.curtain$eLogger.onExplosionDone(
                ((com.Lilith.Curtain.mixins.ExplosionAccessor) (Object) this).getWorld()
                    .getTotalWorldTime());
        }
        if (CurtainRules.explosionNoBlockDamage) {
            this.affectedBlockPositions.clear();
        }
        if (CurtainRules.optimizedTNT) {
            OptimizedExplosion.doExplosionB((Explosion) (Object) this, spawnParticles);
            ci.cancel();
        }
    }

    @Redirect(
        method = "doExplosionA",
        require = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"))
    private Block curtain$noBlockCalcsWithNoBlockDamage(World world, int x, int y, int z) {
        if (CurtainRules.explosionNoBlockDamage) {
            return Blocks.bedrock;
        }
        return world.getBlock(x, y, z);
    }
}
