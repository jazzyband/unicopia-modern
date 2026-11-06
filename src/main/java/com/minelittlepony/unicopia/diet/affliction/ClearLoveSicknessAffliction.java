package com.minelittlepony.unicopia.diet.affliction;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

public final class ClearLoveSicknessAffliction implements Affliction {
    public static final ClearLoveSicknessAffliction INSTANCE = new ClearLoveSicknessAffliction();
    public static final MapCodec<ClearLoveSicknessAffliction> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, ClearLoveSicknessAffliction> PACKET_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.CURE_LOVE_SICKNESS;
    }

    @Override
    public void afflict(Player player, ItemStack stack) {
        @Nullable
        FoodProperties food = stack.get(DataComponents.FOOD);
        player.heal(food == null ? 1 : food.nutrition());
        if (player.level().isClientSide()) {
            return;
        }
        player.removeEffect(MobEffects.NAUSEA);
        player.removeEffect(UEffects.FOOD_POISONING);
        player.removeEffect(MobEffects.WEAKNESS);
    }
}
