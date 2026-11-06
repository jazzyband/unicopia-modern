package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.Unicopia;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;

public interface UItemPredicates {

    private static <P extends SelectItemModelProperty<T>, T> void option(String name, SelectItemModelProperty.Type<P, T> type) {
        SelectItemModelProperties.ID_MAPPER.put(Unicopia.id(name), type);
    }

    private static void range(String name, MapCodec<? extends RangeSelectItemModelProperty> codec) {
        RangeSelectItemModelProperties.ID_MAPPER.put(Unicopia.id(name), codec);
    }

    private static void tint(String name, MapCodec<? extends ItemTintSource> codec) {
        ItemTintSources.ID_MAPPER.put(Unicopia.id(name), codec);
    }

    private static void specialModel(String name, MapCodec<? extends SpecialModelRenderer.Unbaked<?>> codec) {
        SpecialModelRenderers.ID_MAPPER.put(Unicopia.id(name), codec);
    }

    static void bootstrap() {
        option("gem_shape", GemShapeProperty.TYPE);
        option("balloon_design", BalloonDesignProperty.TYPE);
        option("butterfly_variant", ButterflyVariantProperty.TYPE);
        option("affinity", AffinityProperty.TYPE);

        range("zap_apple_cycle", ZapAppleCycleProperty.CODEC);

        tint("spell", SpellTintSource.CODEC);
        tint("block", BlockTintSource.CODEC);

        specialModel("cloud_bed", CloudBedModelRenderer.Unbaked.CODEC);
        specialModel("cloud_chest", CloudChestModelRenderer.Unbaked.CODEC);
        specialModel("jar_contents", JarContentsModelRenderer.Unbaked.CODEC);
        specialModel("polearm", PolearmModelRenderer.Unbaked.CODEC);
        /* polearms ---> */ // UsingItemProperty.CODEC
    }
}
