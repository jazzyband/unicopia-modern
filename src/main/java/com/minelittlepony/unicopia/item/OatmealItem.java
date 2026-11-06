package com.minelittlepony.unicopia.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class OatmealItem extends Item {

    public OatmealItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide()) {
            user.removeAllEffects();
        }
        return super.finishUsingItem(stack, world, user);
    }
}
