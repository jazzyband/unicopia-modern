package com.minelittlepony.unicopia.diet;

import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FoodAttributes(FoodProperties food, Optional<Consumable> consumable) {
    public static final float SNACK_SECONDS = 0.8F;
    public static final float NORMAL_SECONDS = 1.6F;

    public static final Codec<FoodAttributes> CODEC = RecordCodecBuilder.create(i -> i.group(
            FoodProperties.DIRECT_CODEC.fieldOf("food").forGetter(FoodAttributes::food),
            Consumable.CODEC.optionalFieldOf("consumable").forGetter(FoodAttributes::consumable)
    ).apply(i, FoodAttributes::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodAttributes> PACKET_CODEC = StreamCodec.composite(
            FoodProperties.DIRECT_STREAM_CODEC, FoodAttributes::food,
            ByteBufCodecs.optional(Consumable.STREAM_CODEC), FoodAttributes::consumable,
            FoodAttributes::new
    );

    public Consumable getConsumableComponent(@Nullable Consumable existing) {
        return consumable.orElse(MoreObjects.firstNonNull(existing, Consumables.DEFAULT_FOOD));
    }
}
