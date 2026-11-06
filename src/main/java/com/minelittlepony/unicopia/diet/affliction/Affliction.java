package com.minelittlepony.unicopia.diet.affliction;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface Affliction {
    void afflict(Player player, ItemStack stack);

    default boolean isEmpty() {
        return getType() == AfflictionType.EMPTY;
    }

    default void appendTooltip(Consumer<Component> tooltip) {
        tooltip.accept(Component.literal(" ").append(getName()).withStyle(ChatFormatting.DARK_GRAY));
    }

    default Component getName() {
        return Component.translatable(getType().getTranslationKey());
    }

    AfflictionType<?> getType();
}
