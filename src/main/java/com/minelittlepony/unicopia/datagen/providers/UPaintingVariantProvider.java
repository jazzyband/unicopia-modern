package com.minelittlepony.unicopia.datagen.providers;

import java.util.Optional;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.datagen.DataGenRegistryProvider;

public class UPaintingVariantProvider extends DataGenRegistryProvider<PaintingVariant> {
    public UPaintingVariantProvider() {
        super(Registries.PAINTING_VARIANT);
    }

    @Override
    public void run(BootstrapContext<PaintingVariant> registerable) {
        register(registerable, "bloom", 2, 1);
        register(registerable, "chicken", 2, 1);
        register(registerable, "bells", 2, 1);

        register(registerable, "crystal", 3, 3);
        register(registerable, "harmony", 3, 3);

        register(registerable, "equality", 2, 4);
        register(registerable, "solar", 2, 4);
        register(registerable, "lunar", 2, 4);
        register(registerable, "platinum", 2, 4);
        register(registerable, "hurricane", 2, 4);
        register(registerable, "pudding", 2, 4);
        register(registerable, "terra", 2, 4);
        register(registerable, "equestria", 2, 4);

        register(registerable, "blossom", 2, 3);
        register(registerable, "shadow", 2, 3);
    }

    private void register(BootstrapContext<PaintingVariant> registerable, String name, int width, int height) {
        ResourceKey<PaintingVariant> key = ResourceKey.create(Registries.PAINTING_VARIANT, Unicopia.id(name));
        registerable.register(key, new PaintingVariant(
                width, height,
                key.identifier(),
                Optional.of(Component.translatable(key.identifier().toLanguageKey("painting", "title"))),
                Optional.of(Component.translatable(key.identifier().toLanguageKey("painting", "author")))
        ));
    }

    @Override
    protected void configureTags(TagProvider tagProvider, Provider lookup) {
        tagProvider.tag(PaintingVariantTags.PLACEABLE).addAll(getKeys());
    }
}
