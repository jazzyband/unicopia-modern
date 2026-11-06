package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.NeedleDirectionHelper;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

public class ZapAppleCycleProperty extends NeedleDirectionHelper implements RangeSelectItemModelProperty {
    public static final MapCodec<ZapAppleCycleProperty> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.BOOL.optionalFieldOf("wobble", true).forGetter(ZapAppleCycleProperty::wobble)
    ).apply(i, ZapAppleCycleProperty::new));
    private final NeedleDirectionHelper.Wobbler angler;

    public ZapAppleCycleProperty(boolean wobble) {
        super(wobble);
        angler = newWobbler(0.9F);
    }

    @Override
    protected float calculate(ItemStack stack, ClientLevel world, int seed, ItemOwner user) {
        float angle = UnicopiaClient.getInstance().getZapAppleStage().getCycleProgress(world);
        long time = world.getGameTime();
        if (angler.shouldUpdate(time)) {
            angler.update(time, angle);
        }

        return angler.rotation();
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return CODEC;
    }

}
