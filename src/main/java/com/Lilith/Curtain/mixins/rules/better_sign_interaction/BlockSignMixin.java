package com.Lilith.Curtain.mixins.rules.better_sign_interaction;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSign;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Facing;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.Lilith.Curtain.CurtainRules;

/**
 * 1.7.10 wall signs cannot be edited, so right clicking one is forwarded to the block behind it.
 * The method is added to the target class and therefore takes part in normal virtual dispatch.
 */
@Mixin(BlockSign.class)
public abstract class BlockSignMixin extends Block {

    @Shadow
    private boolean field_149967_b;

    protected BlockSignMixin(Material material) {
        super(material);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (CurtainRules.betterSignInteraction && this.field_149967_b) {
            int meta = world.getBlockMetadata(x, y, z);
            if (meta >= 2 && meta <= 5) {
                int bx = x - Facing.offsetsXForSide[meta];
                int bz = z - Facing.offsetsZForSide[meta];
                Block block = world.getBlock(bx, y, bz);
                if (block != null && !(block instanceof BlockSign) && block.getMaterial() != Material.air) {
                    if (block.onBlockActivated(world, bx, y, bz, player, side, hitX, hitY, hitZ)) {
                        return true;
                    }
                }
            }
        }
        return super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
    }
}
