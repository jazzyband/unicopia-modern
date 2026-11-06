package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.Datagen;
import com.minelittlepony.unicopia.datagen.FarmersDelightContent;
import com.minelittlepony.unicopia.datagen.ItemFamilies;
import com.minelittlepony.unicopia.datagen.UBlockFamilies;
import com.minelittlepony.unicopia.item.BedsheetsItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.UTreeGen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class UItemTagProvider extends FabricTagsProvider.ItemTagsProvider {


    private final UBlockTagProvider blockTagProvider;

    public UItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, UBlockTagProvider blockTagProvider) {
        super(output, registriesFuture, blockTagProvider);
        this.blockTagProvider = blockTagProvider;
    }

    @Override
    protected RegistryEntryTagAppender<Item> tag(TagKey<Item> tag) {
        return new RegistryEntryTagAppender<>(super.tag(tag), item -> item.builtInRegistryHolder().key(),
                id -> ResourceKey.create(Registries.ITEM, id));
    }

    @Override
    public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        TagBuilder blockTagBuilder = Objects.requireNonNull(blockTagProvider, "Pass Block tag provider via constructor to use copy").getOrCreateRawBuilder(blockTag);
        TagBuilder itemTagBuilder = getOrCreateRawBuilder(itemTag);
        blockTagBuilder.build().forEach(entry -> {
            if (entry.verifyIfPresent(BuiltInRegistries.ITEM::containsKey, tagId -> getOrCreateRawBuilder(TagKey.create(Registries.ITEM, tagId)) != null)) {
                itemTagBuilder.add(entry);
            } else {
                Datagen.LOGGER.warn("Cannot copy missing entry {} to item tag {}", entry, itemTag.location());
            }
        });
    }

    @Override
    protected void addTags(Provider arg) {
        copyBlockTags();
        exportConventionalTags();
        tag(ItemTags.BOOKSHELF_BOOKS).add(UItems.SPELLBOOK);
        tag(ItemTags.BEDS).add(UItems.CLOTH_BED, UItems.CLOUD_BED);

        tag(ItemTags.CHEST_BOATS).add(UItems.PALM_CHEST_BOAT);
        tag(ItemTags.BOATS).add(UItems.PALM_BOAT);
        tag(ItemTags.CREEPER_DROP_MUSIC_DISCS).add(UItems.MUSIC_DISC_CRUSADE, UItems.MUSIC_DISC_FUNK, UItems.MUSIC_DISC_PET, UItems.MUSIC_DISC_POPULAR);

        tag(ItemTags.SIGNS).add(UBlocks.PALM_SIGN.asItem());
        tag(ItemTags.HANGING_SIGNS).add(UBlocks.PALM_HANGING_SIGN.asItem());

        tag(UTags.Items.HORSE_SHOES).add(ItemFamilies.HORSE_SHOES);
        tag(UTags.Items.POLEARMS).add(ItemFamilies.POLEARMS);

        tag(UTags.Items.BASKETS).add(ItemFamilies.BASKETS);
        tag(UTags.Items.BADGES).add(Race.REGISTRY.stream()
                .map(race -> race.getId().withPath(p -> p + "_badge"))
                .flatMap(id -> BuiltInRegistries.ITEM.getOptional(id).stream())
                .toArray(Item[]::new));
        tag(UTags.Items.WOOL_BED_SHEETS).add(BedsheetsItem.ITEMS.values().stream().filter(sheet -> sheet != UItems.KELP_BED_SHEETS).toArray(Item[]::new));
        tag(UTags.Items.BED_SHEETS).forceAddTag(UTags.Items.WOOL_BED_SHEETS).add(UItems.KELP_BED_SHEETS);
        tag(UTags.Items.APPLE_SEEDS).add(UItems.GREEN_APPLE_SEEDS, UItems.SWEET_APPLE_SEEDS, UItems.SOUR_APPLE_SEEDS);
        tag(ConventionalItemTags.SEEDS).forceAddTag(UTags.Items.APPLE_SEEDS);
        tag(UTags.Items.MAGIC_FEATHERS).add(UItems.PEGASUS_FEATHER, UItems.GRYPHON_FEATHER);
        tag(ConventionalItemTags.FEATHERS).forceAddTag(UTags.Items.MAGIC_FEATHERS);
        tag(UTags.Items.FRESH_APPLES).add(Items.APPLE, UItems.GREEN_APPLE, UItems.SWEET_APPLE, UItems.SOUR_APPLE);
        tag(UTags.Items.CLOUD_JARS).add(UItems.RAIN_CLOUD_JAR, UItems.STORM_CLOUD_JAR);
        tag(UTags.Items.PIES).add(UItems.APPLE_PIE, UItems.APPLE_PIE_HOOF);

        // technical tags
        tag(ItemTags.VILLAGER_PLANTABLE_SEEDS).addTag(UTags.Items.APPLE_SEEDS);
        tag(UTags.Items.CAN_CUT_PIE).forceAddTag(ConventionalItemTags.SHEAR_TOOLS).addOptionalTag(UConventionalTags.Items.TOOL_KNIVES);
        tag(UTags.Items.COOLS_OFF_KIRINS).add(Items.MELON_SLICE, UItems.JUICE).forceAddTag(ConventionalItemTags.WATER_BUCKETS);
        tag(UTags.Items.FALLS_SLOWLY).add(Items.FEATHER, UItems.CLOUD_LUMP).forceAddTag(UTags.Items.MAGIC_FEATHERS);
        tag(UTags.Items.IS_DELIVERED_AGGRESSIVELY).forceAddTag(ItemTags.ANVIL);
        tag(UTags.Items.SPOOKED_MOB_DROPS).add(Items.BRICK);
        tag(UTags.Items.TINTED_SHADES).add(
                UItems.SUNGLASSES
        );
        tag(UTags.Items.PROTECTS_BAT_PONY_EYES).add(
                Items.CARVED_PUMPKIN, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.PLAYER_HEAD,
                Items.ZOMBIE_HEAD, Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PIGLIN_HEAD,
                UItems.SUNGLASSES
        );
        tag(UTags.Items.FLOATS_ON_CLOUDS)
            .forceAddTag(UTags.Items.CLOUD_BEDS)
            .forceAddTag(UTags.Items.CLOUD_SLABS)
            .forceAddTag(UTags.Items.CLOUD_STAIRS)
            .forceAddTag(UTags.Items.CLOUD_BLOCKS)
            .add(UItems.CLOUD_LUMP);
        tag(UTags.Items.CONTAINER_WITH_LOVE).add(UItems.LOVE_BOTTLE, UItems.LOVE_BUCKET, UItems.LOVE_MUG);
        tag(UTags.Items.HAS_NO_TRAITS).add(
                Items.AIR, Items.SPAWNER, Items.STRUCTURE_VOID, Items.STRUCTURE_BLOCK,
                Items.COMMAND_BLOCK, Items.CHAIN_COMMAND_BLOCK, Items.REPEATING_COMMAND_BLOCK,
                Items.LIGHT, Items.JIGSAW, Items.BARRIER, Items.BEDROCK, Items.END_PORTAL_FRAME,
                Items.DEBUG_STICK, Items.COMMAND_BLOCK_MINECART,
                UItems.PLUNDER_VINE
        ).forceAddTag(UTags.Items.BADGES);
        tag(UTags.Items.LOOT_BUG_COMMON_DROPS).forceAddTag(ConventionalItemTags.NUGGETS);
        tag(UTags.Items.LOOT_BUG_RARE_DROPS).add(
                    Items.DIAMOND, Items.GOLDEN_APPLE, Items.GOLDEN_CARROT,
                    Items.GOLDEN_HELMET, Items.GOLDEN_BOOTS, Items.GOLDEN_LEGGINGS, Items.GOLDEN_CHESTPLATE,
                    Items.GOLDEN_HORSE_ARMOR,
                    Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE, Items.GOLDEN_SWORD, Items.GOLDEN_HOE,
                    UItems.GOLDEN_HORSE_SHOE, UItems.GOLDEN_POLEARM, UItems.GOLDEN_FEATHER, UItems.GOLDEN_WING,
                    UItems.GOLDEN_OAK_SEEDS
            ).forceAddTag(ConventionalItemTags.GOLD_INGOTS).forceAddTag(ConventionalItemTags.GOLD_RAW_MATERIALS).forceAddTag(ConventionalItemTags.STORAGE_BLOCKS_RAW_GOLD)
            .addOptionalTag(Identifier.parse("farmersdelight:golden_knife"));
        tag(UTags.Items.LOOT_BUG_EPIC_DROPS).add(
                Items.DIAMOND_BLOCK,
                Items.DIAMOND_HELMET, Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE,
                Items.DIAMOND_HORSE_ARMOR,
                Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_AXE, Items.DIAMOND_SWORD, Items.DIAMOND_HOE,
                UItems.DIAMOND_POLEARM
        ).forceAddTag(UTags.Items.BADGES).forceAddTag(ConventionalItemTags.GOLD_INGOTS);
        tag(UTags.Items.SHELLS).add(Items.NAUTILUS_SHELL, UItems.CLAM_SHELL, UItems.SCALLOP_SHELL, UItems.TURRET_SHELL);
        tag(UTags.Items.SPECIAL_SHELLS).add(UItems.SHELLY);
        tag(UTags.Items.ROCK_STEWS).add(UItems.ROCK_STEW);
        tag(UTags.Items.HIGH_QUALITY_SEA_VEGETABLES)
            .add(Items.DRIED_KELP_BLOCK, Items.GLOW_LICHEN)
            .forceAddTag(UConventionalTags.Items.CORAL_BLOCKS);
        tag(UTags.Items.LOW_QUALITY_SEA_VEGETABLES)
            .add(Items.KELP, Items.DRIED_KELP, Items.SEAGRASS, Items.SEA_PICKLE)
            .forceAddTag(UConventionalTags.Items.CORALS).forceAddTag(UConventionalTags.Items.CORAL_FANS);

        tag(ItemTags.MELEE_WEAPON_ENCHANTABLE).forceAddTag(UTags.Items.POLEARMS);
        tag(ItemTags.SHARP_WEAPON_ENCHANTABLE).addTag(UTags.Items.POLEARMS);
        tag(ItemTags.DURABILITY_ENCHANTABLE).addTag(UTags.Items.HORSE_SHOES);

        exportForagingTags();
        exportCreativeTabs();
        exportFarmersDelightItems();
    }

    private void copyBlockTags() {
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.LOGS, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        copy(BlockTags.SLABS, TagKey.create(Registries.ITEM, Identifier.parse("minecraft:slabs")));
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.STAIRS, TagKey.create(Registries.ITEM, Identifier.parse("minecraft:stairs")));
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.TRAPDOORS, TagKey.create(Registries.ITEM, Identifier.parse("minecraft:trapdoors")));
        copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);

        copy(UTags.Blocks.ZAP_LOGS, UTags.Items.ZAP_LOGS);
        copy(UTags.Blocks.WAXED_ZAP_LOGS, UTags.Items.WAXED_ZAP_LOGS);
        copy(UTags.Blocks.PALM_LOGS, UTags.Items.PALM_LOGS);
        copy(UTags.Blocks.GOLDEN_OAK_LOGS, UTags.Items.GOLDEN_OAK_LOGS);
        copy(UTags.Blocks.CLOUD_BEDS, UTags.Items.CLOUD_BEDS);
        copy(UTags.Blocks.CLOUD_SLABS, UTags.Items.CLOUD_SLABS);
        copy(UTags.Blocks.CLOUD_STAIRS, UTags.Items.CLOUD_STAIRS);
        copy(UTags.Blocks.CLOUD_BLOCKS, UTags.Items.CLOUD_BLOCKS);
        copy(UTags.Blocks.CHITIN_BLOCKS, UTags.Items.CHITIN_BLOCKS);

        copy(SereneSeasonsTags.Blocks.AUTUMN_CROPS, SereneSeasonsTags.Items.AUTUMN_CROPS);
        copy(SereneSeasonsTags.Blocks.WINTER_CROPS, SereneSeasonsTags.Items.WINTER_CROPS);
        copy(SereneSeasonsTags.Blocks.SPRING_CROPS, SereneSeasonsTags.Items.SPRING_CROPS);
        copy(SereneSeasonsTags.Blocks.SUMMER_CROPS, SereneSeasonsTags.Items.SUMMER_CROPS);
    }

    private void exportForagingTags() {
        tag(UTags.Items.FORAGE_BLINDING).add(Items.OXEYE_DAISY);
        tag(UTags.Items.FORAGE_DANGEROUS).add(Items.POPPY, Items.LILY_OF_THE_VALLEY);
        tag(UTags.Items.FORAGE_FILLING).add(Items.HAY_BLOCK);
        tag(UTags.Items.FORAGE_SAFE).add(Items.BLUE_ORCHID, Items.RED_TULIP, Items.ORANGE_TULIP,
                Items.PINK_TULIP, Items.CORNFLOWER, Items.PEONY, Items.SUNFLOWER, Items.DANDELION, Items.LILAC, Items.TALL_GRASS,
                Items.DEAD_BUSH, Items.PINK_PETALS
        ).forceAddTag(UConventionalTags.Items.MUSHROOMS).forceAddTag(ItemTags.SAPLINGS);
        tag(UTags.Items.FORAGE_NAUSEATING).add(Items.SHORT_GRASS, UItems.CIDER);
        tag(UTags.Items.FORAGE_PRICKLY).add(Items.ROSE_BUSH).forceAddTag(ItemTags.SAPLINGS);
        tag(UTags.Items.FORAGE_GLOWING).add(Items.AZURE_BLUET, Items.TORCHFLOWER);
        tag(UTags.Items.FORAGE_RISKY).add(Items.ALLIUM, Items.WHITE_TULIP, UItems.BURNED_JUICE);
        tag(UTags.Items.FORAGE_STRENGHENING).add(Items.FERN);
        tag(UTags.Items.FORAGE_SEVERE_NAUSEATING).add(Items.PITCHER_PLANT);
        tag(UTags.Items.FORAGE_SEVERE_PRICKLY).add(Items.LARGE_FERN);
        tag(UTags.Items.GROUP_FORAGING)
            .forceAddTag(UTags.Items.FORAGE_BLINDING)
            .forceAddTag(UTags.Items.FORAGE_DANGEROUS)
            .forceAddTag(UTags.Items.FORAGE_FILLING)
            .forceAddTag(UTags.Items.FORAGE_SAFE)
            .forceAddTag(UTags.Items.FORAGE_NAUSEATING)
            .forceAddTag(UTags.Items.FORAGE_PRICKLY)
            .forceAddTag(UTags.Items.FORAGE_GLOWING)
            .forceAddTag(UTags.Items.FORAGE_RISKY)
            .forceAddTag(UTags.Items.FORAGE_STRENGHENING)
            .forceAddTag(UTags.Items.FORAGE_SEVERE_NAUSEATING)
            .forceAddTag(UTags.Items.FORAGE_SEVERE_PRICKLY);
    }

    private void exportCreativeTabs() {
        tag(UTags.Items.GROUP_UNICORN).add(
                UItems.SPELLBOOK, UItems.GEMSTONE, UItems.BOTCHED_GEM, UItems.FRIENDSHIP_BRACELET,
                UItems.CRYSTAL_HEART, UItems.CRYSTAL_SHARD, UBlocks.CRYSTAL_DOOR.asItem(),
                UItems.MEADOWBROOKS_STAFF, UItems.MAGIC_STAFF, UItems.GROGARS_BELL,
                UItems.DRAGON_BREATH_SCROLL, UItems.PEGASUS_AMULET, UItems.ALICORN_AMULET,
                UItems.BROKEN_ALICORN_AMULET, UItems.UNICORN_AMULET, UItems.SPECTRAL_CLOCK
        );
        tag(UTags.Items.GROUP_PEGASUS)
            .add(UBlocks.SHAPING_BENCH.asItem(), UBlocks.CLOUD_CHEST.asItem(), UItems.CLOUD_LUMP)
            .add(List.of(UBlockFamilies.CLOUD, UBlockFamilies.CLOUD_PLANKS, UBlockFamilies.CLOUD_BRICKS, UBlockFamilies.DENSE_CLOUD, UBlockFamilies.ETCHED_CLOUD)
                    .stream()
                    .map(BlockFamily::getVariants)
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .map(ItemLike::asItem)
                    .filter(Objects::nonNull)
                    .toArray(Item[]::new))
            .add(UBlocks.UNSTABLE_CLOUD.asItem(), UBlocks.CLOUD_PILLAR.asItem(), UBlocks.CLOUD_DOOR.asItem(), UBlocks.CLOUD_BED.asItem())
            .forceAddTag(UTags.Items.BED_SHEETS)
            .forceAddTag(ConventionalItemTags.RAW_FISH_FOODS)
            .forceAddTag(ConventionalItemTags.COOKED_FISH_FOODS)
            .forceAddTag(UConventionalTags.Items.ROTTEN_FISH)
            .forceAddTag(UTags.Items.CLOUD_JARS)
            .add(UItems.LIGHTNING_JAR)
            .forceAddTag(UTags.Items.POLEARMS)
            .add(UItems.PEGASUS_FEATHER, UItems.GRYPHON_FEATHER, UItems.GOLDEN_FEATHER, UItems.GOLDEN_WING);
        tag(UTags.Items.GROUP_EARTH_PONY)
            .add(
                UBlocks.GREEN_APPLE_LEAVES.asItem(), UBlocks.SWEET_APPLE_LEAVES.asItem(), UBlocks.SOUR_APPLE_LEAVES.asItem(), UBlocks.ZAP_LEAVES.asItem(), UBlocks.FLOWERING_ZAP_LEAVES.asItem(),
                UBlocks.ZAP_LOG.asItem(), UBlocks.ZAP_WOOD.asItem(),
                UBlocks.STRIPPED_ZAP_LOG.asItem(), UBlocks.STRIPPED_ZAP_WOOD.asItem(),
                UBlocks.ZAP_PLANKS.asItem(), UBlocks.ZAP_STAIRS.asItem(), UBlocks.ZAP_SLAB.asItem(), UBlocks.ZAP_FENCE.asItem(), UBlocks.ZAP_FENCE_GATE.asItem(),

                UBlocks.WAXED_ZAP_LOG.asItem(), UBlocks.WAXED_ZAP_WOOD.asItem(),
                UBlocks.WAXED_STRIPPED_ZAP_LOG.asItem(), UBlocks.WAXED_STRIPPED_ZAP_WOOD.asItem(),
                UBlocks.WAXED_ZAP_PLANKS.asItem(), UBlocks.WAXED_ZAP_STAIRS.asItem(), UBlocks.WAXED_ZAP_SLAB.asItem(), UBlocks.WAXED_ZAP_FENCE.asItem(), UBlocks.WAXED_ZAP_FENCE_GATE.asItem(),

                UItems.CANDIED_APPLE, UBlocks.STABLE_DOOR.asItem(), UBlocks.DARK_OAK_DOOR.asItem())
            .forceAddTag(UTags.Items.FRESH_APPLES)
            .add(
                UItems.ZAP_APPLE, UItems.ZAP_BULB, UItems.ROTTEN_APPLE,
                UItems.GREEN_APPLE_SEEDS, UItems.SWEET_APPLE_SEEDS, UItems.SOUR_APPLE_SEEDS,
                UTreeGen.ZAP_APPLE_TREE.sapling().get().asItem(),
                UTreeGen.BANANA_TREE.sapling().get().asItem(),
                UItems.CURING_JOKE, UItems.MANGO, UItems.EMPTY_JAR, UItems.COOKED_ZAP_APPLE,
                UItems.ZAP_APPLE_JAM_JAR,
                UItems.OAT_SEEDS, UItems.OATS, UItems.IMPORTED_OATS,
                UItems.OATMEAL, UItems.OATMEAL_COOKIE, UItems.CHOCOLATE_OATMEAL_COOKIE,
                UItems.DAFFODIL_DAISY_SANDWICH, UItems.HAY_BURGER, UItems.HAY_BURGER, UItems.HAY_FRIES,
                UItems.CRISPY_HAY_FRIES, UItems.HORSE_SHOE_FRIES, UItems.WHEAT_WORMS,
                UItems.BAITED_FISHING_ROD, UBlocks.WORM_BLOCK.asItem(),
                UItems.MUFFIN, UItems.SCONE, UItems.ACORN, UItems.PINECONE, UItems.PINECONE_COOKIE,
                UItems.BOWL_OF_NUTS, UItems.CRYSTAL_SHARD, UItems.PEBBLES, UItems.ROCK, UItems.WEIRD_ROCK,
                UItems.ROCK_STEW, UItems.ROCK_CANDY, UItems.SALT_CUBE, UItems.MUG, UItems.CIDER, UItems.JUICE,
                UItems.BURNED_JUICE, UItems.TOAST, UItems.JAM_TOAST, UItems.BURNED_TOAST, UItems.APPLE_PIE,
                UItems.APPLE_PIE_HOOF, UItems.APPLE_PIE_SLICE, UBlocks.WEATHER_VANE.asItem()
            )
            .forceAddTag(UTags.Items.BASKETS)
            .add(UItems.GIANT_BALLOON, UBlocks.CLOTH_BED.asItem())
            .forceAddTag(UTags.Items.BED_SHEETS);
        tag(UTags.Items.GROUP_BAT_PONY)
            .forceAddTag(UConventionalTags.Items.RAW_INSECT)
            .forceAddTag(UConventionalTags.Items.COOKED_INSECT)
            .forceAddTag(UTags.Items.POLEARMS)
            .add(
                UBlocks.MANGO_LEAVES.asItem(), UTreeGen.MANGO_TREE.sapling().get().asItem(), UItems.MANGO,
                UItems.PINEAPPLE, UItems.PINEAPPLE_CROWN, UItems.BANANA, UItems.SUNGLASSES, UItems.BROKEN_SUNGLASSES
            );
        tag(UTags.Items.GROUP_CHANGELING)
            .add(
                UItems.CARAPACE, UBlocks.SURFACE_CHITIN.asItem(), UBlocks.CHITIN.asItem(),
                UBlockFamilies.CHISELED_CHITIN.getBaseBlock().asItem(),
                UBlockFamilies.POLISHED_CHITIN.getBaseBlock().asItem()
            )
            .add(UBlockFamilies.CHISELED_CHITIN.getVariants().values().stream().map(ItemLike::asItem).toArray(Item[]::new))
            .add(UBlockFamilies.POLISHED_CHITIN.getVariants().values().stream().map(ItemLike::asItem).toArray(Item[]::new))
            .add(
                UBlocks.CHISELLED_CHITIN_HULL.asItem(),
                UBlocks.POLISHED_CHITIN_HULL.asItem(),
                UBlocks.CHITIN_SPIKES.asItem(),
                UBlocks.SLIME_PUSTULE.asItem(),
                UBlocks.MYSTERIOUS_EGG.asItem(), UItems.GREEN_FRIED_EGG,
                UBlocks.HIVE.asItem(),
                UBlocks.SLIME.asItem()
            )
            .forceAddTag(ConventionalItemTags.RAW_MEAT_FOODS)
            .forceAddTag(ConventionalItemTags.COOKED_MEAT_FOODS)
            .forceAddTag(UConventionalTags.Items.ROTTEN_MEAT)
            .forceAddTag(UConventionalTags.Items.RAW_INSECT)
            .forceAddTag(UConventionalTags.Items.COOKED_INSECT)
            .forceAddTag(UConventionalTags.Items.ROTTEN_INSECT)
            .forceAddTag(UTags.Items.CONTAINER_WITH_LOVE);
        tag(UTags.Items.GROUP_SEA_PONY)
            .add(UItems.PEARL_NECKLACE)
            .forceAddTag(UTags.Items.SHELLS)
            .forceAddTag(UTags.Items.SPECIAL_SHELLS)
            .forceAddTag(UTags.Items.LOW_QUALITY_SEA_VEGETABLES)
            .forceAddTag(UTags.Items.HIGH_QUALITY_SEA_VEGETABLES);
    }

    private void exportConventionalTags() {
        copy(ConventionalBlockTags.CONCRETES, UConventionalTags.Items.CONCRETES);
        copy(UConventionalTags.Blocks.CORAL_BLOCKS, UConventionalTags.Items.CORAL_BLOCKS);
        copy(UConventionalTags.Blocks.CORAL_FANS, UConventionalTags.Items.CORAL_FANS);
        copy(UConventionalTags.Blocks.CORALS, UConventionalTags.Items.CORALS);
        tag(UConventionalTags.Items.ACORNS).add(UItems.ACORN);
        tag(UConventionalTags.Items.APPLES)
            .add(Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, UItems.ROTTEN_APPLE)
            .forceAddTag(UTags.Items.FRESH_APPLES)
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "pyrite_apples")) // no idea which mod add pyrite apples
        ;
        tag(ConventionalItemTags.WOODEN_RODS).add(UItems.GOLDEN_STICK);
        tag(UConventionalTags.Items.BANANAS).add(UItems.BANANA);
        tag(ConventionalItemTags.RAW_FISH_FOODS).addOptionalTag(Identifier.fromNamespaceAndPath("c", "mollusks"));
        tag(ConventionalItemTags.COOKED_FISH_FOODS).add(UItems.COOKED_TROPICAL_FISH, UItems.COOKED_PUFFERFISH, UItems.FRIED_AXOLOTL);
        tag(UConventionalTags.Items.ROTTEN_FISH).add(UItems.ROTTEN_COD, UItems.ROTTEN_TROPICAL_FISH, UItems.ROTTEN_SALMON, UItems.ROTTEN_PUFFERFISH);
        tag(ItemTags.FISHES).add(
                UItems.COOKED_TROPICAL_FISH, UItems.COOKED_PUFFERFISH, UItems.FRIED_AXOLOTL,
                UItems.ROTTEN_COD, UItems.ROTTEN_TROPICAL_FISH, UItems.ROTTEN_SALMON, UItems.ROTTEN_PUFFERFISH
        );
        tag(ConventionalItemTags.COOKED_MEAT_FOODS)
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "cooked_bacon"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "cooked_beef"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "cooked_chicken"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "cooked_mutton"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "cooked_pork"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "fried_chickens"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "hamburgers"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "pork_and_beans"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "pork_jerkies"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "protien"));
        tag(ConventionalItemTags.RAW_MEAT_FOODS)
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "raw_bacon"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "raw_beef"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "raw_chicken"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "raw_mutton"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "raw_pork"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "lemon_chickens"));
        tag(UConventionalTags.Items.ROTTEN_MEAT).add(Items.ROTTEN_FLESH);
        tag(UConventionalTags.Items.ROTTEN_INSECT).add(Items.FERMENTED_SPIDER_EYE);
        tag(UConventionalTags.Items.COOKED_INSECT).add(UItems.COOKED_FROG_LEGS);
        tag(UConventionalTags.Items.RAW_INSECT).add(Items.SPIDER_EYE, UItems.BUTTERFLY, UItems.FROG_LEGS, UItems.WHEAT_WORMS, UBlocks.WORM_BLOCK.asItem());
        tag(UConventionalTags.Items.WORMS).add(UItems.WHEAT_WORMS);
        tag(UConventionalTags.Items.ROCKS).add(UItems.ROCK);
        tag(UConventionalTags.Items.GEMS).add(UItems.GEMSTONE, UItems.BOTCHED_GEM);
        tag(UConventionalTags.Items.PINECONES).add(UItems.PINECONE);
        tag(UConventionalTags.Items.PINEAPPLES).add(UItems.PINEAPPLE);
        tag(UConventionalTags.Items.MANGOES).add(UItems.MANGO);
        tag(UConventionalTags.Items.MUSHROOMS).add(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM);
        tag(UConventionalTags.Items.MUFFINS).add(UItems.MUFFIN);
        tag(UConventionalTags.Items.SEEDS).add(Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TORCHFLOWER_SEEDS, Items.WHEAT_SEEDS)
            .add(UItems.OAT_SEEDS)
            .forceAddTag(UTags.Items.APPLE_SEEDS);
        tag(UConventionalTags.Items.COOKIES).add(Items.COOKIE, UItems.OATMEAL_COOKIE, UItems.CHOCOLATE_OATMEAL_COOKIE, UItems.PINECONE_COOKIE);
        tag(UConventionalTags.Items.OATMEALS).add(UItems.OATMEAL);
        tag(UConventionalTags.Items.GRAIN).add(Items.WHEAT, UItems.OATS);
        tag(UConventionalTags.Items.NUTS).add(UItems.BOWL_OF_NUTS)
            .addOptionalTag(UConventionalTags.Items.CROPS_PEANUTS)
            .forceAddTag(UConventionalTags.Items.ACORNS)
            .addOptional(Identifier.fromNamespaceAndPath("garnished", "nuts"))
            .addOptional(Identifier.fromNamespaceAndPath("garnished", "nut_mix"))
            .addOptional(Identifier.fromNamespaceAndPath("garnished", "neverable_delecacies"));
        tag(ConventionalItemTags.FRUIT_FOODS)
            .add(Items.MELON_SLICE, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.CHORUS_FRUIT)
            .add(UItems.JUICE, UItems.ZAP_APPLE, UItems.ZAP_BULB)
            .forceAddTag(UConventionalTags.Items.MANGOES)
            .forceAddTag(UConventionalTags.Items.PINEAPPLES)
            .forceAddTag(UConventionalTags.Items.APPLES)
            .forceAddTag(UConventionalTags.Items.BANANAS)
            .addOptionalTag(Identifier.fromNamespaceAndPath("garnished", "berries"));
        tag(UConventionalTags.Items.DESSERTS).add(Items.CAKE, UItems.APPLE_PIE_SLICE).forceAddTag(UTags.Items.PIES);
        tag(ConventionalItemTags.CANDY_FOODS).add(Items.SUGAR, UItems.ROCK_CANDY, UItems.CANDIED_APPLE);
        tag(UTags.Items.BAKED_GOODS).add(
                Items.BREAD, Items.COOKIE, Items.PUMPKIN_PIE,
                UItems.MUFFIN, UItems.SCONE, UItems.COOKED_ZAP_APPLE, UItems.TOAST, UItems.BURNED_TOAST, UItems.JAM_TOAST, UItems.IMPORTED_OATS,
                UItems.HAY_FRIES, UItems.CRISPY_HAY_FRIES, UItems.HORSE_SHOE_FRIES)
            .forceAddTag(UConventionalTags.Items.OATMEALS)
            .forceAddTag(UConventionalTags.Items.COOKIES);
    }



    private void exportFarmersDelightItems() {
        tag(UTags.Items.COOLS_OFF_KIRINS)
            .addOptional(FarmersDelightContent.id("melon_popsicle"))
            .addOptional(FarmersDelightContent.id("melon_juice"));
        tag(FarmersDelightContent.CABBAGE_ROLE_INGREDIENTS).add(UItems.OATS, UItems.ROCK, UItems.WHEAT_WORMS);
        tag(FarmersDelightContent.COMFORT_FOODS).add(UItems.OATMEAL, UItems.ROCK_STEW, UItems.MUFFIN);
        tag(ConventionalItemTags.RAW_FISH_FOODS)
            .addOptional(FarmersDelightContent.id("cod_roll"))
            .addOptional(FarmersDelightContent.id("salmon_roll"))
            .addOptional(FarmersDelightContent.id("cod_slice"))
            .addOptional(FarmersDelightContent.id("salmon_slice"));
        tag(ConventionalItemTags.COOKED_FISH_FOODS)
            .addOptional(FarmersDelightContent.id("fish_stew"))
            .addOptional(FarmersDelightContent.id("baked_cod_stew"))
            .addOptional(FarmersDelightContent.id("grilled_salmon"));
        tag(ConventionalItemTags.RAW_MEAT_FOODS)
            .addOptional(FarmersDelightContent.id("ham"));
        tag(ConventionalItemTags.COOKED_MEAT_FOODS)
            .addOptional(FarmersDelightContent.id("chicken_soup"))
            .addOptional(FarmersDelightContent.id("bacon_and_eggs"))
            .addOptional(FarmersDelightContent.id("pasta_with_meatballs"))
            .addOptional(FarmersDelightContent.id("beef_stew"))
            .addOptional(FarmersDelightContent.id("bone_broth"))
            .addOptional(FarmersDelightContent.id("mutton_wrap"))
            .addOptional(FarmersDelightContent.id("bacon_sandwich"))
            .addOptional(FarmersDelightContent.id("hamburger"))
            .addOptional(FarmersDelightContent.id("chicken_sandwich"))
            .addOptional(FarmersDelightContent.id("barbecue_stick"))
            .addOptional(FarmersDelightContent.id("smoked_ham"))
            .addOptional(FarmersDelightContent.id("honey_glazed_ham"))
            .addOptional(FarmersDelightContent.id("honey_glazed_ham_block"))
            .addOptional(FarmersDelightContent.id("roast_chicken"))
            .addOptional(FarmersDelightContent.id("roast_chicken_block"))
            .addOptional(FarmersDelightContent.id("steak_and_potatoes"))
            .addOptional(FarmersDelightContent.id("roasted_mutton_chops"))
            .addOptional(FarmersDelightContent.id("pasta_with_mutton_chop"));
        tag(ConventionalItemTags.FRUIT_FOODS)
            .addOptional(FarmersDelightContent.id("pumpkin_slice"))
            .addOptional(FarmersDelightContent.id("tomato"))
            .addOptional(FarmersDelightContent.id("melon_juice"))
            .addOptional(FarmersDelightContent.id("fruit_salad"));
        tag(UConventionalTags.Items.DESSERTS)
            .addOptional(FarmersDelightContent.id("sweet_berry_cheesecake"))
            .addOptional(FarmersDelightContent.id("sweet_berry_cheesecake_slice"))
            .addOptional(FarmersDelightContent.id("chocolate_pie_slice"))
            .addOptional(FarmersDelightContent.id("cake_slice"))
            .addOptional(FarmersDelightContent.id("apple_pie_slice"))
            .addOptional(FarmersDelightContent.id("glow_berry_custard"));
        tag(UConventionalTags.Items.COOKIES)
            .addOptional(FarmersDelightContent.id("sweet_berry_cookie"))
            .addOptional(FarmersDelightContent.id("honey_cookie"));
        tag(UTags.Items.BAKED_GOODS)
            .addOptional(FarmersDelightContent.id("wheat_dough"))
            .addOptional(FarmersDelightContent.id("raw_pasta"))
            .addOptional(FarmersDelightContent.id("pie_crust"))
            .addOptional(FarmersDelightContent.id("egg_sandwich"));
        tag(UTags.Items.HIGH_QUALITY_SEA_VEGETABLES)
            .addOptional(FarmersDelightContent.id("kelp_roll"));
        tag(UTags.Items.LOW_QUALITY_SEA_VEGETABLES)
            .addOptional(FarmersDelightContent.id("kelp_roll_slice"));
        tag(UTags.Items.FORAGE_FILLING)
            .addOptional(FarmersDelightContent.id("horse_feed"))
            .addOptional(FarmersDelightContent.id("rice_bale"))
            .addOptional(FarmersDelightContent.id("straw_bale"));
        tag(UTags.Items.FORAGE_SAFE)
            .addOptional(FarmersDelightContent.id("sandy_shrub"))
            .addOptional(FarmersDelightContent.id("wild_cabbages"))
            .addOptional(FarmersDelightContent.id("wild_onions"))
            .addOptional(FarmersDelightContent.id("wild_carrots"))
            .addOptional(FarmersDelightContent.id("wild_beetroots"))
            .addOptional(FarmersDelightContent.id("wild_rice"));
        tag(UTags.Items.FORAGE_RISKY)
            .addOptional(FarmersDelightContent.id("wild_tomatoes"))
            .addOptional(FarmersDelightContent.id("wild_potatoes"))
            .addOptionalTag(Identifier.fromNamespaceAndPath("c", "meads"));
    }
}
