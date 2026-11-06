package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.GemstoneItem.Shape;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record GemShapeProperty() implements SelectItemModelProperty<Shape> {
    public static final SelectItemModelProperty.Type<GemShapeProperty, Shape> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit(new GemShapeProperty()), Shape.CODEC);

    @Override
    public Shape get(ItemStack stack, ClientLevel world, LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return EnchantableItem.getSpellKey(stack).getGemShape();
    }

    @Override
    public Codec<Shape> valueCodec() {
        return Shape.CODEC;
    }

    @Override
    public Type<? extends SelectItemModelProperty<Shape>, Shape> type() {
        return TYPE;
    }
}
