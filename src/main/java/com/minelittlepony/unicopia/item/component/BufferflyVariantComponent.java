package com.minelittlepony.unicopia.item.component;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

public record BufferflyVariantComponent (ButterflyEntity.Variant variant, boolean showInTooltip) implements TooltipProvider {
    public static final BufferflyVariantComponent DEFAULT = new BufferflyVariantComponent(ButterflyEntity.Variant.BUTTERFLY, false);
    public static final Codec<BufferflyVariantComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ButterflyEntity.Variant.CODEC.fieldOf("variant").forGetter(BufferflyVariantComponent::variant),
            Codec.BOOL.fieldOf("show_in_tooltip").forGetter(BufferflyVariantComponent::showInTooltip)
    ).apply(instance, BufferflyVariantComponent::new));
    public static final StreamCodec<ByteBuf, BufferflyVariantComponent> PACKET_CODEC = StreamCodec.composite(
            ButterflyEntity.Variant.PACKET_CODEC, BufferflyVariantComponent::variant,
            ByteBufCodecs.BOOL, BufferflyVariantComponent::showInTooltip,
            BufferflyVariantComponent::new
    );

    public static BufferflyVariantComponent get(ItemStack stack) {
        return stack.getOrDefault(UDataComponentTypes.BUTTERFLY_VARIANT, DEFAULT);
    }

    public static ItemStack set(ItemStack stack, BufferflyVariantComponent variant) {
        stack.set(UDataComponentTypes.BUTTERFLY_VARIANT, variant);
        return stack;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
        if (showInTooltip()) {
            textConsumer.accept(Component.literal(variant().name()).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
