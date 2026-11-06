package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.consume.DeathConsumeEffect;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

public class TotemOfDying {
    public static void tryUseTotem(DamageSource damage, LivingEntity entity) {
        ItemStack totem = getTotem(entity, true);

        if (!totem.isEmpty()) {
            if (entity instanceof ServerPlayer player) {
                player.awardStat(Stats.ITEM_USED.get(totem.getItem()));
                CriteriaTriggers.USED_TOTEM.trigger(player, totem);
                player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }

            for (var effect : totem.get(UDataComponentTypes.DEATH_CAUSING).deathEffects()) {
                if (effect instanceof DeathConsumeEffect o) {
                    o.onConsume(entity.level(), totem, entity, damage);
                }
            }
        }
    }

    public static ItemStack getTotem(LivingEntity entity, boolean consume) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = entity.getItemInHand(hand);
            if (stack.has(UDataComponentTypes.DEATH_CAUSING)) {
                return consume ? stack.split(1) : stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
