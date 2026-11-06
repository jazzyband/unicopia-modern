package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.EdibleBlock;
import com.minelittlepony.unicopia.block.EnchantedFruitBlock;
import com.minelittlepony.unicopia.block.PieBlock;
import com.minelittlepony.unicopia.block.PileBlock;
import com.minelittlepony.unicopia.block.SegmentedCropBlock;
import com.minelittlepony.unicopia.block.ShellsBlock;
import com.minelittlepony.unicopia.block.SlimePustuleBlock;
import com.minelittlepony.unicopia.block.StrippingLootable;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.providers.UModelProvider;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.Tree;
import com.minelittlepony.unicopia.server.world.UTreeGen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.mixin.datagen.loot.BlockLootSubProviderAccessor;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;


public class UBlockLootTableProvider extends FabricBlockLootSubProvider {
    protected static final float[] SAPLING_DROP_CHANCE = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
    private static final float[] LEAVES_STICK_DROP_CHANCE = new float[]{0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};

    private final CompletableFuture<HolderLookup.Provider> registryLookupFuture;

    public UBlockLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
        registryLookupFuture = registryLookup;
    }

    public <T> Holder<T> entryOf(ResourceKey<T> key) {
        return registryLookupFuture.getNow(null).lookupOrThrow(key.registryKey()).getOrThrow(key);
    }

    private LootItemConditionalFunction.Builder<?> createBasePreservingFortuneBonusCondition() {
        return ApplyBonusCount.addBonusBinomialDistributionCount(entryOf(Enchantments.FORTUNE), 0.8714286F, 0);
    }

    private LootItemConditionalFunction.Builder<?> createCropsFortuneBonusCondition() {
        return ApplyBonusCount.addBonusBinomialDistributionCount(entryOf(Enchantments.FORTUNE), 0.5714286F, 3);
    }

    @Override
    public void generate() {
        // simple drops
        List.of(
            UBlocks.CARVED_CLOUD, UBlocks.UNSTABLE_CLOUD,
            UBlocks.CHISELLED_CHITIN_STAIRS, UBlocks.CHISELLED_CHITIN, UBlocks.CHISELLED_CHITIN_WALL,
            UBlocks.POLISHED_CHITIN_STAIRS, UBlocks.POLISHED_CHITIN, UBlocks.POLISHED_CHITIN_WALL,
            UBlocks.CLOUD_BRICK_STAIRS, UBlocks.CLOUD_BRICKS,
            UBlocks.CLOUD_PLANK_STAIRS, UBlocks.CLOUD_PLANKS,
            UBlocks.CURING_JOKE,
            UBlocks.GOLDEN_OAK_LOG, UBlocks.GOLDEN_OAK_PLANKS, UBlocks.GOLDEN_OAK_WOOD,
            UBlocks.STRIPPED_GOLDEN_OAK_LOG, UBlocks.STRIPPED_GOLDEN_OAK_WOOD,
            UBlocks.HIVE,

            UBlocks.PALM_BUTTON, UBlocks.PALM_FENCE_GATE, UBlocks.PALM_FENCE, UBlocks.PALM_LOG, UBlocks.PALM_PLANKS,
            UBlocks.PALM_PRESSURE_PLATE, UBlocks.PALM_SIGN, UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_STAIRS, UBlocks.PALM_TRAPDOOR, UBlocks.PALM_WOOD,

            UBlocks.STRIPPED_PALM_LOG, UBlocks.STRIPPED_PALM_WOOD,
            UBlocks.STRIPPED_ZAP_LOG, UBlocks.STRIPPED_ZAP_WOOD,

            UBlocks.WAXED_STRIPPED_ZAP_LOG, UBlocks.WAXED_STRIPPED_ZAP_WOOD,
            UBlocks.WAXED_ZAP_FENCE_GATE, UBlocks.WAXED_ZAP_FENCE,
            UBlocks.WAXED_ZAP_LOG, UBlocks.WAXED_ZAP_PLANKS, UBlocks.WAXED_ZAP_STAIRS, UBlocks.WAXED_ZAP_WOOD,

            UBlocks.WEATHER_VANE,

            UBlocks.ZAP_FENCE_GATE, UBlocks.ZAP_FENCE,
            UBlocks.ZAP_LOG, UBlocks.ZAP_PLANKS, UBlocks.ZAP_STAIRS, UBlocks.ZAP_WOOD,
            UBlocks.WORM_BLOCK
        ).forEach(this::dropSelf);

        // slabs
        List.of(
                UBlocks.CHISELLED_CHITIN_SLAB, UBlocks.POLISHED_CHITIN_SLAB,
                UBlocks.CLOUD_BRICK_SLAB, UBlocks.CLOUD_PLANK_SLAB,
                UBlocks.PALM_SLAB, UBlocks.ZAP_SLAB, UBlocks.WAXED_ZAP_SLAB, UBlocks.GOLDEN_OAK_SLAB
        ).forEach(slab -> add(slab, this::createSlabItemTable));
        add(UBlocks.CLOUD_SLAB, slab -> decomposingSlabDrops(slab, UItems.CLOUD_LUMP, 2));
        add(UBlocks.SOGGY_CLOUD_SLAB, slab -> decomposingSlabDrops(slab, UItems.CLOUD_LUMP, 2));
        add(UBlocks.DENSE_CLOUD_SLAB, slab -> decomposingSlabDrops(slab, UItems.CLOUD_LUMP, 4));
        add(UBlocks.ETCHED_CLOUD_SLAB, slab -> decomposingSlabDrops(slab, UItems.CLOUD_LUMP, 4));

        // fruit
        UModelProvider.FRUITS.forEach(block -> {
            if (block != UBlocks.GOLDEN_APPLE && block != UBlocks.BANANAS) {
                add(block, fortuneBonusDrops(block.asItem()));
            }
        });
        add(UBlocks.GOLDEN_APPLE, LootTable.lootTable().withPool(LootPool.lootPool()
            .setRolls(exactly(1))
            .add(applyStateCondition(UBlocks.GOLDEN_APPLE, EnchantedFruitBlock.ENCHANTED, false, applyExplosionDecay(UBlocks.GOLDEN_APPLE, LootItem.lootTableItem(Items.GOLDEN_APPLE))).apply(createBasePreservingFortuneBonusCondition()))
            .add(applyStateCondition(UBlocks.GOLDEN_APPLE, EnchantedFruitBlock.ENCHANTED, true, applyExplosionDecay(UBlocks.GOLDEN_APPLE, LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE))).apply(createBasePreservingFortuneBonusCondition()))
        ));
        List.of(UBlocks.GREEN_APPLE_LEAVES, UBlocks.SOUR_APPLE_LEAVES, UBlocks.SWEET_APPLE_LEAVES, UBlocks.GOLDEN_OAK_LEAVES).forEach(block -> add(block, b -> fruitLeavesDrops(b, Items.STICK)));
        add(UBlocks.GOLDEN_OAK_LEAVES, b -> fruitLeavesDrops(b, UItems.GOLDEN_STICK));
        add(UBlocks.MANGO_LEAVES, block -> createLeavesDrops(block, UTreeGen.MANGO_TREE.sapling().get(), 0.025F, 0.027777778F, 0.03125F, 0.041666668F, 0.1F)); // same chance as jungle
        add(UBlocks.ZAP_LEAVES, block -> createLeavesDrops(block, UTreeGen.ZAP_APPLE_TREE.sapling().get(), NORMAL_LEAVES_SAPLING_CHANCES));
        add(UBlocks.FLOWERING_ZAP_LEAVES, block -> createLeavesDrops(block, UTreeGen.ZAP_APPLE_TREE.sapling().get(), NORMAL_LEAVES_SAPLING_CHANCES));
        add(UBlocks.PALM_LEAVES, block -> createLeavesDrops(block, UTreeGen.BANANA_TREE.sapling().get(), NORMAL_LEAVES_SAPLING_CHANCES));

        Tree.REGISTRY.forEach(tree -> {
            tree.sapling().ifPresent(this::dropSelf);
            tree.pot().ifPresent(this::dropPottedContents);
        });

        // jars
        List.of(
                UBlocks.JAR, UBlocks.CLOUD_JAR, UBlocks.STORM_JAR, UBlocks.LIGHTNING_JAR, UBlocks.STORM_JAR
        ).forEach(jar -> add(jar, this::createSilkTouchOnlyTable));

        // doors
        List.of(
            UBlocks.CLOUD_DOOR, UBlocks.CRYSTAL_DOOR,
            UBlocks.DARK_OAK_DOOR, UBlocks.PALM_DOOR, UBlocks.STABLE_DOOR
        ).forEach(door -> add(door, this::createDoorTable));

        //beds
        List.of(
            UBlocks.CLOUD_BED, UBlocks.CLOTH_BED
        ).forEach(bed -> add(bed, b -> createSinglePropConditionTable(b, BedBlock.PART, BedPart.HEAD)));

        add(UBlocks.CHITIN_SPIKES, createSingleItemTableWithSilkTouch(UBlocks.CHITIN_SPIKES, UItems.CARAPACE, exactly(6)));
        add(UBlocks.CHITIN, createSingleItemTableWithSilkTouch(UBlocks.CHITIN, UItems.CARAPACE, exactly(9)));
        add(UBlocks.SURFACE_CHITIN, createSingleItemTableWithSilkTouch(UBlocks.SURFACE_CHITIN, UItems.CARAPACE, exactly(9)));
        add(UBlocks.CHISELLED_CHITIN_HULL, hullDrops(UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHITIN, UBlocks.CHISELLED_CHITIN));
        add(UBlocks.POLISHED_CHITIN_HULL, hullDrops(UBlocks.POLISHED_CHITIN_HULL, UBlocks.CHITIN, UBlocks.POLISHED_CHITIN));

        add(UBlocks.SLIME_PUSTULE, LootTable.lootTable()
                .withPool(applyStateCondition(UBlocks.SLIME_PUSTULE, SlimePustuleBlock.SHAPE, SlimePustuleBlock.Shape.POD,
                        applyExplosionCondition(UBlocks.SLIME_PUSTULE, LootPool.lootPool()
                    .setRolls(exactly(1))
                    .add(LootItem.lootTableItem(UBlocks.SLIME_PUSTULE)).when(hasShearsOrSilkTouch()))
        )));
        add(UBlocks.SLIME, block -> LootTable.lootTable()
                .withPool(LootPool.lootPool().when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS))
                        .add(AlternativesEntry.alternatives(
                                AlternativesEntry.alternatives(SnowLayerBlock.LAYERS.getPossibleValues(), layer -> LootItem.lootTableItem(Items.SLIME_BALL)
                                    .when(MatchBlock.blockMatches(net.minecraft.core.registries.BuiltInRegistries.BLOCK, block, StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, layer)))
                                    .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(layer.intValue())))
                                ).when(doesNotHaveSilkTouch()),
                                AlternativesEntry.alternatives(SnowLayerBlock.LAYERS.getPossibleValues(), layer -> layer == 8
                                    ? LootItem.lootTableItem(Blocks.SLIME_BLOCK)
                                    : LootItem.lootTableItem(UBlocks.SLIME)
                                        .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(layer)))
                                        .when(MatchBlock.blockMatches(net.minecraft.core.registries.BuiltInRegistries.BLOCK, block, StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, layer)))))
        )));
        add(UBlocks.MYSTERIOUS_EGG, LootTable.lootTable()
            .withPool(applyExplosionCondition(UBlocks.MYSTERIOUS_EGG, LootPool.lootPool()
                .setRolls(exactly(1))
                .add(LootItem.lootTableItem(UBlocks.MYSTERIOUS_EGG))
                    .apply(PileBlock.COUNT.getPossibleValues(), count -> applyStateCondition(UBlocks.MYSTERIOUS_EGG, PileBlock.COUNT, count, SetItemCountFunction.setCount(exactly(count)))))));

        add(UBlocks.CLOUD, createSingleItemTableWithSilkTouch(UBlocks.CLOUD, UItems.CLOUD_LUMP, exactly(4)));
        add(UBlocks.CLOUD_STAIRS, createSingleItemTableWithSilkTouch(UBlocks.CLOUD_STAIRS, UItems.CLOUD_LUMP, exactly(6)));

        add(UBlocks.SOGGY_CLOUD, createSingleItemTableWithSilkTouch(UBlocks.CLOUD, UItems.CLOUD_LUMP, exactly(4)));
        add(UBlocks.SOGGY_CLOUD_STAIRS, createSingleItemTableWithSilkTouch(UBlocks.CLOUD_STAIRS, UItems.CLOUD_LUMP, exactly(6)));

        add(UBlocks.DENSE_CLOUD, createSingleItemTableWithSilkTouch(UBlocks.DENSE_CLOUD, UItems.CLOUD_LUMP, exactly(9)));
        add(UBlocks.DENSE_CLOUD_STAIRS, createSingleItemTableWithSilkTouch(UBlocks.DENSE_CLOUD_STAIRS, UItems.CLOUD_LUMP, exactly(13)));
        add(UBlocks.ETCHED_CLOUD, createSingleItemTableWithSilkTouch(UBlocks.ETCHED_CLOUD, UItems.CLOUD_LUMP, exactly(9)));
        add(UBlocks.ETCHED_CLOUD_STAIRS, createSingleItemTableWithSilkTouch(UBlocks.ETCHED_CLOUD_STAIRS, UItems.CLOUD_LUMP, exactly(13)));

        // recipe produces: 6 blocks -> 3 pillars means: 6/3 = 2
        add(UBlocks.CLOUD_PILLAR, createSingleItemTableWithSilkTouch(UBlocks.CLOUD_PILLAR, UBlocks.CLOUD, exactly(2)));

        dropOther(UBlocks.FROSTED_OBSIDIAN, Blocks.OBSIDIAN);
        add(UBlocks.APPLE_PIE, pieDrops(UBlocks.APPLE_PIE, UItems.APPLE_PIE, UItems.APPLE_PIE_HOOF));

        // crops
        addTallCropDrops(UBlocks.OATS, UItems.OATS);
        add(UBlocks.BANANAS, LootTable.lootTable()
            .withPool(applyExplosionCondition(UBlocks.BANANAS, LootPool.lootPool()
                .setRolls(exactly(1))
                .add(item(UItems.BANANA, between(6, 12)).apply(createCropsFortuneBonusCondition()))
            )));
        add(UBlocks.PINEAPPLE, LootTable.lootTable()
            .withPool(applyExplosionCondition(UBlocks.PINEAPPLE, LootPool.lootPool()
                .setRolls(exactly(1))
                .add(item(UItems.PINEAPPLE, exactly(1))
                    .apply(createBasePreservingFortuneBonusCondition())
                    .when(MatchBlock.blockMatches(net.minecraft.core.registries.BuiltInRegistries.BLOCK, UBlocks.PINEAPPLE, StatePropertiesPredicate.Builder.properties()
                        .hasProperty(BlockStateProperties.HALF, Half.TOP)
                        .hasProperty(BlockStateProperties.AGE_7, BlockStateProperties.MAX_AGE_7))))
            )));
        add(UBlocks.ROCKS, applyExplosionDecay(UBlocks.ROCKS, LootTable.lootTable()
                .withPool(applyStateCondition(UBlocks.ROCKS, BlockStateProperties.AGE_7, BlockStateProperties.MAX_AGE_7, LootPool.lootPool()
                    .setRolls(exactly(1))
                    .add(LootItem.lootTableItem(UItems.WEIRD_ROCK).when(LootItemRandomChanceCondition.randomChance(0.25F)).apply(createCropsFortuneBonusCondition()))
                    .add(LootItem.lootTableItem(UItems.ROCK).apply(createCropsFortuneBonusCondition()))))
                .withPool(LootPool.lootPool()
                    .setRolls(exactly(1))
                    .add(LootItem.lootTableItem(UItems.PEBBLES)))
        ));
        add(UBlocks.GOLD_ROOT, applyExplosionDecay(UBlocks.GOLD_ROOT, LootTable.lootTable()
            .withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.GOLDEN_CARROT)))
            .withPool(applyStateCondition(UBlocks.GOLD_ROOT, CarrotBlock.AGE, 7, LootPool.lootPool())
                    .add(LootItem.lootTableItem(Items.GOLDEN_CARROT).apply(createCropsFortuneBonusCondition())))));
        /*
        addDrop(UBlocks.PLUNDER_VINE, applyExplosionDecay(UBlocks.PLUNDER_VINE, LootTable.builder()
                .pool(LootPool.builder().rolls(exactly(1)).conditionally(RandomChanceLootCondition.builder(0.25F))
                    .with(ItemEntry.builder(Items.STICK))
                    .with(ItemEntry.builder(Items.DEAD_BUSH))
                    .with(ItemEntry.builder(UItems.GRYPHON_FEATHER).conditionally(RandomChanceLootCondition.builder(0.2F))))
        ));
        */

        // hay
        add(UBlocks.HAY_BLOCK, b -> edibleBlockDrops(b, Items.WHEAT));

        // shells
        add(UBlocks.CLAM_SHELL, shellDrops(UBlocks.CLAM_SHELL, UItems.CLAM_SHELL));
        add(UBlocks.SCALLOP_SHELL, shellDrops(UBlocks.SCALLOP_SHELL, UItems.SCALLOP_SHELL));
        add(UBlocks.TURRET_SHELL, shellDrops(UBlocks.TURRET_SHELL, UItems.TURRET_SHELL));

        var farmersDelightGenerator = new UExternalBlockLootTableProvider(((BlockLootSubProviderAccessor)this).getMap()::put, ResourceConditions.allModsLoaded("farmersdelight"));

        farmersDelightGenerator.addDrop(Unicopia.id("rice_block"), b -> UExternalBlockLootTableProvider.edibleBlockDrops(b, Identifier.fromNamespaceAndPath("farmersdelight", "rice_panicle")));
        farmersDelightGenerator.addDrop(Unicopia.id("straw_block"), b -> UExternalBlockLootTableProvider.edibleBlockDrops(b, Identifier.fromNamespaceAndPath("farmersdelight", "straw")));

        addStrippingDrops(UBlocks.GOLDEN_OAK_LOG, b -> simpleDrops(b, Items.GOLD_NUGGET, 4));
        addStrippingDrops(UBlocks.GOLDEN_OAK_WOOD, b -> simpleDrops(b, Items.GOLD_NUGGET, 6));
    }

    private <T extends Block & StrippingLootable> void addStrippingDrops(T block, Function<T, LootTable.Builder> drops) {
        block.getStrippingLootTableKey().ifPresent(key -> {
            ((BlockLootSubProviderAccessor)this).getMap().put(key, drops.apply(block));
        });
    }

    private void addTallCropDrops(SegmentedCropBlock baseCrop, ItemLike crop) {
        add(baseCrop, applyExplosionDecay(baseCrop, LootTable.lootTable()
            .withPool(LootPool.lootPool()
                .setRolls(exactly(1))
                .add(LootItem.lootTableItem(baseCrop.getBaseSeedId()))))
            .withPool(applyStateCondition(baseCrop, baseCrop.getAgeProperty(), baseCrop.getMaxAge(), LootPool.lootPool()
                .setRolls(exactly(1))
                .add(LootItem.lootTableItem(baseCrop.getBaseSeedId()).apply(createCropsFortuneBonusCondition())))));

        SegmentedCropBlock stage = baseCrop;
        while ((stage = stage.getNext()) != null) {
            add(stage, applyExplosionDecay(stage, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                    .setRolls(exactly(1))
                    .add(applyStateCondition(stage, stage.getAgeProperty(), stage.getMaxAge(), LootItem.lootTableItem(crop))))));
        }
    }

    private LootTable.Builder simpleDrops(Block block, ItemLike drop, int count) {
        return LootTable.lootTable()
                .withPool(applyExplosionDecay(block, LootPool.lootPool()
                    .setRolls(exactly(1))
                    .add(item(drop, between(0, count)))));
    }

    private LootTable.Builder decomposingSlabDrops(Block slab, ItemLike drop, int count) {
        return LootTable.lootTable()
            .withPool(applyExplosionDecay(slab, LootPool.lootPool()
                .setRolls(exactly(1))
                .add(item(drop, exactly(count))
                        .apply(applyStateCondition(slab, SlabBlock.TYPE, SlabType.DOUBLE, SetItemCountFunction.setCount(exactly(count * 2)))))));
    }

    private LootTable.Builder fruitLeavesDrops(Block leaves, ItemLike stick) {
        return LootTable.lootTable()
            .withPool(LootPool.lootPool()
                .setRolls(exactly(1))
                .add(LootItem.lootTableItem(leaves).when(hasShearsOrSilkTouch())))
            .withPool(applyExplosionDecay(leaves, LootPool.lootPool()
                .setRolls(exactly(1))
                .when(doesNotHaveShearsOrSilkTouch())
                .add(item(stick, between(1, 2)).when(BonusLevelTableCondition.bonusLevelFlatChance(entryOf(Enchantments.FORTUNE), LEAVES_STICK_DROP_CHANCE)))));
    }

    private LootTable.Builder hullDrops(Block hull, ItemLike inner, ItemLike outer) {
        return LootTable.lootTable()
            .withPool(applyExplosionCondition(hull, LootPool.lootPool()
                .setRolls(exactly(1))
                .add(item(hull, exactly(2)).when(doesNotHaveSilkTouch()))
                .add(item(inner, exactly(2)).when(doesNotHaveSilkTouch()))
                .add(item(outer, exactly(2)).when(hasSilkTouch()))));
    }

    private LootTable.Builder edibleBlockDrops(Block block, ItemLike drop) {
        LootTable.Builder builder = LootTable.lootTable();
        for (BooleanProperty segment : EdibleBlock.SEGMENTS) {
            builder
                .withPool(applyExplosionCondition(block, LootPool.lootPool()
                    .setRolls(exactly(1))
                        .add(applyStateCondition(block, segment, true, LootItem.lootTableItem(drop)))));
        }
        return builder;
    }

    private LootTable.Builder pieDrops(Block block, Item drop, Item stomped) {
        return LootTable.lootTable()
            .withPool(applyExplosionDecay(block, LootPool.lootPool()
                .setRolls(exactly(1)).when(hasSilkTouch())
                .add(applyStateCondition(block, PieBlock.STOMPED, false, LootItem.lootTableItem(drop)))
                .add(applyStateCondition(block, PieBlock.STOMPED, true, LootItem.lootTableItem(stomped)))));
    }

    private LootTable.Builder shellDrops(Block block, Item shell) {
        return LootTable.lootTable()
            .withPool(applyExplosionDecay(block, LootPool.lootPool()
                .setRolls(exactly(1))
                .add(LootItem.lootTableItem(shell))
                    .apply(ShellsBlock.COUNT.getPossibleValues(), count -> applyStateCondition(block, ShellsBlock.COUNT, count, SetItemCountFunction.setCount(exactly(count))))
                    .apply(createBasePreservingFortuneBonusCondition())));
    }


    public LootTable.Builder fortuneBonusDrops(ItemLike drop) {
        return LootTable.lootTable().withPool(applyExplosionCondition(drop, LootPool.lootPool()
                .setRolls(exactly(1))
                .add(LootItem.lootTableItem(drop).apply(createBasePreservingFortuneBonusCondition()))));
    }

    public static net.minecraft.core.Holder<net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider> exactly(int n) {
        return ContextIntProviders.exactly(n);
    }

    public static net.minecraft.core.Holder<net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider> between(int from, int to) {
        return ContextIntProviders.between(from, to);
    }

    public static LootItem.Builder<?> item(ItemLike item, Holder<ContextIntProvider> count) {
        return LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(count));
    }

    public static <T extends ConditionUserBuilder<T>, P extends Comparable<P> & StringRepresentable> T applyStateCondition(Block block,
            Property<P> property, P value, ConditionUserBuilder<T> builder) {
        return builder.when(MatchBlock.blockMatches(net.minecraft.core.registries.BuiltInRegistries.BLOCK, block, StatePropertiesPredicate.Builder.properties().hasProperty(property, value)));
    }

    public static <T extends ConditionUserBuilder<T>> T applyStateCondition(Block block,
            BooleanProperty property, boolean value, ConditionUserBuilder<T> builder) {
        return builder.when(MatchBlock.blockMatches(net.minecraft.core.registries.BuiltInRegistries.BLOCK, block, StatePropertiesPredicate.Builder.properties().hasProperty(property, value)));
    }

    public static <T extends ConditionUserBuilder<T>> T applyStateCondition(Block block,
            IntegerProperty property, int value, ConditionUserBuilder<T> builder) {
        return builder.when(MatchBlock.blockMatches(net.minecraft.core.registries.BuiltInRegistries.BLOCK, block, StatePropertiesPredicate.Builder.properties().hasProperty(property, value)));
    }
}
