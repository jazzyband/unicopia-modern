package com.minelittlepony.unicopia.datagen.providers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.util.Util;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.FancyBedBlock;
import com.minelittlepony.unicopia.block.FruitBearingBlock;
import com.minelittlepony.unicopia.block.PieBlock;
import com.minelittlepony.unicopia.block.PileBlock;
import com.minelittlepony.unicopia.block.ShellsBlock;
import com.minelittlepony.unicopia.block.SlimePustuleBlock;
import com.minelittlepony.unicopia.block.SproutBlock;
import com.minelittlepony.unicopia.block.TintedBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.zap.ZapAppleLeavesBlock;
import com.minelittlepony.unicopia.client.render.item.BlockTintSource;
import com.minelittlepony.unicopia.client.render.item.CloudBedModelRenderer;
import com.minelittlepony.unicopia.client.render.item.CloudChestModelRenderer;
import com.minelittlepony.unicopia.datagen.UBlockFamilies;
import com.minelittlepony.unicopia.server.world.Tree;
import com.mojang.math.Quadrant;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import static net.minecraft.client.data.models.model.TextureSlot.*;
import static com.mojang.math.Quadrant.*;
import static com.minelittlepony.unicopia.datagen.providers.ItemModels.*;

public class UBlockStateModelGenerator extends BlockModelGenerators {
    static final Identifier AIR_BLOCK_ID = Identifier.withDefaultNamespace("block/air");
    static final Identifier AIR_ITEM_ID = Identifier.withDefaultNamespace("item/air");

    private static final PropertyDispatch<VariantMutator> UP_DEFAULT_ROTATION_OPERATIONS = PropertyDispatch.modify(BlockStateProperties.FACING)
            .select(Direction.DOWN, X_ROT_180)
            .select(Direction.UP, NOP)
            .select(Direction.NORTH, X_ROT_90)
            .select(Direction.SOUTH, X_ROT_90.then(Y_ROT_180))
            .select(Direction.WEST, X_ROT_90.then(Y_ROT_270))
            .select(Direction.EAST, X_ROT_90.then(Y_ROT_90));
    private static final PropertyDispatch<VariantMutator> DOWN_DEFAULT_ROTATION_OPERATIONS = Util.make(PropertyDispatch.modify(BlockStateProperties.FACING), map -> createDownDefaultFacingVariantMap(map::select));

    public static final void createDownDefaultFacingVariantMap(BiConsumer<Direction, VariantMutator> builder) {
        builder.accept(Direction.DOWN, NOP);
        builder.accept(Direction.UP, X_ROT_180);
        builder.accept(Direction.SOUTH, X_ROT_90);
        builder.accept(Direction.NORTH, X_ROT_90.then(Y_ROT_180));
        builder.accept(Direction.EAST, X_ROT_90.then(Y_ROT_270));
        builder.accept(Direction.WEST, X_ROT_90.then(Y_ROT_90));
    }

    static UBlockStateModelGenerator create(BlockModelGenerators modelGenerator) {
        return new UBlockStateModelGenerator(modelGenerator);
    }

    protected UBlockStateModelGenerator(BlockModelGenerators modelGenerator) {
        this(modelGenerator.blockStateOutput, modelGenerator.itemModelOutput, modelGenerator.modelOutput);
    }

    public UBlockStateModelGenerator(
            Consumer<BlockModelDefinitionGenerator> blockStateCollector,
            ItemModelOutput itemModelCollector,
            BiConsumer<Identifier, ModelInstance> modelCollector) {
        super(blockStateCollector, itemModelCollector, (id, modelSupplier) -> {
            if (AIR_BLOCK_ID.equals(id) || AIR_ITEM_ID.equals(id)) {
                throw new IllegalStateException("Registered air id for block model: " + modelSupplier.get().toString());
            }
            modelCollector.accept(id, modelSupplier);
        });
    }

