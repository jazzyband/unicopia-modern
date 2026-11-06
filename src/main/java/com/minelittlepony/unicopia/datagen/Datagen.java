package com.minelittlepony.unicopia.datagen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.unicopia.datagen.providers.DietsProvider;
import com.minelittlepony.unicopia.datagen.providers.SeasonsGrowthRatesProvider;
import com.minelittlepony.unicopia.datagen.providers.SpellbookChapterProvider;
import com.minelittlepony.unicopia.datagen.providers.StateMapProvider;
import com.minelittlepony.unicopia.datagen.providers.TreeTypeProvider;
import com.minelittlepony.unicopia.datagen.providers.UAdvancementsProvider;
import com.minelittlepony.unicopia.datagen.providers.UDynamicRegistriesProvider;
import com.minelittlepony.unicopia.datagen.providers.UEnchantmentProvider;
import com.minelittlepony.unicopia.datagen.providers.UJukeboxSongProvider;
import com.minelittlepony.unicopia.datagen.providers.UModelProvider;
import com.minelittlepony.unicopia.datagen.providers.UPaintingVariantProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UBlockAdditionsLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UBlockLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UChestAdditionsLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UChestLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UEntityAdditionsLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UEntityLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.recipe.URecipeProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UBiomeTagProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UBlockTagProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UDamageTypeProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UDimensionTypeTagProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UEntityTypeTagProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UItemTagProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UStatusEffectTagProvider;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageType;

public class Datagen implements DataGeneratorEntrypoint {
    public static final Logger LOGGER = LogManager.getLogger();

    private final UPaintingVariantProvider paintingVariants = new UPaintingVariantProvider();
    private final UEnchantmentProvider enchantments = new UEnchantmentProvider();

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        final var pack = fabricDataGenerator.createPack();
        final var blockTags = pack.addProvider(UBlockTagProvider::new);
        final var itemTags = pack.addProvider((output, registries) -> new UItemTagProvider(output, registries, blockTags));
        pack.addProvider((output, registries) -> new DietsProvider(output, itemTags));
        pack.addProvider(UDamageTypeProvider::new);
        pack.addProvider(UEntityTypeTagProvider::new);
        pack.addProvider(UStatusEffectTagProvider::new);
        pack.addProvider(UDimensionTypeTagProvider::new);
        pack.addProvider(UBiomeTagProvider::new);

        paintingVariants.addToPack(pack);
        enchantments.addToPack(pack);

        pack.addProvider(UJukeboxSongProvider::new);
        pack.addProvider(UModelProvider::new);
        pack.addProvider(UDynamicRegistriesProvider::new);
        final var recipeProvider = pack.addProvider(URecipeProvider::new);
        pack.addProvider((output, registries) -> new SpellbookChapterProvider(output, registries, recipeProvider));
        pack.addProvider(UBlockLootTableProvider::new);
        pack.addProvider(UEntityLootTableProvider::new);
        pack.addProvider(UEntityAdditionsLootTableProvider::new);
        pack.addProvider(UChestLootTableProvider::new);
        pack.addProvider(UBlockAdditionsLootTableProvider::new);
        pack.addProvider(UChestAdditionsLootTableProvider::new);
        pack.addProvider(SeasonsGrowthRatesProvider::new);
        pack.addProvider(TreeTypeProvider::new);
        pack.addProvider(StateMapProvider::new);
        pack.addProvider(UAdvancementsProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder) {
        builder.add(Registries.DAMAGE_TYPE, registerable -> {
            UDamageTypes.REGISTRY.forEach(key -> registerable.register(key, new DamageType(key.identifier().getNamespace() + "." + key.identifier().getPath(), 0)));
        });
        builder.add(Registries.FEATURE, registerable -> UWorldGenFeatures.bootstrapConfiguredFeatures(registerable));
        builder.add(Registries.PLACED_FEATURE, UWorldGenFeatures::bootstrapPlacedFeatures);
        builder.add(Registries.BIOME, UWorldGenFeatures::bootstrapBiomes);
        builder.add(Registries.PAINTING_VARIANT, paintingVariants);
        builder.add(Registries.ENCHANTMENT, enchantments);
        builder.add(Registries.TEMPLATE_POOL, CustomStructurePools::bootstrapPools);
        builder.add(Registries.PROCESSOR_LIST, CustomStructurePools::bootstrapProcessors);
    }
}
