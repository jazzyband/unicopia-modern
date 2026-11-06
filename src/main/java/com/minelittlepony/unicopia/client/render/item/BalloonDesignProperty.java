package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.item.component.BalloonDesignComponent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record BalloonDesignProperty() implements SelectItemModelProperty<AirBalloonEntity.BalloonDesign> {
    public static final BalloonDesignProperty INSTANCE = new BalloonDesignProperty();
    public static final SelectItemModelProperty.Type<BalloonDesignProperty, AirBalloonEntity.BalloonDesign> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit(INSTANCE), AirBalloonEntity.BalloonDesign.CODEC);

    @Override
    public AirBalloonEntity.BalloonDesign get(ItemStack stack, ClientLevel world, LivingEntity user, int seed, ItemDisplayContext displayContext) {
        return BalloonDesignComponent.get(stack).design();
    }

    @Override
    public Codec<AirBalloonEntity.BalloonDesign> valueCodec() {
        return AirBalloonEntity.BalloonDesign.CODEC;
    }

    @Override
    public Type<? extends SelectItemModelProperty<AirBalloonEntity.BalloonDesign>, AirBalloonEntity.BalloonDesign> type() {
        return TYPE;
    }
}
