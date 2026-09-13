package com.Lilith.Curtain.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.Lilith.Curtain.CurtainRules;

/**
 * 1.7.10 flavoured block rotator. 1.7.10 stores the orientation in the block metadata instead of
 * block states, so every supported block type has its own small rotation table.
 */
public class BlockRotator {

    public static boolean flipBlockWithCactus(Block block, World world, int x, int y, int z, EntityPlayer player,
        int side, float hitX, float hitY, float hitZ) {
        if (player == null || !player.capabilities.allowEdit
            || !CurtainRules.flippingCactus
            || !playerHoldsCactusMainhand(player)) {
            return false;
        }
        CurtainRules.impendingFillSkipUpdates.set(true);
        boolean retval = flipBlock(block, world, x, y, z, player, side, hitX, hitY, hitZ);
        CurtainRules.impendingFillSkipUpdates.set(false);
        return retval;
    }

    public static boolean flipBlock(Block block, World world, int x, int y, int z, EntityPlayer player, int side,
        float hitX, float hitY, float hitZ) {
        int meta = world.getBlockMetadata(x, y, z);
        int newMeta = rotateMeta(block, meta, side, hitX, hitY, hitZ);
        if (newMeta < 0 || newMeta == meta) return false;
        int flags = CurtainRules.interactionUpdates ? 3 : 2;
        world.setBlock(x, y, z, block, newMeta, flags);
        if (!CurtainRules.interactionUpdates) {
            world.markBlockForUpdate(x, y, z);
        }
        return true;
    }

    /** @return the rotated metadata, or -1 when the block is not rotatable */
    public static int rotateMeta(Block block, int meta, int side, float hitX, float hitY, float hitZ) {
        if (block instanceof BlockStairs) {
            // bits 0-1 facing, bit 3 upside down
            return (meta & 12) | ((meta + 1) & 3);
        }
        if (block instanceof BlockLog) {
            int axis = meta & 12;
            int newAxis = axis == 0 ? 4 : (axis == 4 ? 8 : 0);
            return (meta & 3) | newAxis;
        }
        if (block instanceof BlockFenceGate) {
            return (meta & 12) | ((meta + 1) & 3);
        }
        if (block instanceof BlockHopper) {
            return rotateOrientation(meta);
        }
        if (block instanceof BlockDispenser) {
            return rotateOrientation(meta);
        }
        if (block instanceof BlockPistonBase) {
            if ((meta & 8) != 0) return -1;
            return meta ^ 1;
        }
        if (block instanceof BlockSlab) {
            return meta ^ 8;
        }
        if (block instanceof BlockRailBase) {
            switch (meta) {
                case 0:
                    return 1;
                case 1:
                    return 0;
                case 2:
                    return 5;
                case 3:
                    return 4;
                case 4:
                    return 3;
                case 5:
                    return 2;
                default:
                    return -1;
            }
        }
        return -1;
    }

    /** Rotates the 1.7.10 orientation metadata (2..5) clockwise. */
    private static int rotateOrientation(int meta) {
        switch (meta) {
            case 2:
                return 5;
            case 5:
                return 3;
            case 3:
                return 4;
            case 4:
                return 2;
            default:
                return -1;
        }
    }

    public static ItemStack dispenserRotate(IBlockSource source, ItemStack stack) {
        World world = source.getWorld();
        int x = source.getXInt();
        int y = source.getYInt();
        int z = source.getZInt();
        if (!(world.getBlock(x, y, z) instanceof BlockDispenser)) return stack;
        net.minecraft.util.EnumFacing dir = BlockDispenser.func_149937_b(source.getBlockMetadata());
        int tx = x + dir.getFrontOffsetX();
        int ty = y + dir.getFrontOffsetY();
        int tz = z + dir.getFrontOffsetZ();
        Block block = world.getBlock(tx, ty, tz);
        if (block == null || block.getMaterial() == net.minecraft.block.material.Material.air) return stack;
        int tmeta = world.getBlockMetadata(tx, ty, tz);
        int newMeta = rotateMeta(
            block,
            tmeta,
            net.minecraft.util.EnumFacing.getFront(dir.ordinal() ^ 1)
                .ordinal(),
            0.5F,
            0.5F,
            0.5F);
        if (newMeta >= 0 && newMeta != tmeta) {
            world.setBlock(tx, ty, tz, block, newMeta, 3);
            world.notifyBlockChange(tx, ty, tz, block);
        }
        return stack;
    }

    private static boolean playerHoldsCactusMainhand(EntityPlayer playerIn) {
        ItemStack stack = playerIn.getCurrentEquippedItem();
        return stack != null
            && stack.getItem() == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.cactus);
    }

    public static boolean flippinEligibility(Entity entity) {
        return CurtainRules.flippingCactus && entity instanceof EntityPlayer
            && playerHoldsCactusMainhand((EntityPlayer) entity);
    }

    public static class CactusDispenserBehaviour implements IBehaviorDispenseItem {

        private final BehaviorDefaultDispenseItem fallback = new BehaviorDefaultDispenseItem();

        @Override
        public ItemStack dispense(IBlockSource source, ItemStack stack) {
            if (CurtainRules.rotatorBlock) {
                return BlockRotator.dispenserRotate(source, stack);
            }
            return this.fallback.dispense(source, stack);
        }
    }
}
