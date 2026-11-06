package com.minelittlepony.unicopia.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class StagedFoodItem extends Item {

    private final ItemLike eatingRemainder;

    public StagedFoodItem(Properties settings, ItemLike eatingRemainder) {
        super(settings);
        this.eatingRemainder = eatingRemainder;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        Consumable food = stack.get(DataComponents.CONSUMABLE);
        if (food != null) {
            food.onConsume(world, user, stack);
            if (user instanceof Player player && player.getAbilities().instabuild) {
                return stack;
            }

            if (stack.isDamageableItem() && stack.getDamageValue() < stack.getMaxDamage() - 1) {
                stack.hurtAndBreak(1, user, stack == user.getItemInHand(InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                return stack;
            }
            return eatingRemainder.asItem().getDefaultInstance();
        }
        return stack;
    }
}
