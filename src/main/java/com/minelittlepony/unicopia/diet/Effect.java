package com.minelittlepony.unicopia.diet;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;

public interface Effect extends Predicate<ItemStack> {
    Effect EMPTY = new FoodGroupEffects(List.of(), Optional.empty(), Ailment.EMPTY);

    List<FoodGroupKey> tags();

    Optional<FoodAttributes> foodAttributes();

    Ailment ailment();

    default void appendTooltip(ItemStack stack, Consumer<Component> tooltip, TooltipFlag context) {
        if (!test(stack)) {
            if (stack.has(DataComponents.FOOD)) {
                tooltip.accept(Component.literal(" ").append(Component.translatable("food_group.unicopia.misc")).withStyle(ChatFormatting.GRAY));
            } else if (stack.getUseAnimation() == ItemUseAnimation.DRINK) {
                tooltip.accept(Component.literal(" ").append(Component.translatable("food_group.unicopia.drinks")).withStyle(ChatFormatting.GRAY));
            }
        }

        if (context.isAdvanced() && stack.has(DataComponents.FOOD)) {
            if (!ailment().effects().isEmpty()) {
                tooltip.accept(Component.translatable("unicopia.diet.side_effects").withStyle(ChatFormatting.DARK_PURPLE));
                ailment().effects().appendTooltip(tooltip);
            }
        }
    }

    @Override
    default boolean test(ItemStack stack) {
        return tags().stream().anyMatch(tag -> tag.contains(stack));
    }
}
