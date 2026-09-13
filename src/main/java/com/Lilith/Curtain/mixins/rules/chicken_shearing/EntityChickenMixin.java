package com.Lilith.Curtain.mixins.rules.chicken_shearing;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import org.spongepowered.asm.mixin.Mixin;

import com.Lilith.Curtain.CurtainRules;

@Mixin(EntityChicken.class)
public abstract class EntityChickenMixin extends EntityAnimal {

    public EntityChickenMixin(net.minecraft.world.World world) {
        super(world);
    }

    @Override
    public boolean interact(EntityPlayer player) {
        ItemStack stack = player.getCurrentEquippedItem();
        if (CurtainRules.chickenShearing && stack != null && stack.getItem() == Items.shears && !this.isChild()) {
            if (this.attackEntityFrom(DamageSource.generic, 1.0F)) {
                this.dropItem(Items.feather, 1);
                stack.damageItem(1, player);
                return true;
            }
        }
        return super.interact(player);
    }
}
