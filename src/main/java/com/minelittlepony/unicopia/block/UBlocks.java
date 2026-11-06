package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import java.util.Set;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.cloud.CloudPillarBlock;
import com.minelittlepony.unicopia.block.cloud.CloudSlabBlock;
import com.minelittlepony.unicopia.block.cloud.CloudStairsBlock;
import com.minelittlepony.unicopia.block.cloud.CompactedCloudBlock;
import com.minelittlepony.unicopia.block.cloud.NaturalCloudBlock;
import com.minelittlepony.unicopia.block.cloud.OrientedCloudBlock;
import com.minelittlepony.unicopia.block.cloud.PoreousCloudStairsBlock;
import com.minelittlepony.unicopia.block.cloud.ShapingBenchBlock;
import com.minelittlepony.unicopia.block.cloud.CloudBedBlock;
import com.minelittlepony.unicopia.block.cloud.CloudChestBlock;
import com.minelittlepony.unicopia.block.cloud.CloudDoorBlock;
import com.minelittlepony.unicopia.block.cloud.CloudLike;
import com.minelittlepony.unicopia.block.cloud.SoggyCloudBlock;
import com.minelittlepony.unicopia.block.cloud.SoggyCloudSlabBlock;
import com.minelittlepony.unicopia.block.cloud.SoggyCloudStairsBlock;
import com.minelittlepony.unicopia.block.cloud.UnstableCloudBlock;
import com.minelittlepony.unicopia.block.zap.BaseZapAppleLeavesBlock;
import com.minelittlepony.unicopia.block.zap.ElectrifiedFenceBlock;
import com.minelittlepony.unicopia.block.zap.ElectrifiedFenceGateBlock;
import com.minelittlepony.unicopia.block.zap.ZapAppleLeavesBlock;
import com.minelittlepony.unicopia.block.zap.ZapAppleLeavesPlaceholderBlock;
import com.minelittlepony.unicopia.block.zap.ZapAppleLogBlock;
import com.minelittlepony.unicopia.block.zap.ZapBlock;
import com.minelittlepony.unicopia.block.zap.ZapSlabBlock;
import com.minelittlepony.unicopia.block.zap.ZapStairsBlock;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.cloud.CloudBlockItem;
import com.minelittlepony.unicopia.item.group.ItemGroupRegistry;
import com.minelittlepony.unicopia.server.world.UTreeGen;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.item.v1.BlockTransformerHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.server.dedicated.Settings;

public interface UBlocks {
    List<Block> TRANSLUCENT_BLOCKS = new ArrayList<>();
    List<Block> SEMI_TRANSPARENT_BLOCKS = new ArrayList<>();

    @SuppressWarnings("deprecation")
    Block ROCKS = register("rocks", s -> new RockCropBlock(s
            .forceSolidOff()
            .mapColor(MapColor.STONE)
            .noOcclusion()
            .pushReaction(PushReaction.POPPED)
            .requiresCorrectToolForDrops()
            .randomTicks()
            .strength(2)
            .sound(SoundType.STONE)));

    Block FROSTED_OBSIDIAN = register("frosted_obsidian", Properties.ofFullCopy(Blocks.OBSIDIAN), s -> new FrostedObsidianBlock(s.randomTicks()));

    Block ZAP_LOG = register("zap_log", ZapAppleLogBlock.settings(MapColor.COLOR_GRAY, MapColor.DEEPSLATE), s -> new ZapAppleLogBlock(Blocks.OAK_LOG.defaultBlockState(), s), CreativeModeTabs.BUILDING_BLOCKS);
    Block ZAP_WOOD = register("zap_wood", ZapAppleLogBlock.settings(MapColor.DEEPSLATE, MapColor.DEEPSLATE), s -> new ZapAppleLogBlock(Blocks.OAK_WOOD.defaultBlockState(), s), CreativeModeTabs.BUILDING_BLOCKS);

    Block STRIPPED_ZAP_LOG = register("stripped_zap_log", ZapAppleLogBlock.settings(MapColor.COLOR_LIGHT_GRAY, MapColor.COLOR_GRAY), s -> new ZapAppleLogBlock(Blocks.STRIPPED_OAK_LOG.defaultBlockState(), s), CreativeModeTabs.BUILDING_BLOCKS);
    Block STRIPPED_ZAP_WOOD = register("stripped_zap_wood", ZapAppleLogBlock.settings(MapColor.COLOR_GRAY, MapColor.COLOR_GRAY), s -> new ZapAppleLogBlock(Blocks.STRIPPED_OAK_WOOD.defaultBlockState(), s), CreativeModeTabs.BUILDING_BLOCKS);

    Block WAXED_ZAP_LOG = register("waxed_zap_log", BlockConstructionUtils.createLogBlock(UMapColors.ZAP_LOG_END, UMapColors.ZAP_LOG_SIDE), CreativeModeTabs.BUILDING_BLOCKS);
    Block WAXED_ZAP_WOOD = register("waxed_zap_wood", BlockConstructionUtils.createLogBlock(UMapColors.ZAP_LOG_SIDE, UMapColors.ZAP_LOG_SIDE), CreativeModeTabs.BUILDING_BLOCKS);
    Block WAXED_STRIPPED_ZAP_LOG = register("waxed_stripped_zap_log", BlockConstructionUtils.createLogBlock(UMapColors.ZAP_LOG_END, UMapColors.STRIPPED_ZAP_LOG_SIDE), CreativeModeTabs.BUILDING_BLOCKS);
    Block WAXED_STRIPPED_ZAP_WOOD = register("waxed_stripped_zap_wood", BlockConstructionUtils.createLogBlock(UMapColors.STRIPPED_ZAP_LOG_SIDE, UMapColors.STRIPPED_ZAP_LOG_SIDE), CreativeModeTabs.BUILDING_BLOCKS);

