package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record AffinityProperty() implements SelectItemModelProperty<Affinity> {
    public static final SelectItemModelProperty.Type<AffinityProperty, Affinity> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit(new AffinityProperty()), Affinity.CODEC);

    @Override
    public Affinity get(ItemStack stack, ClientLevel world, LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return EnchantableItem.getSpellKey(stack).getAffinity();
    }

    @Override
    public Codec<Affinity> valueCodec() {
        return Affinity.CODEC;
    }

    @Override
    public Type<? extends SelectItemModelProperty<Affinity>, Affinity> type() {
        return TYPE;
    }
}
