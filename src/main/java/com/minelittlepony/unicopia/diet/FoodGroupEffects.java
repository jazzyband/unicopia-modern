package com.minelittlepony.unicopia.diet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.diet.affliction.Affliction;
import com.minelittlepony.unicopia.item.UFoodComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

public record FoodGroupEffects(
        List<FoodGroupKey> tags,
        Optional<FoodAttributes> foodAttributes,
        Ailment ailment
) implements Effect {
    public static Codec<FoodGroupEffects> createCodec(Codec<FoodGroupKey> keyCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
                keyCodec.listOf().fieldOf("tags").forGetter(FoodGroupEffects::tags),
                FoodAttributes.CODEC.optionalFieldOf("food_attributes").forGetter(FoodGroupEffects::foodAttributes),
                Ailment.CODEC.fieldOf("ailment").forGetter(FoodGroupEffects::ailment)
        ).apply(instance, FoodGroupEffects::new));
    }
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodGroupEffects> createPacketCodec(StreamCodec<ByteBuf, FoodGroupKey> keyCodec) {
        return StreamCodec.composite(
                keyCodec.apply(ByteBufCodecs.list()), FoodGroupEffects::tags,
                ByteBufCodecs.optional(FoodAttributes.PACKET_CODEC), FoodGroupEffects::foodAttributes,
                Ailment.PACKET_CODEC, FoodGroupEffects::ailment,
                FoodGroupEffects::new
        );
    }

    @Override
    public void appendTooltip(ItemStack stack, Consumer<Component> tooltip, TooltipFlag context) {
        tags.forEach(tag -> {
            if (tag.contains(stack)) {
                tooltip.accept(Component.literal(" ").append(Component.translatable(Util.makeDescriptionId("tag", tag.id()))).withStyle(ChatFormatting.GRAY));
            }
        });
        Effect.super.appendTooltip(stack, tooltip, context);
    }

    public static final class Builder {
        private final List<FoodGroupKey> tags = new ArrayList<>();
        private Optional<FoodAttributes> food = Optional.empty();
        private Ailment ailment = Ailment.EMPTY;

        public Builder tag(Identifier tag) {
            return tag(TagKey.create(Registries.ITEM, tag));
        }

        public Builder tag(TagKey<Item> tag) {
            tags.add(FoodGroupKey.TAG_LOOKUP.apply(tag));
            return this;
        }

        public Builder ailment(Affliction affliction) {
            ailment = new Ailment(affliction);
            return this;
        }

        public Builder food(int hunger, float saturation) {
            return food(UFoodComponents.builder(hunger, saturation), null);
        }

        public Builder food(FoodProperties.Builder food) {
            return food(food, null);
        }

        public Builder food(FoodProperties.Builder food, @Nullable Consumable.Builder consumable) {
            return food(food.build(), consumable == null ? null : consumable.build());
        }

        public Builder food(FoodProperties food) {
            return food(food, null);
        }

        public Builder food(FoodProperties food, @Nullable Consumable consumable) {
            this.food = Optional.of(new FoodAttributes(food, Optional.ofNullable(consumable)));
            return this;
        }

        public FoodGroupEffects build() {
            return new FoodGroupEffects(tags, food, ailment);
        }
    }
}