    Block ZAP_PLANKS = register("zap_planks", s -> new ZapBlock(s.mapColor(UMapColors.ZAP_PLANKS).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block ZAP_STAIRS = register("zap_stairs", Properties.ofFullCopy(ZAP_PLANKS), s -> new ZapStairsBlock(ZAP_PLANKS.defaultBlockState(), s.pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block ZAP_SLAB = register("zap_slab", s -> new ZapSlabBlock(s.mapColor(ZAP_PLANKS.defaultMapColor()).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block ZAP_FENCE = register("zap_fence", s -> new ElectrifiedFenceBlock(s.mapColor(ZAP_PLANKS.defaultMapColor()).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block ZAP_FENCE_GATE = register("zap_fence_gate", s -> new ElectrifiedFenceGateBlock(UWoodTypes.ZAP, s.mapColor(ZAP_PLANKS.defaultMapColor()).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);

    Block WAXED_ZAP_PLANKS = register("waxed_zap_planks", s -> new Block(s.mapColor(UMapColors.ZAP_PLANKS).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block WAXED_ZAP_STAIRS = register("waxed_zap_stairs", Properties.ofFullCopy(WAXED_ZAP_PLANKS), s -> new StairBlock(WAXED_ZAP_PLANKS.defaultBlockState(), s.pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block WAXED_ZAP_SLAB = register("waxed_zap_slab", s -> new SlabBlock(s.mapColor(WAXED_ZAP_PLANKS.defaultMapColor()).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block WAXED_ZAP_FENCE = register("waxed_zap_fence", s -> new FenceBlock(s.mapColor(WAXED_ZAP_PLANKS.defaultMapColor()).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block WAXED_ZAP_FENCE_GATE = register("waxed_zap_fence_gate", s -> new FenceGateBlock(UWoodTypes.ZAP, s.mapColor(WAXED_ZAP_PLANKS.defaultMapColor()).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);

    Block ZAP_LEAVES = register("zap_leaves", ZapAppleLeavesBlock.settings(), ZapAppleLeavesBlock::new, CreativeModeTabs.NATURAL_BLOCKS);
    Block FLOWERING_ZAP_LEAVES = register("flowering_zap_leaves", BaseZapAppleLeavesBlock.settings(), BaseZapAppleLeavesBlock::new, CreativeModeTabs.NATURAL_BLOCKS);
    Block ZAP_LEAVES_PLACEHOLDER = register("zap_leaves_placeholder", s -> new ZapAppleLeavesPlaceholderBlock(s.replaceable().noCollision().noLootTable().air()));
    Block ZAP_BULB = register("zap_bulb", s -> new FruitBlock(Direction.DOWN, Set.of(ZAP_LEAVES, FLOWERING_ZAP_LEAVES), Unicopia.key(Registries.ITEM, "zap_bulb"), FruitBlock.DEFAULT_SHAPE, false, s.mapColor(MapColor.COLOR_GRAY).strength(500, 1200).sound(SoundType.AZALEA_LEAVES)));
    Block ZAP_APPLE = register("zap_apple", s -> new FruitBlock(Direction.DOWN, Set.of(ZAP_LEAVES, FLOWERING_ZAP_LEAVES), Unicopia.key(Registries.ITEM, "zap_apple"), FruitBlock.DEFAULT_SHAPE, false, s.mapColor(MapColor.COLOR_GRAY).sound(SoundType.AZALEA_LEAVES)));

    Block PALM_LOG = register("palm_log", BlockConstructionUtils.createLogBlock(UMapColors.PALM_LOG_END, UMapColors.PALM_LOG_SIDE), CreativeModeTabs.BUILDING_BLOCKS);
    Block PALM_WOOD = register("palm_wood", BlockConstructionUtils.createWoodBlock(UMapColors.PALM_LOG_SIDE), CreativeModeTabs.BUILDING_BLOCKS);
    Block STRIPPED_PALM_LOG = register("stripped_palm_log", BlockConstructionUtils.createLogBlock(UMapColors.PALM_LOG_END, UMapColors.STRIPPED_PALM_LOG_SIDE), CreativeModeTabs.BUILDING_BLOCKS);
    Block STRIPPED_PALM_WOOD = register("stripped_palm_wood", BlockConstructionUtils.createWoodBlock(UMapColors.STRIPPED_PALM_LOG_SIDE), CreativeModeTabs.BUILDING_BLOCKS);

    Block PALM_PLANKS = register("palm_planks", s -> new Block(s.mapColor(UMapColors.PALM_PLANKS).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block PALM_STAIRS = register("palm_stairs", Properties.ofFullCopy(PALM_PLANKS), s -> new StairBlock(PALM_PLANKS.defaultBlockState(), s.pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block PALM_SLAB = register("palm_slab", s -> new SlabBlock(s.mapColor(PALM_PLANKS.defaultMapColor()).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block PALM_FENCE = register("palm_fence", s -> new FenceBlock(s.mapColor(PALM_PLANKS.defaultMapColor()).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block PALM_FENCE_GATE = register("palm_fence_gate", s -> new FenceGateBlock(UWoodTypes.PALM, s.mapColor(PALM_PLANKS.defaultMapColor()).strength(2, 3).sound(SoundType.WOOD).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block PALM_DOOR = register("palm_door", s -> new DoorBlock(UWoodTypes.PALM.setType(), s.mapColor(PALM_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3.0f).noOcclusion().ignitedByLava().pushReaction(PushReaction.POPPED)), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Block PALM_TRAPDOOR = register("palm_trapdoor", s -> new TrapDoorBlock(UWoodTypes.PALM.setType(), s.mapColor(PALM_PLANKS.defaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3).noOcclusion().isValidSpawn(BlockConstructionUtils::never).ignitedByLava()), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Block PALM_PRESSURE_PLATE = register("palm_pressure_plate", s -> new PressurePlateBlock(UWoodTypes.PALM.setType(), s.mapColor(PALM_PLANKS.defaultMapColor()).noCollision().strength(0.5f).sound(SoundType.WOOD).pushReaction(PushReaction.POPPED)), CreativeModeTabs.BUILDING_BLOCKS);
    Block PALM_BUTTON = register("palm_button", BlockConstructionUtils.woodenButton(UWoodTypes.PALM.setType()), CreativeModeTabs.BUILDING_BLOCKS);
    Block PALM_SIGN = register("palm_sign", s -> new StandingSignBlock(UWoodTypes.PALM, s.mapColor(PALM_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).ignitedByLava().sound(SoundType.WOOD)));
    Block PALM_WALL_SIGN = register("palm_wall_sign", s -> new WallSignBlock(UWoodTypes.PALM, s.mapColor(PALM_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).overrideLootTable(PALM_SIGN.getLootTable()).ignitedByLava()));
    Block PALM_HANGING_SIGN = register("palm_hanging_sign", s -> new CeilingHangingSignBlock(UWoodTypes.PALM, s.mapColor(PALM_LOG.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).ignitedByLava()));
    Block PALM_WALL_HANGING_SIGN = register("palm_wall_hanging_sign", s -> new WallHangingSignBlock(UWoodTypes.PALM, s.mapColor(PALM_LOG.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0f).ignitedByLava().overrideLootTable(PALM_HANGING_SIGN.getLootTable())));

    Block PALM_LEAVES = register("palm_leaves", BlockConstructionUtils.createLeavesBlock(DiagonallyStableLeavesBlock::new, SoundType.GRASS), CreativeModeTabs.BUILDING_BLOCKS);
    Block BANANAS = register("bananas", s -> new FruitBlock(Direction.DOWN, PALM_LEAVES, Unicopia.key(Registries.ITEM, "banana"), Shapes.block(), s.mapColor(MapColor.COLOR_YELLOW).sound(SoundType.WOOD).noCollision().randomTicks().instabreak().pushReaction(PushReaction.POPPED)));

    PineappleCropBlock PINEAPPLE = register("pineapple", s -> new PineappleCropBlock(s.sound(SoundType.GRASS).noCollision().instabreak().pushReaction(PushReaction.POPPED)));

    Block MANGO_LEAVES = register("mango_leaves", Properties.ofFullCopy(Blocks.JUNGLE_LEAVES), s -> new FruitBearingBlock(
            0xCCFFAA00,
            () -> UBlocks.MANGO,
            () -> UItems.MANGO.getDefaultInstance(),
            s
    ), CreativeModeTabs.NATURAL_BLOCKS);
    Block MANGO = register("mango", s -> new FruitBlock(Direction.DOWN, MANGO_LEAVES, Unicopia.key(Registries.ITEM, "mango"), FruitBlock.DEFAULT_SHAPE, s.mapColor(MapColor.COLOR_ORANGE)));

    Block WEATHER_VANE = register("weather_vane", s -> new WeatherVaneBlock(s.mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(3.0f, 6.0f).sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)), CreativeModeTabs.TOOLS_AND_UTILITIES);

    Block GREEN_APPLE_LEAVES = register("green_apple_leaves", Properties.ofFullCopy(Blocks.OAK_LEAVES), s -> new FruitBearingBlock(
            0xE5FFFF88,
            () -> UBlocks.GREEN_APPLE,
            () -> UItems.GREEN_APPLE.getDefaultInstance(),
            s
    ), CreativeModeTabs.NATURAL_BLOCKS);
    Block GREEN_APPLE = register("green_apple", s -> new FruitBlock(Direction.DOWN, GREEN_APPLE_LEAVES, Unicopia.key(Registries.ITEM, "green_apple"), FruitBlock.DEFAULT_SHAPE, s.mapColor(MapColor.COLOR_GREEN)));
    Block GREEN_APPLE_SPROUT = register("green_apple_sprout", SproutBlock.settings(), s -> new SproutBlock(0xE5FFFF88, () -> UItems.GREEN_APPLE_SEEDS, () -> UTreeGen.GREEN_APPLE_TREE.sapling().map(Block::defaultBlockState).get(), s));

    Block SWEET_APPLE_LEAVES = register("sweet_apple_leaves", Properties.ofFullCopy(Blocks.OAK_LEAVES), s -> new FruitBearingBlock(
            0xE5FFCC88,
            () -> UBlocks.SWEET_APPLE,
            () -> UItems.SWEET_APPLE.getDefaultInstance(),
            s
    ), CreativeModeTabs.NATURAL_BLOCKS);
    Block SWEET_APPLE = register("sweet_apple", s -> new FruitBlock(Direction.DOWN, SWEET_APPLE_LEAVES, Unicopia.key(Registries.ITEM, "sweet_apple"), FruitBlock.DEFAULT_SHAPE, s.mapColor(MapColor.COLOR_GREEN)));
    Block SWEET_APPLE_SPROUT = register("sweet_apple_sprout", SproutBlock.settings(), s -> new SproutBlock(0xE5FFCC88, () -> UItems.SWEET_APPLE_SEEDS, () -> UTreeGen.SWEET_APPLE_TREE.sapling().map(Block::defaultBlockState).get(), s));

    Block SOUR_APPLE_LEAVES = register("sour_apple_leaves", Properties.ofFullCopy(Blocks.OAK_LEAVES), s -> new FruitBearingBlock(
            0xE5FFCCCC,
            () -> UBlocks.SOUR_APPLE,
            () -> UItems.SOUR_APPLE.getDefaultInstance(),
            s
    ), CreativeModeTabs.NATURAL_BLOCKS);
    Block SOUR_APPLE = register("sour_apple", s -> new FruitBlock(Direction.DOWN, SOUR_APPLE_LEAVES, Unicopia.key(Registries.ITEM, "sour_apple"), FruitBlock.DEFAULT_SHAPE, s.mapColor(MapColor.COLOR_GREEN)));
    Block SOUR_APPLE_SPROUT = register("sour_apple_sprout", SproutBlock.settings(), s -> new SproutBlock(0xE5FFCC88, () -> UItems.SOUR_APPLE_SEEDS, () -> UTreeGen.SOUR_APPLE_TREE.sapling().map(Block::defaultBlockState).get(), s));

    // TODO: Separate into two blocks (block.toItem() is returning "pie_with_hoof" which is incorrect)
    Block APPLE_PIE = register("apple_pie", s -> new PieBlock(
            () -> UItems.APPLE_PIE_SLICE,
            () -> UItems.APPLE_PIE,
            () -> UItems.APPLE_PIE_HOOF,
            s.forceSolidOn().mapColor(MapColor.COLOR_ORANGE).strength(0.5F).sound(SoundType.WOOL).pushReaction(PushReaction.POPPED)
    ));
    Block GOLDEN_OAK_LEAVES = register("golden_oak_leaves", Properties.ofFullCopy(Blocks.OAK_LEAVES), s -> new GoldenOakLeavesBlock(
            MapColor.GOLD.col,
            () -> UBlocks.GOLDEN_APPLE,
            () -> Items.GOLDEN_APPLE.getDefaultInstance(),
            s
    ), CreativeModeTabs.NATURAL_BLOCKS);
    Block GOLDEN_APPLE = register("golden_apple", s -> new EnchantedFruitBlock(Direction.DOWN, GOLDEN_OAK_LEAVES, ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("golden_apple")), FruitBlock.DEFAULT_SHAPE, false, s.mapColor(MapColor.GOLD)));
    Block GOLDEN_OAK_SPROUT = register("golden_oak_sprout", SproutBlock.settings(), s -> new SproutBlock(0xE5FFCC88, () -> UItems.GOLDEN_OAK_SEEDS, () -> UTreeGen.GOLDEN_OAK_TREE.sapling().map(Block::defaultBlockState).get(), s));
    StrippablePillarBlock GOLDEN_OAK_LOG = register("golden_oak_log", BlockConstructionUtils.createMetallicLogBlock(MapColor.QUARTZ, MapColor.GOLD), CreativeModeTabs.BUILDING_BLOCKS);
    StrippablePillarBlock GOLDEN_OAK_WOOD = register("golden_oak_wood", BlockConstructionUtils.createMetallicWoodBlock(MapColor.QUARTZ), CreativeModeTabs.BUILDING_BLOCKS);
    Block GOLDEN_OAK_PLANKS = register("golden_oak_planks", s -> new Block(s.mapColor(MapColor.GOLD).strength(3, 4).sound(SoundType.METAL).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block GOLDEN_OAK_SLAB = register("golden_oak_slab", s -> new SlabBlock(s.mapColor(GOLDEN_OAK_PLANKS.defaultMapColor()).strength(2, 3).sound(SoundType.METAL).pushReaction(PushReaction.PUSH_PULL)), CreativeModeTabs.BUILDING_BLOCKS);
    Block GOLDEN_OAK_STAIRS = register("golden_oak_stairs", Properties.ofFullCopy(GOLDEN_OAK_PLANKS), s -> new StairBlock(GOLDEN_OAK_PLANKS.defaultBlockState(), s), CreativeModeTabs.BUILDING_BLOCKS);

    StrippablePillarBlock STRIPPED_GOLDEN_OAK_LOG = register("stripped_golden_oak_log", BlockConstructionUtils.createMetallicLogBlock(MapColor.QUARTZ, MapColor.GOLD), CreativeModeTabs.BUILDING_BLOCKS);
    StrippablePillarBlock STRIPPED_GOLDEN_OAK_WOOD = register("stripped_golden_oak_wood", BlockConstructionUtils.createMetallicWoodBlock(MapColor.QUARTZ), CreativeModeTabs.BUILDING_BLOCKS);

    SegmentedCropBlock OATS = register("oats", Properties.ofFullCopy(Blocks.WHEAT), s -> SegmentedCropBlock.create(11, 5, () -> UItems.OAT_SEEDS, null, null, s));
    SegmentedCropBlock OATS_STEM = register("oats_stem", Properties.ofFullCopy(Blocks.WHEAT), OATS.createNext(5));
    SegmentedCropBlock OATS_CROWN = register("oats_crown", Properties.ofFullCopy(Blocks.WHEAT), OATS_STEM.createNext(5));

    Block PLUNDER_VINE = register("plunder_vine", s -> new ThornBlock(() -> UBlocks.PLUNDER_VINE_BUD, s.mapColor(MapColor.CRIMSON_HYPHAE).destroyTime(1).randomTicks().sound(SoundType.WOOD).pushReaction(PushReaction.POPPED)));
    Block PLUNDER_VINE_BUD = register("plunder_vine_bud", s -> new ThornBudBlock(PLUNDER_VINE.defaultBlockState(), s.mapColor(MapColor.CRIMSON_HYPHAE).destroyTime(1).noOcclusion().randomTicks().noCollision().sound(SoundType.GRASS).pushReaction(PushReaction.POPPED)));
    CuringJokeBlock CURING_JOKE = register("curing_joke", s -> new CuringJokeBlock(UEffects.BUTTER_FINGERS, 7, s.mapColor(MapColor.ICE).noCollision().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED)));
    Block GOLD_ROOT = register("gold_root", s -> new CarrotBlock(s.mapColor(MapColor.GOLD).noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.POPPED)) {
        @Override
        protected ItemLike getBaseSeedId() {
            return Items.GOLDEN_CARROT;
        }
    });

    Block CHITIN = register("chitin", s -> new SnowyBlock(s.mapColor(MapColor.ICE).destroyTime(5).requiresCorrectToolForDrops().randomTicks().sound(SoundType.CORAL_BLOCK)), CreativeModeTabs.NATURAL_BLOCKS);
    Block SURFACE_CHITIN = register("surface_chitin", Properties.ofFullCopy(CHITIN), s -> new GrowableBlock(() -> CHITIN, s), CreativeModeTabs.NATURAL_BLOCKS);
    Block CHISELLED_CHITIN = register("chiselled_chitin", s -> new Block(s.mapColor(MapColor.ICE).destroyTime(5).requiresCorrectToolForDrops()), CreativeModeTabs.BUILDING_BLOCKS);
    Block CHITIN_SPIKES = register("chitin_spikes", Properties.ofFullCopy(CHISELLED_CHITIN), s -> new SpikesBlock(s.noCollision().noOcclusion()), CreativeModeTabs.NATURAL_BLOCKS);
    Block CHISELLED_CHITIN_SLAB = register("chiselled_chitin_slab", Properties.ofFullCopy(CHISELLED_CHITIN), SlabBlock::new, CreativeModeTabs.BUILDING_BLOCKS);
    Block CHISELLED_CHITIN_STAIRS = register("chiselled_chitin_stairs", Properties.ofFullCopy(CHISELLED_CHITIN), s -> new StairBlock(CHISELLED_CHITIN.defaultBlockState(), s), CreativeModeTabs.BUILDING_BLOCKS);
    Block CHISELLED_CHITIN_HULL = register("chiselled_chitin_hull", Properties.ofFullCopy(CHISELLED_CHITIN), OrientedBlock::new, CreativeModeTabs.BUILDING_BLOCKS);
    Block CHISELLED_CHITIN_WALL = register("chiselled_chitin_wall", Properties.ofFullCopy(CHISELLED_CHITIN), WallBlock::new, CreativeModeTabs.BUILDING_BLOCKS);
    Block POLISHED_CHITIN = register("polished_chitin", Properties.ofFullCopy(CHISELLED_CHITIN), Block::new, CreativeModeTabs.BUILDING_BLOCKS);
    Block POLISHED_CHITIN_SLAB = register("polished_chitin_slab", Properties.ofFullCopy(POLISHED_CHITIN), SlabBlock::new, CreativeModeTabs.BUILDING_BLOCKS);
    Block POLISHED_CHITIN_STAIRS = register("polished_chitin_stairs", Properties.ofFullCopy(POLISHED_CHITIN), s -> new StairBlock(POLISHED_CHITIN.defaultBlockState(), s), CreativeModeTabs.BUILDING_BLOCKS);
    Block POLISHED_CHITIN_HULL = register("polished_chitin_hull", Properties.ofFullCopy(POLISHED_CHITIN), OrientedBlock::new, CreativeModeTabs.BUILDING_BLOCKS);
    Block POLISHED_CHITIN_WALL = register("polished_chitin_wall", Properties.ofFullCopy(POLISHED_CHITIN), WallBlock::new, CreativeModeTabs.BUILDING_BLOCKS);
    Block HIVE = register("hive", s -> new HiveBlock(s.mapColor(MapColor.COLOR_PURPLE).destroyTime(6).randomTicks().sound(SoundType.CORAL_BLOCK)), CreativeModeTabs.NATURAL_BLOCKS);
    Block MYSTERIOUS_EGG = register("mysterious_egg", Properties.ofFullCopy(Blocks.SLIME_BLOCK), s -> new PileBlock(s, PileBlock.MYSTERIOUS_EGG_SHAPES), CreativeModeTabs.NATURAL_BLOCKS);
    Block SLIME_PUSTULE = register("slime_pustule", Properties.ofFullCopy(Blocks.SLIME_BLOCK), SlimePustuleBlock::new, CreativeModeTabs.NATURAL_BLOCKS);
    Block SLIME = register("slime", s -> new SlimeLayerBlock(s
            .mapColor(MapColor.GRASS)
            .replaceable()
            .noOcclusion()
            .randomTicks()
            .friction(0.8F)
            .strength(0.1F)
            .requiresCorrectToolForDrops()
            .sound(SoundType.SLIME_BLOCK)
            .isViewBlocking((state, world, pos, bounds) -> state.getValue(SlimeLayerBlock.LAYERS) >= 8)
            .pushReaction(PushReaction.POPPED)
    ), CreativeModeTabs.NATURAL_BLOCKS);

    Block SHAPING_BENCH = register("shaping_bench", s -> new ShapingBenchBlock(s.mapColor(MapColor.QUARTZ).destroyTime(0.3F).explosionResistance(0).sound(SoundType.WOOL)), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    @SuppressWarnings("deprecation")
    Block CLOUD = register("cloud", s -> new NaturalCloudBlock(true,
            () -> UBlocks.SOGGY_CLOUD,
            () -> UBlocks.COMPACTED_CLOUD,
            s.forceSolidOff().mapColor(MapColor.QUARTZ).destroyTime(0.3F).explosionResistance(0).sound(SoundType.WOOL)), CreativeModeTabs.NATURAL_BLOCKS);
    Block COMPACTED_CLOUD = register("compacted_cloud", Properties.ofFullCopy(CLOUD), s -> new CompactedCloudBlock(CLOUD.defaultBlockState(), s));
    Block CLOUD_SLAB = register("cloud_slab", Properties.ofFullCopy(CLOUD), s -> new CloudSlabBlock(true, () -> UBlocks.SOGGY_CLOUD_SLAB, s), CreativeModeTabs.NATURAL_BLOCKS);
    PoreousCloudStairsBlock CLOUD_STAIRS = register("cloud_stairs", Properties.ofFullCopy(CLOUD), s -> new PoreousCloudStairsBlock(CLOUD.defaultBlockState(), () -> UBlocks.SOGGY_CLOUD_STAIRS, s), CreativeModeTabs.NATURAL_BLOCKS);

    Block CLOUD_PLANKS = register("cloud_planks", Properties.ofFullCopy(CLOUD), s -> new NaturalCloudBlock(false, null, () -> UBlocks.COMPACTED_CLOUD_PLANKS, s.destroyTime(0.4F).requiresCorrectToolForDrops().forceSolidOn()), CreativeModeTabs.BUILDING_BLOCKS);
    Block COMPACTED_CLOUD_PLANKS = register("compacted_cloud_planks", Properties.ofFullCopy(CLOUD_PLANKS), s -> new CompactedCloudBlock(CLOUD_PLANKS.defaultBlockState(), s));
    Block CLOUD_PLANK_SLAB = register("cloud_plank_slab", Properties.ofFullCopy(CLOUD_PLANKS), s -> new CloudSlabBlock(false, null, s), CreativeModeTabs.BUILDING_BLOCKS);
    Block CLOUD_PLANK_STAIRS = register("cloud_plank_stairs", Properties.ofFullCopy(CLOUD_PLANKS), s -> new CloudStairsBlock(CLOUD_PLANKS.defaultBlockState(), s), CreativeModeTabs.BUILDING_BLOCKS);

    Block CLOUD_BRICKS = register("cloud_bricks", Properties.ofFullCopy(CLOUD), s -> new NaturalCloudBlock(false, null, () -> UBlocks.COMPACTED_CLOUD_BRICKS, s.destroyTime(0.6F).requiresCorrectToolForDrops().forceSolidOn()), CreativeModeTabs.BUILDING_BLOCKS);
    Block COMPACTED_CLOUD_BRICKS = register("compacted_cloud_bricks", Properties.ofFullCopy(CLOUD_BRICKS), s -> new CompactedCloudBlock(CLOUD_BRICKS.defaultBlockState(), s));
    Block CLOUD_BRICK_SLAB = register("cloud_brick_slab", Properties.ofFullCopy(CLOUD_BRICKS), s -> new CloudSlabBlock(false, null, s), CreativeModeTabs.BUILDING_BLOCKS);
    Block CLOUD_BRICK_STAIRS = register("cloud_brick_stairs", Properties.ofFullCopy(CLOUD_BRICKS), s -> new CloudStairsBlock(CLOUD_BRICKS.defaultBlockState(), s), CreativeModeTabs.BUILDING_BLOCKS);

    Block ETCHED_CLOUD = register("etched_cloud", Properties.ofFullCopy(CLOUD_BRICKS), s -> new NaturalCloudBlock(false, null, () -> UBlocks.COMPACTED_CLOUD_BRICKS, s), CreativeModeTabs.BUILDING_BLOCKS);
    Block COMPACTED_ETCHED_CLOUD = register("compacted_etched_cloud", Properties.ofFullCopy(ETCHED_CLOUD), s -> new CompactedCloudBlock(ETCHED_CLOUD.defaultBlockState(), s));
    Block ETCHED_CLOUD_SLAB = register("etched_cloud_slab", Properties.ofFullCopy(ETCHED_CLOUD), s -> new CloudSlabBlock(false, null, s), CreativeModeTabs.BUILDING_BLOCKS);
    Block ETCHED_CLOUD_STAIRS = register("etched_cloud_stairs", Properties.ofFullCopy(ETCHED_CLOUD), s -> new CloudStairsBlock(ETCHED_CLOUD.defaultBlockState(), s), CreativeModeTabs.BUILDING_BLOCKS);

    SoggyCloudBlock SOGGY_CLOUD = register("soggy_cloud", Properties.ofFullCopy(CLOUD), s -> new SoggyCloudBlock(() -> UBlocks.CLOUD.defaultBlockState(), s.destroyTime(0.7F)));
    SoggyCloudSlabBlock SOGGY_CLOUD_SLAB = register("soggy_cloud_slab", Properties.ofFullCopy(SOGGY_CLOUD), s -> new SoggyCloudSlabBlock(() -> UBlocks.CLOUD_SLAB.defaultBlockState(), s));
    SoggyCloudStairsBlock SOGGY_CLOUD_STAIRS = register("soggy_cloud_stairs", Properties.ofFullCopy(CLOUD), s -> new SoggyCloudStairsBlock(SOGGY_CLOUD.defaultBlockState(), () -> UBlocks.CLOUD_STAIRS.defaultBlockState(), s));

    Block DENSE_CLOUD = register("dense_cloud", s -> new NaturalCloudBlock(false, null, () -> UBlocks.COMPACTED_DENSE_CLOUD, s.mapColor(MapColor.COLOR_GRAY).destroyTime(0.5F).explosionResistance(0).sound(SoundType.WOOL).forceSolidOn()), CreativeModeTabs.BUILDING_BLOCKS);
    Block COMPACTED_DENSE_CLOUD = register("compacted_dense_cloud", Properties.ofFullCopy(DENSE_CLOUD), s -> new CompactedCloudBlock(DENSE_CLOUD.defaultBlockState(), s));
    Block DENSE_CLOUD_SLAB = register("dense_cloud_slab", Properties.ofFullCopy(DENSE_CLOUD), s -> new CloudSlabBlock(false, null, s), CreativeModeTabs.BUILDING_BLOCKS);
    Block DENSE_CLOUD_STAIRS = register("dense_cloud_stairs", Properties.ofFullCopy(DENSE_CLOUD), s -> new CloudStairsBlock(DENSE_CLOUD.defaultBlockState(), s), CreativeModeTabs.BUILDING_BLOCKS);

    Block CARVED_CLOUD = register("carved_cloud", Properties.ofFullCopy(CLOUD), s -> new OrientedCloudBlock(false, s.destroyTime(0.4F).requiresCorrectToolForDrops().forceSolidOn()), CreativeModeTabs.BUILDING_BLOCKS);
    Block UNSTABLE_CLOUD = register("unstable_cloud", Properties.ofFullCopy(CLOUD), UnstableCloudBlock::new, CreativeModeTabs.NATURAL_BLOCKS);
    Block CLOUD_PILLAR = register("cloud_pillar", Properties.of(), s -> new CloudPillarBlock(s.mapColor(MapColor.COLOR_GRAY).destroyTime(0.5F).explosionResistance(0).sound(SoundType.WOOL).forceSolidOn()), CreativeModeTabs.NATURAL_BLOCKS);
    Block CLOUD_CHEST = register("cloud_chest", Properties.ofFullCopy(DENSE_CLOUD), s -> new CloudChestBlock(DENSE_CLOUD.defaultBlockState(), s.instrument(NoteBlockInstrument.BASS).strength(2.5f)), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Block CLOTH_BED = register("cloth_bed", Properties.ofFullCopy(Blocks.BED.white()), s -> new FancyBedBlock("cloth", s.sound(SoundType.WOOD)));
    Block CLOUD_BED = register("cloud_bed", Properties.ofFullCopy(Blocks.BED.white()), s -> new CloudBedBlock("cloud", CLOUD.defaultBlockState(), s.sound(SoundType.WOOL)));

    Block CLAM_SHELL = register("clam_shell", s -> new ShellsBlock(s.mapColor(MapColor.CRIMSON_STEM).instabreak().noOcclusion()));
    Block SCALLOP_SHELL = register("scallop_shell", s -> new ShellsBlock(s.mapColor(MapColor.CRIMSON_STEM).instabreak().noOcclusion()));
    Block TURRET_SHELL = register("turret_shell", s -> new ShellsBlock(s.mapColor(MapColor.CRIMSON_STEM).instabreak().noOcclusion()));

    Block STABLE_DOOR = register("stable_door", Properties.ofFullCopy(Blocks.OAK_DOOR), s -> new StableDoorBlock(BlockSetType.OAK, s), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Block DARK_OAK_DOOR = register("dark_oak_stable_door", Properties.ofFullCopy(Blocks.OAK_DOOR), s -> new StableDoorBlock(BlockSetType.OAK, s), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Block CRYSTAL_DOOR = register("crystal_door", Properties.ofFullCopy(Blocks.IRON_DOOR), s -> new CrystalDoorBlock(UWoodTypes.CRYSTAL, s), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Block CLOUD_DOOR = register("cloud_door", Properties.ofFullCopy(CLOUD), s -> new CloudDoorBlock(CLOUD.defaultBlockState(), UWoodTypes.CLOUD, s), CreativeModeTabs.FUNCTIONAL_BLOCKS);

    Block SPECTRAL_FIRE = register("spectral_fire", Properties.ofFullCopy(Blocks.SOUL_FIRE), SpectralFireBlock::new);
    Block JAR = register("jar", Properties.ofFullCopy(Blocks.GLASS), ItemJarBlock::new);
    Block CLOUD_JAR = register("cloud_jar", Properties.ofFullCopy(Blocks.GLASS), JarBlock::new);
    Block STORM_JAR = register("storm_jar", Properties.ofFullCopy(Blocks.GLASS), JarBlock::new);
    Block LIGHTNING_JAR = register("lightning_jar", Properties.ofFullCopy(Blocks.GLASS), JarBlock::new);
    Block ZAP_JAR = register("zap_jar", Properties.ofFullCopy(Blocks.GLASS), JarBlock::new);

    Block WORM_BLOCK = register("worm_block", s -> new ColoredFallingBlock(new ColorRGBA(0xFF0088), s.destroyTime(0.1F).explosionResistance(0).requiresCorrectToolForDrops().sound(SoundType.MUD)), CreativeModeTabs.NATURAL_BLOCKS);
    EdibleBlock HAY_BLOCK = register("hay_block", Properties.ofFullCopy(Blocks.HAY_BLOCK), s -> new EdibleBlock(Identifier.withDefaultNamespace("hay_block"), Identifier.withDefaultNamespace("wheat"), true, s));

    private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> block) {
        return register(name, block, null);
    }

    static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> block, ResourceKey<CreativeModeTab> group) {
        return register(name, Properties.of(), block, group);
    }

    private static <T extends Block> T register(String name, BlockBehaviour.Properties settings, Function<BlockBehaviour.Properties, T> blockFunc) {
        return register(name, settings, blockFunc, null);
    }

    private static <T extends Block> T register(String name, BlockBehaviour.Properties settings, Function<BlockBehaviour.Properties, T> blockFunc, @Nullable ResourceKey<CreativeModeTab> group) {
        return register(Unicopia.id(name), settings, blockFunc, group);
    }

    static <T extends Block> T register(Identifier id, BlockBehaviour.Properties settings, Function<BlockBehaviour.Properties, T> blockFunc, @Nullable ResourceKey<CreativeModeTab> group) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        T block = blockFunc.apply(settings.setId(key));
        if (block instanceof TintedBlock) {
            TintedBlock.REGISTRY.add(block);
        }
        if (block instanceof SaplingBlock || block instanceof SproutBlock || block instanceof FruitBlock || block instanceof CropBlock || block instanceof DoorBlock || block instanceof TrapDoorBlock) {
            TRANSLUCENT_BLOCKS.add(block);
        }
        if (block instanceof CloudLike || block instanceof SlimePustuleBlock || block instanceof PileBlock || block instanceof SlimeLayerBlock) {
            SEMI_TRANSPARENT_BLOCKS.add(block);
        }

        if (group != null) {
            ItemGroupRegistry.register(id, s -> {
                if (id.getPath().equals("worm_block")) {
                    s.compostable(com.minelittlepony.unicopia.item.UCompostables.ALWAYS);
                }
                return block instanceof CloudLike ? new CloudBlockItem(block, s) : new BlockItem(block, s);
            }, group);
        }
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    static void bootstrap() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            var block = world.getBlockState(hit.getBlockPos()).getBlock();
            if ((block == GOLDEN_OAK_LOG || block == GOLDEN_OAK_WOOD)
                    && player.getItemInHand(hand).is(item -> item.is(net.minecraft.tags.ItemTags.AXES))) {
                return ((StrippingLootable)block).onStripped(new net.minecraft.world.item.context.UseOnContext(player, hand, hit))
                        ? net.minecraft.world.InteractionResult.PASS : net.minecraft.world.InteractionResult.FAIL;
            }
            return net.minecraft.world.InteractionResult.PASS;
        });
        if (FabricLoader.getInstance().isModLoaded("farmersdelight")) {
            register("rice_block", Properties.ofFullCopy(Blocks.HAY_BLOCK), s -> new EdibleBlock(Identifier.fromNamespaceAndPath("farmersdelight", "rice_bale"), Identifier.fromNamespaceAndPath("farmersdelight", "rice_panicle"), true, s));
            register("straw_block", Properties.ofFullCopy(Blocks.HAY_BLOCK), s -> new EdibleBlock(Identifier.fromNamespaceAndPath("farmersdelight", "straw_bale"), Identifier.fromNamespaceAndPath("farmersdelight", "straw"), true, s));
        }
        BlockEntityTypeSupportHelper.of(net.minecraft.world.level.block.entity.BlockEntityTypes.SIGN).addSupportedBlocks(PALM_SIGN, PALM_WALL_SIGN);
        BlockEntityTypeSupportHelper.of(net.minecraft.world.level.block.entity.BlockEntityTypes.HANGING_SIGN).addSupportedBlocks(PALM_HANGING_SIGN, PALM_WALL_HANGING_SIGN);

        BlockTransformerHelper.registerStripping(ZAP_LOG, STRIPPED_ZAP_LOG);
        BlockTransformerHelper.registerStripping(PALM_LOG, STRIPPED_PALM_LOG);
        BlockTransformerHelper.registerStripping(GOLDEN_OAK_LOG, STRIPPED_GOLDEN_OAK_LOG);
        BlockTransformerHelper.registerStripping(ZAP_WOOD, STRIPPED_ZAP_WOOD);
        BlockTransformerHelper.registerStripping(PALM_WOOD, STRIPPED_PALM_WOOD);
        BlockTransformerHelper.registerStripping(GOLDEN_OAK_WOOD, STRIPPED_GOLDEN_OAK_WOOD);
        OxidizableBlocksRegistry.registerWaxable(ZAP_LOG, WAXED_ZAP_LOG);
        OxidizableBlocksRegistry.registerWaxable(ZAP_WOOD, WAXED_ZAP_WOOD);
        OxidizableBlocksRegistry.registerWaxable(STRIPPED_ZAP_LOG, WAXED_STRIPPED_ZAP_LOG);
        OxidizableBlocksRegistry.registerWaxable(STRIPPED_ZAP_WOOD, WAXED_STRIPPED_ZAP_WOOD);
        OxidizableBlocksRegistry.registerWaxable(ZAP_PLANKS, WAXED_ZAP_PLANKS);
        OxidizableBlocksRegistry.registerWaxable(ZAP_STAIRS, WAXED_ZAP_STAIRS);
        OxidizableBlocksRegistry.registerWaxable(ZAP_SLAB, WAXED_ZAP_SLAB);
        OxidizableBlocksRegistry.registerWaxable(ZAP_FENCE, WAXED_ZAP_FENCE);
        OxidizableBlocksRegistry.registerWaxable(ZAP_FENCE_GATE, WAXED_ZAP_FENCE_GATE);
        Collections.addAll(TRANSLUCENT_BLOCKS,
                WEATHER_VANE, CHITIN_SPIKES, PLUNDER_VINE, PLUNDER_VINE_BUD, CLAM_SHELL, SCALLOP_SHELL, TURRET_SHELL, CURING_JOKE, SPECTRAL_FIRE,
                JAR, CLOUD_JAR, STORM_JAR, LIGHTNING_JAR, ZAP_JAR
        );

        FlammableBlockRegistry.getDefaultInstance().add(GREEN_APPLE_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(SWEET_APPLE_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(SOUR_APPLE_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(MANGO_LEAVES, 30, 60);

        FlammableBlockRegistry.getDefaultInstance().add(PALM_PLANKS, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_SLAB, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_FENCE_GATE, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_FENCE, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_STAIRS, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_LOG, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_PALM_LOG, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_PALM_WOOD, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_WOOD, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_LEAVES, 30, 60);

        FlammableBlockRegistry.getDefaultInstance().add(BANANAS, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(CURING_JOKE, 60, 100);


        UBlockEntities.bootstrap();
        EdibleBlock.bootstrap();
    }
}
