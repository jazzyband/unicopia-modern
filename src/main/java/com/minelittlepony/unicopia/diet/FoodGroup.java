package com.minelittlepony.unicopia.diet;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import com.mojang.serialization.Codec;

public record FoodGroup(
        Identifier id,
        FoodGroupEffects attributes) implements Effect {
    public static final Codec<FoodGroupEffects> EFFECTS_CODEC = FoodGroupEffects.createCodec(FoodGroupKey.TAG_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodGroup> PACKET_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, FoodGroup::id,
            FoodGroupEffects.createPacketCodec(FoodGroupKey.TAG_PACKET_CODEC), FoodGroup::attributes,
            FoodGroup::new
    );

    @Override
    public List<FoodGroupKey> tags() {
        return attributes.tags();
    }

    @Override
    public Optional<FoodAttributes> foodAttributes() {
        return attributes.foodAttributes();
    }

    @Override
    public Ailment ailment() {
        return attributes.ailment();
    }
    @Override
    public void appendTooltip(ItemStack stack, Consumer<Component> tooltip, TooltipFlag context) {
        tooltip.accept(Component.literal(" ").append(Component.translatable(Util.makeDescriptionId("food_group", id()))).withStyle(ChatFormatting.GRAY));
        Effect.super.appendTooltip(stack, tooltip, context);
    }
}
