package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.mixin.datagen.loot.BlockLootSubProviderAccessor;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.EnchantmentPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;


public class UBlockAdditionsLootTableProvider extends FabricBlockLootSubProvider {
    public static final float[] GEMSTONES_FORTUNE_CHANCE = { 0.1F, 0.14285715F, 0.25F, 1F };

    private final CompletableFuture<HolderLookup.Provider> registryLookupFuture;

    public UBlockAdditionsLootTableProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
        registryLookupFuture = registryLookup;
    }

    public LootItemCondition.Builder createNeedsOceanOrBeachCondition() {
        return LocationCheck.checkLocation(LocationPredicate.Builder.inBiome(entryOf(Biomes.OCEAN)))
           .or(LocationCheck.checkLocation(LocationPredicate.Builder.inBiome(entryOf(Biomes.BEACH))));
    }

    public LootItemCondition.Builder createWithoutSilkTouchOrGemFinderCondition() {
        return doesNotHaveSilkTouch().and(createWithGemFinderCondition());
    }

    public LootItemCondition.Builder createWithGemFinderCondition() {
        return MatchTool.toolMatches(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components()
                .partial(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(
            new EnchantmentPredicate(entryOf(UEnchantments.GEM_FINDER), MinMaxBounds.Ints.atLeast(1))
        ))).build()));
    }

    public <T> Holder<T> entryOf(ResourceKey<T> key) {
        return registryLookupFuture.getNow(null).lookupOrThrow(key.registryKey()).getOrThrow(key);
    }

    @Override
    public String getName() {
        return "Block Loot Table Additions";
    }

    @Override
    public void generate() {
        addVanillaDrop(Blocks.STONE, this::gemstoneDrops);
        addVanillaDrop(Blocks.DIRT, block -> gemstoneAndWormDrops(block, 2, 0.05F, 0.052222223F, 0.055F, 0.066666665F, 0.1F));
        addVanillaDrop(Blocks.COARSE_DIRT, block -> gemstoneAndWormDrops(block, 2, 0.05F, 0.052222223F, 0.055F, 0.066666665F, 0.1F));
        addVanillaDrop(Blocks.GRASS_BLOCK, block -> gemstoneAndWormDrops(block, 2, 0.05F, 0.052222223F, 0.055F, 0.066666665F, 0.1F));
        addVanillaDrop(Blocks.SHORT_GRASS, block -> chanceDrop(block, UItems.OAT_SEEDS, 2, 0.05F, 0.052222223F, 0.055F, 0.066666665F, 0.1F));
        addVanillaDrop(Blocks.FERN, block -> chanceDrop(block, UItems.OAT_SEEDS, 2, 0.05F, 0.052222223F, 0.055F, 0.066666665F, 0.1F));
        addVanillaDrop(Blocks.TALL_GRASS, block -> chanceDrop(block, UItems.OAT_SEEDS, 2, 0.05F, 0.052222223F, 0.055F, 0.066666665F, 0.1F));
        addVanillaDrop(Blocks.LARGE_FERN, block -> chanceDrop(block, UItems.OAT_SEEDS, 2, 0.05F, 0.052222223F, 0.055F, 0.066666665F, 0.1F));
        addVanillaDrop(Blocks.MYCELIUM, block -> wormDrops(block, 3, 0.06F, 0.062222223F, 0.065F, 0.077777776F, 0.2F));
        addVanillaDrop(Blocks.PODZOL, block -> wormDrops(block, 4, 0.06F, 0.062222223F, 0.065F, 0.077777776F, 0.2F));
        addVanillaDrop(Blocks.DIAMOND_ORE, this::crystalShardDrops);
        addVanillaDrop(Blocks.DEEPSLATE_DIAMOND_ORE, this::crystalShardDrops);
        addVanillaDrop(Blocks.OAK_LEAVES, block -> chanceDropWithShears(block, UItems.ACORN, GEMSTONES_FORTUNE_CHANCE));
        addVanillaDrop(Blocks.SPRUCE_LEAVES, block -> chanceDropWithShears(block, UItems.PINECONE, GEMSTONES_FORTUNE_CHANCE));
        addVanillaDrop(Blocks.GRAVEL, this::shellDrops);
        addVanillaDrop(Blocks.SUSPICIOUS_GRAVEL, this::shellDrops);
    }

    private void addVanillaDrop(Block block, Function<Block, LootTable.Builder> lootTableFunction) {
        block.getLootTable().ifPresent(key -> {
            ((BlockLootSubProviderAccessor)this).getMap().put(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("unicopiamc", key.identifier().getPath())), lootTableFunction.apply(block));
        });
    }

    public LootTable.Builder shellDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .when(doesNotHaveSilkTouch().and(createNeedsOceanOrBeachCondition()))
                .add(applyExplosionDecay(block, TagEntry.tagContents(items.getOrThrow(UTags.Items.SHELLS))
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(1)))
                    )
                    .when(BonusLevelTableCondition.bonusLevelFlatChance(entryOf(Enchantments.FORTUNE), GEMSTONES_FORTUNE_CHANCE)))
                );
    }

    public LootTable.Builder chanceDropWithShears(Block block, ItemLike drop, float...chance) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(doesNotHaveSilkTouch().and(() -> hasShears().value()))
                        .add(chanceDrops(block, drop, 1, chance))
                );
    }

    public LootTable.Builder chanceDrop(Block block, ItemLike item, int max, float...chance) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(doesNotHaveSilkTouch())
                        .add(chanceDrops(block, item, max, chance))
                );
    }

    public LootTable.Builder wormDrops(Block block, int max, float...chance) {
        return chanceDrop(block, UItems.WHEAT_WORMS, max, chance);
    }

    public LootTable.Builder gemstoneAndWormDrops(Block block, int max, float...chance) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .when(doesNotHaveSilkTouch())
                        .add(gemstoneDrops(block, 0.1F))
                        .add(chanceDrops(block, UItems.WHEAT_WORMS, max, chance))
                );
    }

    public LootTable.Builder gemstoneDrops(Block block) {
        return LootTable.lootTable()
            .withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .when(doesNotHaveSilkTouch())
                .add(gemstoneDrops(block, 0.1F))
            );
    }

    public LootTable.Builder crystalShardDrops(Block block) {
        return LootTable.lootTable()
            .withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .when(doesNotHaveSilkTouch())
                .add(applyExplosionDecay(block, LootItem.lootTableItem(UItems.CRYSTAL_SHARD)
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2)))
                        .apply(ApplyBonusCount.addOreBonusCount(entryOf(Enchantments.FORTUNE)))
                    )
                    .when(LootItemRandomChanceCondition.randomChance(0.25F))
                )
            );
    }

    public LootPoolEntryContainer.Builder<?> gemstoneDrops(Block block, float chance) {
        return applyExplosionDecay(block, LootItem.lootTableItem(UItems.GEMSTONE)
                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2)))
            )
            .when(createWithGemFinderCondition())
            .when(LootItemRandomChanceCondition.randomChance(0.1F))
            .when(BonusLevelTableCondition.bonusLevelFlatChance(entryOf(Enchantments.FORTUNE), GEMSTONES_FORTUNE_CHANCE));
    }

    public LootPoolEntryContainer.Builder<?> chanceDrops(Block block, ItemLike drop, int max, float...chance) {
        return applyExplosionDecay(block, LootItem.lootTableItem(drop)
                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, max)))
            )
            .when(BonusLevelTableCondition.bonusLevelFlatChance(entryOf(Enchantments.FORTUNE), chance));
    }

    public LootTable.Builder dropsWithGemfinding(Block drop, LootPoolEntryContainer.Builder<?> child) {
        return BlockLootSubProvider.createSelfDropDispatchTable(drop, Holder.direct(createWithoutSilkTouchOrGemFinderCondition().build()), child);
    }
}
