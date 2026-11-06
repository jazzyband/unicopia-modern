package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ButterfingersStatusEffect extends MobEffect {

    ButterfingersStatusEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        amplifier = Mth.clamp(amplifier, 0, 5);
        final int scale = 500 + (int)(((5 - amplifier) / 5F) * 900);

        if (entity.level().getRandom().nextInt(scale / 4) == 0) {
            applyInstantaneousEffect(world, null, null, entity, amplifier, entity.level().getRandom().nextInt(scale));
        }
        return true;
    }

    @Override
    public void applyInstantaneousEffect(ServerLevel world, @Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {

        if (target.level().isClientSide()) {
            return;
        }

        if (target instanceof ServerPlayer player) {
            if (!player.getInventory().getSelectedItem().isEmpty()) {
                player.drop(proximity < 1);
                player.level().playSound(null, player.blockPosition(), USounds.ENTITY_GENERIC_BUTTER_FINGERS, player.getSoundSource());
                Inventory inventory = player.getInventory();
                player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, inventory.getSelectedSlot(), inventory.getSelectedItem()));
            }
        } else {
            ItemStack stack = target.getMainHandItem();
            if (!stack.isEmpty()) {
                target.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                target.spawnAtLocation(world, stack);
                target.level().playSound(null, target.blockPosition(), USounds.ENTITY_GENERIC_BUTTER_FINGERS, target.getSoundSource());
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0;
    }
}
