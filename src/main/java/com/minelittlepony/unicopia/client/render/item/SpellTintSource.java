package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.item.EnchantableItem;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record SpellTintSource(int defaultColor) implements ItemTintSource {
    public static final MapCodec<SpellTintSource> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(SpellTintSource::defaultColor)).apply(instance, SpellTintSource::new)
    );

    @Override
    public int calculate(ItemStack stack, ClientLevel world, LivingEntity user) {
        return !EnchantableItem.isEnchanted(stack) ? defaultColor : EnchantableItem.getSpellKey(stack).getColor() | 0xFF000000;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
