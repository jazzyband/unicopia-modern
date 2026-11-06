package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.minelittlepony.unicopia.item.component.BufferflyVariantComponent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record ButterflyVariantProperty() implements SelectItemModelProperty<ButterflyEntity.Variant> {
    public static final ButterflyVariantProperty INSTANCE = new ButterflyVariantProperty();
    public static final SelectItemModelProperty.Type<ButterflyVariantProperty, ButterflyEntity.Variant> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit(INSTANCE), ButterflyEntity.Variant.CODEC);

    @Override
    public ButterflyEntity.Variant get(ItemStack stack, ClientLevel world, LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return BufferflyVariantComponent.get(stack).variant();
    }

    @Override
    public Codec<ButterflyEntity.Variant> valueCodec() {
        return ButterflyEntity.Variant.CODEC;
    }

    @Override
    public Type<? extends SelectItemModelProperty<ButterflyEntity.Variant>, ButterflyEntity.Variant> type() {
        return TYPE;
    }
}
