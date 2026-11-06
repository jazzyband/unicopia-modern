package com.minelittlepony.unicopia.item.component;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;

public record MimicComponent(boolean mimic) {
    public static final Codec<MimicComponent> CODEC = Codec.BOOL.xmap(MimicComponent::new, MimicComponent::mimic);
    public static final StreamCodec<ByteBuf, MimicComponent> PACKET_CODEC = ByteBufCodecs.BOOL.map(MimicComponent::new, MimicComponent::mimic);
    private static final CompoundTag TEMPLATE_NBT = Util.make(new CompoundTag(), nbt -> nbt.putBoolean("mimic", true));

    public static void appendTooltip(ItemStack stack, TooltipContext context, Consumer<Component> tooltip, TooltipFlag type) {
        if (isMimic(stack)) {
            tooltip.accept(Component.translatable("component.unicopia.mimic.is_mimic").withStyle(ChatFormatting.RED));
        }
    }

    static boolean isMimic(ItemStack stack) {
        @Nullable MimicComponent mimic = stack.get(UDataComponentTypes.MIMIC);
        if (mimic != null) {
            return mimic.mimic();
        }
        var nbt = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        return nbt != null && nbt.copyTagWithoutId().getBoolean("mimic").orElse(false);
    }
}
