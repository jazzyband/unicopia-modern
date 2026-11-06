package com.minelittlepony.unicopia.item;

import java.util.Optional;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;

public class ConsumableItem extends Item {

    private final ItemUseAnimation action;

    public ConsumableItem(Item.Properties settings, ItemUseAnimation action) {
        super(settings);
        this.action = action;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        super.finishUsingItem(stack, world, user);
        if (user instanceof ServerPlayer) {
            ServerPlayer serverPlayerEntity = (ServerPlayer)user;
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
            serverPlayerEntity.awardStat(Stats.ITEM_USED.get(this));
        }

        if (stack.isEmpty()) {
            return getCraftingRemainder() == null ? ItemStack.EMPTY : getCraftingRemainder().create();
        }

        if (user instanceof Player player) {
            return Optional.ofNullable(getCraftingRemainder()).map(remainder -> {
                return ItemUtils.createFilledResult(stack, player, remainder.create());
            }).orElse(stack);
        }
        return stack;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return action;
    }
}