    @Override
    public void run() {
        for (int i = 0; i < ModelTemplates.STEMS.length; i++) {
            ModelTemplates.STEMS[i].create(Unicopia.id("block/apple_sprout_stage" + i), TextureMapping.stem(Blocks.MELON_STEM), modelOutput);
        }

        // handmade
        registerAll(UBlockStateModelGenerator::createNonTemplateModelBlock, UBlocks.SHAPING_BENCH, UBlocks.BANANAS);
        registerAll((g, block) -> g.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block)), UBlocks.SHAPING_BENCH, UBlocks.SURFACE_CHITIN);
        // doors
        registerAll(UBlockStateModelGenerator::registerStableDoor, UBlocks.STABLE_DOOR, UBlocks.DARK_OAK_DOOR, UBlocks.CLOUD_DOOR);
        registerLockingDoor(UBlocks.CRYSTAL_DOOR);

        // cloud blocks
        createCustomTexturePool(UBlocks.CLOUD, TexturedModel.CUBE).donateModelTo(UBlocks.CLOUD, UBlocks.UNSTABLE_CLOUD).slab(UBlocks.CLOUD_SLAB).stairs(UBlocks.CLOUD_STAIRS);
        createCustomTexturePool(UBlocks.ETCHED_CLOUD, TexturedModel.CUBE).slab(UBlocks.ETCHED_CLOUD_SLAB).stairs(UBlocks.ETCHED_CLOUD_STAIRS);
        createCustomTexturePool(UBlocks.DENSE_CLOUD, TexturedModel.CUBE).slab(UBlocks.DENSE_CLOUD_SLAB).stairs(UBlocks.DENSE_CLOUD_STAIRS);
        createCustomTexturePool(UBlocks.CLOUD_PLANKS, TexturedModel.CUBE).slab(UBlocks.CLOUD_PLANK_SLAB).stairs(UBlocks.CLOUD_PLANK_STAIRS);
        createCustomTexturePool(UBlocks.CLOUD_BRICKS, TexturedModel.CUBE).slab(UBlocks.CLOUD_BRICK_SLAB).stairs(UBlocks.CLOUD_BRICK_STAIRS);
        createTwoStepTexturePool(UBlocks.SOGGY_CLOUD, TexturedModel.CUBE_BOTTOM_TOP.updateTexture(textures -> textures.put(BOTTOM, new Material(ModelLocationUtils.getModelLocation(UBlocks.CLOUD))))).slab(UBlocks.SOGGY_CLOUD_SLAB).stairs(UBlocks.SOGGY_CLOUD_STAIRS);
        registerRotated(UBlocks.CARVED_CLOUD, TexturedModel.COLUMN);
        registerPillar(UBlocks.CLOUD_PILLAR);

        registerAll(UBlockStateModelGenerator::registerCompactedBlock, UBlocks.COMPACTED_CLOUD, UBlocks.COMPACTED_CLOUD_BRICKS, UBlocks.COMPACTED_CLOUD_PLANKS, UBlocks.COMPACTED_DENSE_CLOUD, UBlocks.COMPACTED_ETCHED_CLOUD);
        registerChest(UBlocks.CLOUD_CHEST, UBlocks.CLOUD);
        registerFancyBed(UBlocks.CLOUD_BED, UBlocks.CLOUD, true);
        registerFancyBed(UBlocks.CLOTH_BED, Blocks.SPRUCE_PLANKS, false);

        // chitin blocks
        registerTopsoil(UBlocks.SURFACE_CHITIN, UBlocks.CHITIN);
        createTrivialBlock(UBlocks.CHITIN, BlockModels.CUBE_BOTTOM);
        family(UBlocks.CHISELLED_CHITIN).generateFor(UBlockFamilies.CHISELED_CHITIN);
        family(UBlocks.POLISHED_CHITIN).generateFor(UBlockFamilies.POLISHED_CHITIN);
        registerHiveBlock(UBlocks.HIVE);
        registerRotated(UBlocks.CHITIN_SPIKES, BlockModels.SPIKES);
        registerHull(UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHITIN, UBlocks.CHISELLED_CHITIN);
        registerHull(UBlocks.POLISHED_CHITIN_HULL, UBlocks.CHITIN, UBlocks.POLISHED_CHITIN);
        registerSimpleFlatItemModel(UBlocks.SLIME_PUSTULE.asItem());
        registerSlimeLayers(UBlocks.SLIME);
        blockStateOutput.accept(MultiVariantGenerator.dispatch(UBlocks.SLIME_PUSTULE)
                .with(PropertyDispatch.initial(SlimePustuleBlock.SHAPE)
                .generate(state -> plainVariant(ModelLocationUtils.getModelLocation(UBlocks.SLIME_PUSTULE, "_" + state.getSerializedName())))));
        registerPie(UBlocks.APPLE_PIE);

        // palm wood
        woodProvider(UBlocks.PALM_LOG).logWithHorizontal(UBlocks.PALM_LOG).wood(UBlocks.PALM_WOOD);
        woodProvider(UBlocks.STRIPPED_PALM_LOG).logWithHorizontal(UBlocks.STRIPPED_PALM_LOG).wood(UBlocks.STRIPPED_PALM_WOOD);
        family(UBlocks.PALM_PLANKS).generateFor(UBlockFamilies.PALM);
        createTrivialBlock(UBlocks.PALM_LEAVES, TexturedModel.LEAVES);

        // zap wood
        woodProvider(UBlocks.ZAP_LOG)
            .logWithHorizontal(UBlocks.ZAP_LOG).wood(UBlocks.ZAP_WOOD)
            .logWithHorizontal(UBlocks.WAXED_ZAP_LOG).wood(UBlocks.WAXED_ZAP_WOOD);
        woodProvider(UBlocks.STRIPPED_ZAP_LOG)
            .logWithHorizontal(UBlocks.STRIPPED_ZAP_LOG).wood(UBlocks.STRIPPED_ZAP_WOOD)
            .logWithHorizontal(UBlocks.WAXED_STRIPPED_ZAP_LOG).wood(UBlocks.WAXED_STRIPPED_ZAP_WOOD);
        family(UBlocks.ZAP_PLANKS).generateFor(UBlockFamilies.ZAP).donateModelTo(UBlocks.ZAP_PLANKS, UBlocks.WAXED_ZAP_PLANKS).generateFor(UBlockFamilies.WAXED_ZAP);
        registerZapLeaves(UBlocks.ZAP_LEAVES);
        createTrivialBlock(UBlocks.FLOWERING_ZAP_LEAVES, TexturedModel.LEAVES);
        createNonTemplateModelBlock(UBlocks.ZAP_LEAVES_PLACEHOLDER, Blocks.AIR);

        // golden oak wood
        createTrivialCube(UBlocks.GOLDEN_OAK_LEAVES);
        woodProvider(UBlocks.GOLDEN_OAK_LOG).logWithHorizontal(UBlocks.GOLDEN_OAK_LOG).wood(UBlocks.GOLDEN_OAK_WOOD);
        woodProvider(UBlocks.STRIPPED_GOLDEN_OAK_LOG).logWithHorizontal(UBlocks.STRIPPED_GOLDEN_OAK_LOG).wood(UBlocks.STRIPPED_GOLDEN_OAK_WOOD);
        family(UBlocks.GOLDEN_OAK_PLANKS).generateFor(UBlockFamilies.GOLDEN_OAK);

        // plants
        Tree.REGISTRY.stream().filter(tree -> tree.sapling().isPresent()).forEach(tree -> createPlantWithDefaultItem(tree.sapling().get(), tree.pot().get(), PlantType.NOT_TINTED));
        createCrossBlockWithDefaultItem(UBlocks.CURING_JOKE, PlantType.NOT_TINTED);
        registerWithStages(UBlocks.GOLD_ROOT, BlockStateProperties.AGE_7, BlockModels.CROP, 0, 0, 1, 1, 2, 2, 2, 3);

        registerTallCrop(UBlocks.PINEAPPLE, BlockStateProperties.AGE_7, BlockStateProperties.HALF,
                new int[] { 0, 1, 2, 3, 4, 5, 5, 6 },
                new int[] { 0, 0, 1, 2, 3, 4, 5, 6 }
        );
        registerPlunderVine(UBlocks.PLUNDER_VINE, UBlocks.PLUNDER_VINE_BUD);

        // leaves
        registerAll(UBlockStateModelGenerator::registerFloweringLeaves, UBlocks.GREEN_APPLE_LEAVES, UBlocks.SOUR_APPLE_LEAVES, UBlocks.SWEET_APPLE_LEAVES);
        registerAll(UBlockStateModelGenerator::registerSprout, UBlocks.GREEN_APPLE_SPROUT, UBlocks.SOUR_APPLE_SPROUT, UBlocks.SWEET_APPLE_SPROUT, UBlocks.GOLDEN_OAK_SPROUT);
        createNonTemplateModelBlock(UBlocks.MANGO_LEAVES, Blocks.JUNGLE_LEAVES);
        registerSimpleTintedItemModel(UBlocks.MANGO_LEAVES, ModelLocationUtils.getModelLocation(Blocks.JUNGLE_LEAVES), BlockTintSource.INSTANCE);

        // fruit
        UModelProvider.FRUITS.forEach(block -> createTrivialBlock(block, BlockModels.FRUIT));

        // shells
        registerAll(UBlockStateModelGenerator::registerShell, UBlocks.CLAM_SHELL, UBlocks.TURRET_SHELL, UBlocks.SCALLOP_SHELL);
        // other
        createTrivialCube(UBlocks.WORM_BLOCK);
        createAirLikeBlock(UBlocks.WEATHER_VANE, UBlocks.WEATHER_VANE.asItem());
        registerSimpleFlatItemModel(UBlocks.WEATHER_VANE.asItem());
        registerWithStages(UBlocks.FROSTED_OBSIDIAN, BlockStateProperties.AGE_3, BlockModels.CUBE_ALL, 0, 1, 2, 3);
        registerWithStagesBuiltinModels(UBlocks.ROCKS, BlockStateProperties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
        registerWithStagesBuiltinModels(UBlocks.MYSTERIOUS_EGG, PileBlock.COUNT, 1, 2, 3);
        FireModels.registerSoulFire(this, UBlocks.SPECTRAL_FIRE, Blocks.SOUL_FIRE);

        registerJar(UBlocks.JAR);
        registerWeatherJar(UBlocks.CLOUD_JAR);
        registerWeatherJar(UBlocks.STORM_JAR);
        registerWeatherJar(UBlocks.ZAP_JAR);
        registerWeatherJar(UBlocks.LIGHTNING_JAR);

        TintedBlock.REGISTRY.forEach(block -> {
            if (block == UBlocks.MANGO_LEAVES || block instanceof SproutBlock) {
                Unicopia.LOGGER.info("[Skipped] Tinted block: " + block);
                return;
            }
            Unicopia.LOGGER.info("Tinted block: " + block);
            registerSimpleTintedItemModel(block, ModelLocationUtils.getModelLocation(block), BlockTintSource.INSTANCE);
        });
    }

    public void registerWeatherJar(Block jar) {
        blockStateOutput.accept(MultiPartGenerator.multiPart(jar)
                .with(plainVariant(BlockModels.TEMPLATE_JAR))
                .with(plainVariant(ModelLocationUtils.getModelLocation(jar, "_filling"))));
        registerSimpleFlatItemModel(jar.asItem());
    }

    public void registerJar(Block jar) {
        blockStateOutput.accept(createSimpleBlock(jar, plainVariant(BlockModels.TEMPLATE_JAR)));
    }

    @SafeVarargs
    public final <T> UBlockStateModelGenerator registerAll(BiConsumer<? super UBlockStateModelGenerator, T> register, T... blocks) {
        for (T block : blocks) {
            register.accept(this, block);
        }
        return this;
    }

    public BlockFamilyProvider createCustomTexturePool(Block block, TexturedModel.Provider modelFactory) {
        final TexturedModel texturedModel = modelFactory.get(block);
        final TextureMapping textures = texturedModel.getMapping();
        return (new BlockFamilyProvider(textures) {
            @Override
            public BlockFamilyProvider stairs(Block block) {
                TextureMapping textMap = textures.copyAndUpdate(BlockModels.STEP, textures.get(SIDE));
                MultiVariant inner = plainVariant(BlockModels.INNER_STAIRS.create(block, textMap, modelOutput));
                Identifier straight = BlockModels.STRAIGHT_STAIRS.create(block, textMap, modelOutput);
                MultiVariant outer = plainVariant(BlockModels.OUTER_STAIRS.create(block, textMap, modelOutput));
                blockStateOutput.accept(createStairs(block, inner, plainVariant(straight), outer));
                if (block.asItem() != Items.AIR) registerSimpleItemModel(block, straight);
                return this;
            }
        }).fullBlock(block, texturedModel.getTemplate());
    }

    public BlockFamilyProvider createTwoStepTexturePool(Block block, TexturedModel.Provider modelFactory) {
        final TexturedModel texturedModel = modelFactory.get(block);
        final TextureMapping textures = texturedModel.getMapping();
        final Identifier baseModelId = ModelLocationUtils.getModelLocation(block);
        final Identifier twoStepTexture = ModelLocationUtils.getModelLocation(block, "_slab_side");
        return (new BlockFamilyProvider(textures) {
            @Override
            public BlockFamilyProvider stairs(Block block) {
                TextureMapping textMap = textures.copyAndUpdate(BlockModels.STEP, new Material(twoStepTexture));
                MultiVariant inner = plainVariant(BlockModels.INNER_STAIRS.create(block, textMap, modelOutput));
                Identifier straight = BlockModels.STRAIGHT_STAIRS.create(block, textMap, modelOutput);
                MultiVariant outer = plainVariant(BlockModels.OUTER_STAIRS.create(block, textMap, modelOutput));
                blockStateOutput.accept(createStairs(block, inner, plainVariant(straight), outer));
                if (block.asItem() != Items.AIR) registerSimpleItemModel(block, straight);
                return this;
            }

            @Override
            public BlockFamilyProvider slab(Block block) {
                TextureMapping textMap = textures.copyAndUpdate(TextureSlot.SIDE, new Material(twoStepTexture));
                Identifier lower = ModelTemplates.SLAB_BOTTOM.create(block, textMap, modelOutput);
                MultiVariant upper = plainVariant(ModelTemplates.SLAB_TOP.create(block, textMap, modelOutput));
                blockStateOutput.accept(createSlab(block, plainVariant(lower), upper, plainVariant(baseModelId)));
                if (block.asItem() != Items.AIR) registerSimpleItemModel(block, lower);
                return this;
            }
        }).fullBlock(block, texturedModel.getTemplate());
    }

    public void registerTopsoil(Block block, Block dirt) {
        TexturedModel model = TexturedModel.CUBE_BOTTOM_TOP.get(dirt);
        createGrassLikeBlock(block,
                plainVariant(model.create(block, modelOutput)),
                plainVariant(ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(dirt, "_snow", model.getMapping()
                        .copyAndUpdate(SIDE, new Material(ModelLocationUtils.getModelLocation(dirt, "_side_snow_covered"))
                ), modelOutput))
        );
    }

    public void registerRotated(Block block, TexturedModel.Provider modelFactory) {
        Identifier modelId = modelFactory.get(block).create(block, modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(modelId))
                .with(UP_DEFAULT_ROTATION_OPERATIONS));
    }

    public void registerPlunderVine(Block plant, Block bud) {
        blockStateOutput.accept(MultiVariantGenerator.dispatch(bud, plainVariant(ModelLocationUtils.getModelLocation(bud)))
                .with(DOWN_DEFAULT_ROTATION_OPERATIONS));

        var supplier = MultiPartGenerator.multiPart(plant);
        String[] stages = { "", "_2", "_3", "_4", "_4"};
        BlockStateProperties.AGE_4.getPossibleValues().forEach(age -> {
            Identifier modelId = ModelLocationUtils.getModelLocation(plant, "_branch" + stages[age]);
            createDownDefaultFacingVariantMap((direction, variant) -> {
                supplier.with(condition().term(BlockStateProperties.AGE_4, age).term(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), true),
                        variant(variant.apply(plainModel(modelId)))
                );
            });
        });

        blockStateOutput.accept(supplier);
        registerSimpleItemModel(bud, ModelLocationUtils.getModelLocation(bud));
    }

    public void registerCompactedBlock(Block block) {
        for (ModelTemplate model : BlockModels.FLATTENED_MODELS) {
            model.create(block, TextureMapping.cube(new Material(ModelLocationUtils.getModelLocation(block).withPath(p -> p.replace("compacted_", "")))), modelOutput);
        }
        MultiPartGenerator supplier = MultiPartGenerator.multiPart(block);
        for (byte i = 0; i < BlockModels.FLATTENED_MODEL_ROTATIONS.length; i++) {
            final BooleanProperty yAxis = (i & 0b100) == 0 ? BlockStateProperties.DOWN : BlockStateProperties.UP;
            final BooleanProperty xAxis = (i & 0b010) == 0 ? BlockStateProperties.NORTH: BlockStateProperties.SOUTH;
            final BooleanProperty zAxis = (i & 0b001) == 0 ? BlockStateProperties.EAST : BlockStateProperties.WEST;
            final Quadrant xRot = yAxis == BlockStateProperties.DOWN ? R0 : R180;
            final Quadrant yRot = BlockModels.FLATTENED_MODEL_ROTATIONS[i];
            final String[] suffexes = yRot.ordinal() % 2 == 0 ? BlockModels.FLATTENED_MODEL_SUFFEXES : BlockModels.FLATTENED_MODEL_SUFFEXES_ROT;
            for (byte v = 0; v < suffexes.length; v++) {
                supplier.with(condition()
                            .term(yAxis, (v & 0b100) != 0)
                            .term(xAxis, (v & 0b010) != 0)
                            .term(zAxis, (v & 0b001) != 0), variant(plainModel(ModelLocationUtils.getModelLocation(block, "_corner_" + suffexes[v]))
                        .withUvLock(true)
                        .withXRot(xRot).withYRot(yRot))
                );
            }
        }
        blockStateOutput.accept(supplier);
    }

    public void registerChest(Block chest, Block particleSource) {
        createParticleOnlyBlock(chest, particleSource);
        Item item = chest.asItem();
        Identifier identifier = ModelTemplates.CHEST_INVENTORY.create(item, TextureMapping.particle(particleSource), modelOutput);
        itemModelOutput.accept(item, ItemModelUtils.specialModel(identifier, new CloudChestModelRenderer.Unbaked(CloudChestModelRenderer.TEXTURE, 0)));
    }

    public void registerFancyBed(Block bed, Block particleSource, boolean translucent) {
        createAirLikeBlock(bed, TextureMapping.getBlockTexture(particleSource));
        Item item = bed.asItem();
        Identifier itemModelId = ModelTemplates.PARTICLE_ONLY.create(ModelLocationUtils.getModelLocation(item), TextureMapping.particle(particleSource), modelOutput);
        this.itemModelOutput.accept(item, ItemModelUtils.specialModel(itemModelId, new CloudBedModelRenderer.Unbaked(Unicopia.id("textures/entity/bed/" + ((FancyBedBlock)bed).getBase() + ".png"), translucent)));
    }

    public void registerStableDoor(Block door) {
        var variants = PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.DOUBLE_BLOCK_HALF, BlockStateProperties.DOOR_HINGE, BlockStateProperties.OPEN);
        registerSimpleFlatItemModel(door.asItem());
        buildDoorStateModels(door, "", variants::select);
        blockStateOutput.accept(MultiVariantGenerator.dispatch(door).with(variants));
    }

    public void registerLockingDoor(Block door) {
        var variants = PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.DOUBLE_BLOCK_HALF, BlockStateProperties.DOOR_HINGE, BlockStateProperties.OPEN, BlockStateProperties.LOCKED);
        registerSimpleFlatItemModel(door.asItem());
        buildDoorStateModels(door, "", (facing, half, hinge, open, map) -> variants.select(facing, half, hinge, open, false, map));
        buildDoorStateModels(door, "_locked", (facing, half, hinge, open, map) -> variants.select(facing, half, hinge, open, true, map));
        blockStateOutput.accept(MultiVariantGenerator.dispatch(door).with(variants));
    }

    private void buildDoorStateModels(Block door, String suffex, DoorStateConsumer variants) {
        TextureMapping topTextures = new TextureMapping()
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(door, "_top" + suffex))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(door, "_bottom" + suffex));
        TextureMapping bottomTextures = topTextures.copyAndUpdate(TextureSlot.TOP, topTextures.get(TextureSlot.BOTTOM));
        fillStableDoorVariantMap(variants, DoubleBlockHalf.LOWER,
                BlockModels.DOOR_LEFT.createWithSuffix(door, "_bottom_left" + suffex, bottomTextures, modelOutput),
                BlockModels.DOOR_RIGHT.createWithSuffix(door, "_bottom_right" + suffex, bottomTextures, modelOutput)
        );
        fillStableDoorVariantMap(variants, DoubleBlockHalf.UPPER,
                BlockModels.DOOR_LEFT.createWithSuffix(door, "_top_left" + suffex, topTextures, modelOutput),
                BlockModels.DOOR_RIGHT.createWithSuffix(door, "_top_right" + suffex, topTextures, modelOutput)
        );
    }

    private static void fillStableDoorVariantMap(
            DoorStateConsumer variantMap,
            DoubleBlockHalf targetHalf, Identifier leftModelId, Identifier rightModelId) {
        fillStableDoorVariantMap(variantMap, targetHalf, DoorHingeSide.LEFT, false, R0, leftModelId);
        fillStableDoorVariantMap(variantMap, targetHalf, DoorHingeSide.RIGHT, false, R0, rightModelId);

        fillStableDoorVariantMap(variantMap, targetHalf, DoorHingeSide.LEFT, true, R90, rightModelId);
        fillStableDoorVariantMap(variantMap, targetHalf, DoorHingeSide.RIGHT, true, R270, leftModelId);
    }

    public static void fillStableDoorVariantMap(
            DoorStateConsumer variantMap,
            DoubleBlockHalf targetHalf,
            DoorHingeSide hinge, boolean open, Quadrant rotation,
            Identifier modelId) {

        for (int i = 0; i < BlockRotation.DIRECTIONS.length; i++) {
            variantMap.register(BlockRotation.DIRECTIONS[i], targetHalf, hinge, open, variant(plainModel(modelId).withYRot(BlockRotation.cycle(rotation, i))));
        }
    }

    interface DoorStateConsumer {
        void register(Direction direction, DoubleBlockHalf half, DoorHingeSide hinge, boolean open, MultiVariant variant);
    }

    public void registerPillar(Block pillar) {
        TextureMapping textures = new TextureMapping()
                .put(SIDE, new Material(ModelLocationUtils.getModelLocation(pillar, "_side")))
                .put(TOP, new Material(ModelLocationUtils.getModelLocation(pillar, "_lip")))
                .put(BOTTOM, new Material(ModelLocationUtils.getModelLocation(pillar, "_end")))
                .put(END, new Material(ModelLocationUtils.getModelLocation(pillar, "_side_end")));
        Identifier middle = BlockModels.TEMPLATE_PILLAR.create(pillar, textures, modelOutput);
        Identifier end = BlockModels.TEMPLATE_PILLAR_END.create(pillar, textures, modelOutput);
        blockStateOutput.accept(MultiPartGenerator.multiPart(pillar)
                .with(condition().term(BlockStateProperties.AXIS, Direction.Axis.X), variant(plainModel(middle).withXRot(R90).withYRot(R90)))
                .with(condition().term(BlockStateProperties.AXIS, Direction.Axis.X).term(BlockStateProperties.NORTH, false), variant(plainModel(end).withXRot(R270).withYRot(R90)))
                .with(condition().term(BlockStateProperties.AXIS, Direction.Axis.X).term(BlockStateProperties.SOUTH, false), variant(plainModel(end).withXRot(R90).withYRot(R90)))

                .with(condition().term(BlockStateProperties.AXIS, Direction.Axis.Y), plainVariant(middle))
                .with(condition().term(BlockStateProperties.AXIS, Direction.Axis.Y).term(BlockStateProperties.NORTH, false), variant(plainModel(end).withXRot(R180)))
                .with(condition().term(BlockStateProperties.AXIS, Direction.Axis.Y).term(BlockStateProperties.SOUTH, false), plainVariant(end))

                .with(condition().term(BlockStateProperties.AXIS, Direction.Axis.Z), variant(plainModel(middle).withXRot(R90)))
                .with(condition().term(BlockStateProperties.AXIS, Direction.Axis.Z).term(BlockStateProperties.NORTH, false), variant(plainModel(end).withXRot(R90)))
                .with(condition().term(BlockStateProperties.AXIS, Direction.Axis.Z).term(BlockStateProperties.SOUTH, false), variant(plainModel(end).withXRot(R270)))
        );
        registerSimpleItemModel(pillar.asItem(), TEMPLATE_PILLAR.create(ModelLocationUtils.getModelLocation(pillar.asItem()), textures, modelOutput));
    }

    public void registerHiveBlock(Block hive) {
        Identifier core = ModelLocationUtils.getModelLocation(hive, "_core");
        Identifier side = ModelLocationUtils.getModelLocation(hive, "_side");
        blockStateOutput.accept(MultiPartGenerator.multiPart(hive)
                .with(plainVariant(core))
                .with(condition().term(BlockStateProperties.NORTH, true), variant(plainModel(side).withUvLock(true)))
                .with(condition().term(BlockStateProperties.EAST, true), variant(plainModel(side).withUvLock(true).withYRot(R90)))
                .with(condition().term(BlockStateProperties.SOUTH, true), variant(plainModel(side).withUvLock(true).withYRot(R180)))
                .with(condition().term(BlockStateProperties.WEST, true), variant(plainModel(side).withUvLock(true).withYRot(R270)))
                .with(condition().term(BlockStateProperties.DOWN, true), variant(plainModel(side).withUvLock(true).withXRot(R90)))
                .with(condition().term(BlockStateProperties.UP, true), variant(plainModel(side).withUvLock(true).withXRot(R270))));
        registerSimpleItemModel(hive.asItem(), ModelTemplates.CUBE_ALL.create(ModelLocationUtils.getModelLocation(hive.asItem()), TextureMapping.cube(new Material(side)), modelOutput));
    }

    public void registerWithStages(Block crop, Property<Integer> ageProperty, BlockModels.Factory modelFactory, int ... stages) {
        if (ageProperty.getPossibleValues().size() != stages.length) {
            throw new IllegalArgumentException();
        }
        int offset = ageProperty.getPossibleValues().iterator().next();
        Int2ObjectOpenHashMap<Identifier> uploadedModels = new Int2ObjectOpenHashMap<>();
        blockStateOutput.accept(MultiVariantGenerator.dispatch(crop)
                .with(PropertyDispatch.initial(ageProperty)
                .generate(age -> plainVariant(uploadedModels.computeIfAbsent(stages[age - offset], stage -> {
                    return modelFactory.upload(crop, "_stage" + stage, modelOutput);
                })))));
    }

    public void registerWithStagesBuiltinModels(Block crop, Property<Integer> ageProperty, int ... stages) {
        if (ageProperty.getPossibleValues().size() != stages.length) {
            throw new IllegalArgumentException();
        }
        int offset = ageProperty.getPossibleValues().iterator().next();
        blockStateOutput.accept(MultiVariantGenerator.dispatch(crop)
                .with(PropertyDispatch.initial(ageProperty)
                .generate(age -> plainVariant(ModelLocationUtils.getModelLocation(crop, "_stage" + stages[age - offset])))));
        registerSimpleFlatItemModel(crop.asItem());
    }

    public <T extends Enum<T> & StringRepresentable> void registerTallCrop(Block crop,
            Property<Integer> ageProperty,
            EnumProperty<T> partProperty,
            int[] ... ageTextureIndices) {
        Map<String, Identifier> uploadedModels = new HashMap<>();
        blockStateOutput.accept(MultiVariantGenerator.dispatch(crop).with(PropertyDispatch.initial(partProperty, ageProperty).generate((part, age) -> {
            int i = ageTextureIndices[part.ordinal()][age];
            Identifier identifier = uploadedModels.computeIfAbsent("_" + part.getSerializedName() + "_stage" + i, variant -> createSuffixedVariant(crop, variant, ModelTemplates.CROSS, TextureMapping::cross));
            return plainVariant(identifier);
        })));
    }

    public void registerSlimeLayers(Block block) {
        Identifier fullModel = ModelLocationUtils.getModelLocation(Blocks.SLIME_BLOCK);
        blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockStateProperties.LAYERS)
                        .generate(height -> plainVariant(height < 8 ? ModelLocationUtils.getModelLocation(block, "_height" + height * 2) : fullModel))));
        if (block.asItem() != Items.AIR) registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block, "_height2"));
    }

    public void registerPie(Block pie) {
        TextureMapping textures = new TextureMapping()
                .put(TOP, new Material(ModelLocationUtils.getModelLocation(pie, "_top")))
                .put(BOTTOM, new Material(ModelLocationUtils.getModelLocation(pie, "_bottom")))
                .put(SIDE, new Material(ModelLocationUtils.getModelLocation(pie, "_side")))
                .put(INSIDE, new Material(ModelLocationUtils.getModelLocation(pie, "_inside")));
        TextureMapping stompedTextures = textures.copyAndUpdate(TOP, new Material(ModelLocationUtils.getModelLocation(pie, "_top_stomped")));
        blockStateOutput.accept(MultiVariantGenerator.dispatch(pie).with(PropertyDispatch.initial(PieBlock.BITES, PieBlock.STOMPED).generate((bites, stomped) -> {
            return plainVariant(BlockModels.PIE_MODELS[bites].createWithSuffix(pie, (stomped ? "_stomped" : ""), stomped ? stompedTextures : textures, modelOutput));
        })));
    }

    public void registerFloweringLeaves(Block block) {
        Identifier baseModel = TexturedModel.LEAVES.create(block, modelOutput);
        Identifier floweringModel = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_flowering", TextureMapping.singleSlot(ALL, new Material(ModelLocationUtils.getModelLocation(block, "_flowering"))), modelOutput);
        blockStateOutput.accept(MultiPartGenerator.multiPart(block)
                .with(plainVariant(baseModel))
                .with(condition().term(FruitBearingBlock.STAGE, FruitBearingBlock.Stage.FLOWERING), plainVariant(floweringModel)));
    }

    public void registerZapLeaves(Block block) {
        Identifier baseModel = TexturedModel.LEAVES.create(block, modelOutput);
        Identifier floweringModel = BuiltInRegistries.BLOCK.getKey(block).withPrefix("block/flowering_");
        Identifier airModel = ModelLocationUtils.getModelLocation(Blocks.AIR);
        blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(ZapAppleLeavesBlock.STAGE)
                .generate(stage -> plainVariant(switch (stage) {
                            case HIBERNATING -> airModel;
                            case FLOWERING -> floweringModel;
                            default -> baseModel;
                        }))));
    }

    public void registerSprout(Block sprout) {
        blockStateOutput.accept(MultiVariantGenerator.dispatch(sprout)
                .with(PropertyDispatch.initial(BlockStateProperties.AGE_7)
                .generate(age -> plainVariant(Unicopia.id("block/apple_sprout_stage" + age)))));
        registerSimpleFlatItemModel(sprout.asItem());
    }

    public void registerShell(Block shell) {
        blockStateOutput.accept(MultiVariantGenerator.dispatch(shell)
                .with(PropertyDispatch.initial(ShellsBlock.COUNT)
                .generate(count -> plainVariant(BlockModels.SHELL_MODELS[count - 1].create(shell, TextureMapping.singleSlot(BlockModels.SHELL, new Material(ModelLocationUtils.getModelLocation(shell))), modelOutput)))));
    }

    public void registerHull(Block block, Block core, Block shell) {
        blockStateOutput.accept(MultiVariantGenerator.dispatch(
                block,
                plainVariant(ModelTemplates.CUBE_BOTTOM_TOP.create(block, new TextureMapping()
                        .put(BOTTOM, new Material(ModelLocationUtils.getModelLocation(core)))
                        .put(TOP, new Material(ModelLocationUtils.getModelLocation(shell)))
                        .put(SIDE, new Material(ModelLocationUtils.getModelLocation(shell, "_half"))), modelOutput))
        ).with(UP_DEFAULT_ROTATION_OPERATIONS));
    }
}
