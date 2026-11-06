package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.UWoodTypes;
import com.minelittlepony.unicopia.block.cloud.CloudBedBlock;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.cloud.CloudBedItem;
import com.minelittlepony.unicopia.item.component.BalloonDesignComponent;
import com.minelittlepony.unicopia.item.component.BreaksIntoItemComponent;
import com.minelittlepony.unicopia.item.component.ConversionComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.item.group.UItemGroups;
import com.minelittlepony.unicopia.recipe.URecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.WoodType;

import static com.minelittlepony.unicopia.item.group.ItemGroupRegistry.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageTypes;

public interface UItems {
    Item GREEN_APPLE = register("green_apple", s -> AppleItem.registerTickCallback(new Item(s.compostable(UCompostables.MEDIUM).food(Foods.APPLE))), CreativeModeTabs.FOOD_AND_DRINKS);
    Item SWEET_APPLE = register("sweet_apple", s -> AppleItem.registerTickCallback(new Item(s.compostable(UCompostables.MEDIUM).food(Foods.APPLE))), CreativeModeTabs.FOOD_AND_DRINKS);
    Item SOUR_APPLE = register("sour_apple", s -> AppleItem.registerTickCallback(new Item(s.compostable(UCompostables.MEDIUM).food(Foods.APPLE))), CreativeModeTabs.FOOD_AND_DRINKS);

    Item ZAP_APPLE = register("zap_apple", s -> AppleItem.registerTickCallback(new ZapAppleItem(s.compostable(UCompostables.ALWAYS).food(UFoodComponents.ZAP_APPLE, UConsumableComponents.SNACK))), CreativeModeTabs.FOOD_AND_DRINKS);
    Item ZAP_BULB = register("zap_bulb", s -> new Item(s.compostable(UCompostables.ALWAYS).food(UFoodComponents.ZAP_BULB, UConsumableComponents.ZAP_BULB)), CreativeModeTabs.FOOD_AND_DRINKS);

    Item ROTTEN_APPLE = register("rotten_apple", s -> new Item(s.cookingFuel(UFuels.ticks(150)).compostable(UCompostables.HIGH).food(Foods.APPLE)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item COOKED_ZAP_APPLE = register("cooked_zap_apple", s -> new Item(s.food(Foods.APPLE)), CreativeModeTabs.FOOD_AND_DRINKS);

    Item MUSIC_DISC_CRUSADE = register("music_disc_crusade", UJukeboxSongs.CRUSADE);
    Item MUSIC_DISC_PET = register("music_disc_pet", UJukeboxSongs.PET);
    Item MUSIC_DISC_POPULAR = register("music_disc_popular", UJukeboxSongs.POPULAR);
    Item MUSIC_DISC_FUNK = register("music_disc_funk", UJukeboxSongs.FUNK);

    Item FRIENDSHIP_BRACELET = register("friendship_bracelet", s -> new FriendshipBraceletItem(s.rarity(Rarity.UNCOMMON)), CreativeModeTabs.TOOLS_AND_UTILITIES);

    Item PLUNDER_VINE = register("plunder_vine", s -> new BlockItem(UBlocks.PLUNDER_VINE_BUD, s));
    Item EMPTY_JAR = register("empty_jar", s -> new EmptyJarItem(UBlocks.JAR, s.fireResistant()), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Item FILLED_JAR = register("filled_jar", s -> new FilledJarItem(s.stacksTo(16).fireResistant().craftRemainder(EMPTY_JAR)));
    Item RAIN_CLOUD_JAR  = register("rain_cloud_jar", s -> new WeatherJarItem(UBlocks.CLOUD_JAR, s.stacksTo(16).fireResistant().craftRemainder(EMPTY_JAR), WeatherJarItem.Type.RAIN), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Item STORM_CLOUD_JAR  = register("storm_cloud_jar", s -> new WeatherJarItem(UBlocks.STORM_JAR, s.stacksTo(16).fireResistant().craftRemainder(EMPTY_JAR), WeatherJarItem.Type.THUNDER), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Item LIGHTNING_JAR  = register("lightning_jar", s -> new WeatherJarItem(UBlocks.LIGHTNING_JAR, s.stacksTo(16).fireResistant().craftRemainder(EMPTY_JAR), WeatherJarItem.Type.LIGHTNING), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Item ZAP_APPLE_JAM_JAR = register("zap_apple_jam_jar", s -> new WeatherJarItem(UBlocks.ZAP_JAR, s.stacksTo(16).fireResistant().craftRemainder(EMPTY_JAR), WeatherJarItem.Type.LIGHTNING), CreativeModeTabs.FUNCTIONAL_BLOCKS);

    Item TOAST = register("toast", s -> new Item(s.stacksTo(16).food(UFoodComponents.TOAST, UConsumableComponents.SNACK)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item BURNED_TOAST = register("burned_toast", s -> new Item(s.cookingFuel(UFuels.ticks(1600)).stacksTo(16).food(UFoodComponents.BURNED_TOAST, UConsumableComponents.SNACK)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item JAM_TOAST = register("jam_toast", s -> new Item(s.stacksTo(16).food(UFoodComponents.JAM_TOAST, UConsumableComponents.SNACK)), CreativeModeTabs.FOOD_AND_DRINKS);

    Item CRYSTAL_HEART = register("crystal_heart", s -> new CrystalHeartItem(s.stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item CRYSTAL_SHARD = register("crystal_shard", Item::new, CreativeModeTabs.NATURAL_BLOCKS);

    Item GEMSTONE = register("gemstone", GemstoneItem::new, CreativeModeTabs.NATURAL_BLOCKS);
    Item BOTCHED_GEM = register("botched_gem", Item::new, CreativeModeTabs.NATURAL_BLOCKS);

    Item PEGASUS_FEATHER = register("pegasus_feather", Item::new, CreativeModeTabs.NATURAL_BLOCKS);
    Item GRYPHON_FEATHER = register("gryphon_feather", Item::new, CreativeModeTabs.NATURAL_BLOCKS);

    Item OAT_SEEDS = register("oat_seeds", s -> new BlockItem(UBlocks.OATS, s.compostable(UCompostables.LOW).overrideDescription(UBlocks.OATS.getDescriptionId())), CreativeModeTabs.NATURAL_BLOCKS);
    Item OATS = register("oats", s -> new Item(s.compostable(UCompostables.VERY_LOW).food(UFoodComponents.OATS)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item IMPORTED_OATS = register("imported_oats", s -> new Item(s.compostable(UCompostables.LOW_MEDIUM).food(UFoodComponents.IMPORTED_OATS).component(UDataComponentTypes.ITEM_AFTER_DRAGON_BREATH, new ConversionComponent(ResourceKey.create(Registries.ITEM, Unicopia.id("imported_oats"))))), CreativeModeTabs.FOOD_AND_DRINKS);
    Item OATMEAL = register("oatmeal", s -> new Item(s.craftRemainder(Items.BOWL).usingConvertsTo(Items.BOWL).stacksTo(1).food(UFoodComponents.OATMEAL, UConsumableComponents.OATMEAL)), CreativeModeTabs.FOOD_AND_DRINKS);

    Item OATMEAL_COOKIE = register("oatmeal_cookie", s -> new Item(s.food(UFoodComponents.OATMEAL_COOKIE)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item CHOCOLATE_OATMEAL_COOKIE = register("chocolate_oatmeal_cookie", s -> new Item(s.food(UFoodComponents.CHOCOLATE_OATMEAL_COOKIE)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item PINECONE_COOKIE = register("pinecone_cookie", s -> new Item(s.food(Foods.COOKIE)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item BOWL_OF_NUTS = register("bowl_of_nuts", s -> new Item(s.food(UFoodComponents.NUT_BOWL).craftRemainder(Items.BOWL).usingConvertsTo(Items.BOWL)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item SCONE = register("scone", s -> new MuffinItem(s.stacksTo(32).food(UFoodComponents.SCONE), 0), CreativeModeTabs.FOOD_AND_DRINKS);

    Item DAFFODIL_DAISY_SANDWICH = register("daffodil_daisy_sandwich", s -> new Item(s.compostable(UCompostables.LOW_MEDIUM).food(UFoodComponents.DAFODIL_DAISY_SANDWICH)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item HAY_BURGER = register("hay_burger", s -> new Item(s.compostable(UCompostables.LOW_MEDIUM).stacksTo(1).food(UFoodComponents.BURGER)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item HAY_FRIES = register("hay_fries", s -> new Item(s.compostable(UCompostables.LOW_MEDIUM).stacksTo(16).food(UFoodComponents.HAY_FRIES)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item CRISPY_HAY_FRIES = register("crispy_hay_fries", s -> new Item(s.stacksTo(16).food(UFoodComponents.CRISPY_HAY_FRIES)), CreativeModeTabs.FOOD_AND_DRINKS);
    /**
     * https://mlp.fandom.com/wiki/Food_and_beverage
     */
    Item HORSE_SHOE_FRIES = register("horse_shoe_fries", s -> new Item(s.stacksTo(32).food(UFoodComponents.HAY_FRIES)), CreativeModeTabs.FOOD_AND_DRINKS);

    Item WHEAT_WORMS = register("wheat_worms", s -> new Item(s.compostable(UCompostables.HIGH).stacksTo(16).food(UFoodComponents.WORMS)), CreativeModeTabs.NATURAL_BLOCKS); //ItemTags.WOLF_FOOD
    Item BAITED_FISHING_ROD = register("baited_fishing_rod", s -> new BaitedFishingRodItem(s.durability(64)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item MUFFIN = register("muffin", s -> new MuffinItem(s.compostable(UCompostables.LOW).stacksTo(32).food(Foods.BREAD), 0), CreativeModeTabs.FOOD_AND_DRINKS);
    Item PINECONE = register("pinecone", s -> new ForageableItem(s.compostable(UCompostables.LOW).food(UFoodComponents.PINECONE, UConsumableComponents.SNACK).stacksTo(16), () -> Blocks.SPRUCE_LEAVES), CreativeModeTabs.FOOD_AND_DRINKS);
    Item ACORN = register("acorn", s -> new ForageableItem(s.compostable(UCompostables.LOW).food(UFoodComponents.ACORN, UConsumableComponents.SNACK).stacksTo(16), () -> Blocks.OAK_LEAVES), CreativeModeTabs.FOOD_AND_DRINKS);
    Item MANGO = register("mango", s -> new Item(s.food(UFoodComponents.MANGO)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item BANANA = register("banana", s -> new Item(s.food(UFoodComponents.BANANA)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item CURING_JOKE = register("curing_joke", s -> new CuringJokeItem(UBlocks.CURING_JOKE, s.food(UFoodComponents.POISON_JOKE, UConsumableComponents.SNACK)), CreativeModeTabs.NATURAL_BLOCKS);
    Item PINEAPPLE = register("pineapple", s -> new PineappleItem(s.food(UFoodComponents.BANANA).durability(3)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item PINEAPPLE_CROWN = register("pineapple_crown", s -> new BlockItem(UBlocks.PINEAPPLE, s.overrideDescription(UBlocks.PINEAPPLE.getDescriptionId())), CreativeModeTabs.NATURAL_BLOCKS);

    Item PEBBLES = register("pebbles", s -> new BlockItem(UBlocks.ROCKS, s.overrideDescription(UBlocks.ROCKS.getDescriptionId())), CreativeModeTabs.NATURAL_BLOCKS);
    Item ROCK = register("rock", s -> new HeavyProjectileItem(s, 3), CreativeModeTabs.NATURAL_BLOCKS);
    Item WEIRD_ROCK = register("weird_rock", s -> new Item(s.attributes(ItemAttributeModifiers.builder()
            .add(Attributes.LUCK, new AttributeModifier(UItemModifierIds.LUCK_MODIFIER_ID, 9, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build())), CreativeModeTabs.NATURAL_BLOCKS);
    Item TOM = register("tom", s -> new Item(s.attributes(ItemAttributeModifiers.builder()
            .add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(UItemModifierIds.KNOCKBACK_MODIFIER_ID, 0.9, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build())), CreativeModeTabs.NATURAL_BLOCKS);
    Item ROCK_STEW = register("rock_stew", s -> new Item(s.food(Foods.MUSHROOM_STEW).stacksTo(1).craftRemainder(Items.BOWL).usingConvertsTo(Items.BOWL)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item ROCK_CANDY = register("rock_candy", s -> new Item(s.food(UFoodComponents.CANDY).stacksTo(16)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item SALT_CUBE = register("salt_cube", s -> new Item(s.food(UFoodComponents.SALT_CUBE)), CreativeModeTabs.FOOD_AND_DRINKS);

    Item GREEN_APPLE_SEEDS = register("green_apple_seeds", s -> new BlockItem(UBlocks.GREEN_APPLE_SPROUT, s.compostable(UCompostables.LOW)), CreativeModeTabs.NATURAL_BLOCKS);
    Item SWEET_APPLE_SEEDS = register("sweet_apple_seeds", s -> new BlockItem(UBlocks.SWEET_APPLE_SPROUT, s.compostable(UCompostables.LOW)), CreativeModeTabs.NATURAL_BLOCKS);
    Item SOUR_APPLE_SEEDS = register("sour_apple_seeds", s -> new BlockItem(UBlocks.SOUR_APPLE_SPROUT, s.compostable(UCompostables.LOW)), CreativeModeTabs.NATURAL_BLOCKS);
    Item GOLDEN_OAK_SEEDS = register("golden_oak_seeds", s -> new BlockItem(UBlocks.GOLDEN_OAK_SPROUT, s), CreativeModeTabs.NATURAL_BLOCKS);

    Item MUG = register("mug", s -> new Item(s.cookingFuel(UFuels.ticks(250))), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item CIDER = register("cider", s -> new ConsumableItem(s.food(UFoodComponents.CIDER).stacksTo(16).craftRemainder(MUG).usingConvertsTo(MUG), ItemUseAnimation.DRINK), CreativeModeTabs.FOOD_AND_DRINKS);
    Item JUICE = register("juice", s -> new ConsumableItem(s.craftRemainder(Items.GLASS_BOTTLE).usingConvertsTo(Items.GLASS_BOTTLE).stacksTo(16).food(UFoodComponents.JUICE), ItemUseAnimation.DRINK), CreativeModeTabs.FOOD_AND_DRINKS);
    Item BURNED_JUICE = register("burned_juice", s -> new ConsumableItem(s.craftRemainder(Items.GLASS_BOTTLE).usingConvertsTo(Items.GLASS_BOTTLE).stacksTo(16).food(UFoodComponents.BURNED_JUICE), ItemUseAnimation.DRINK), CreativeModeTabs.FOOD_AND_DRINKS);
    Item APPLE_PIE = register("apple_pie", s -> new BlockItem(UBlocks.APPLE_PIE, s.compostable(UCompostables.LOW_MEDIUM).stacksTo(1)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item APPLE_PIE_HOOF = register("apple_pie_hoof", s -> new BlockItem(UBlocks.APPLE_PIE, s.compostable(UCompostables.LOW_MEDIUM).stacksTo(1)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item APPLE_PIE_SLICE = register("apple_pie_slice", s -> new Item(s.compostable(UCompostables.VERY_LOW).stacksTo(16).food(UFoodComponents.PIE)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item CANDIED_APPLE = register("candied_apple", s -> new StagedFoodItem(s.food(UFoodComponents.CANDY).durability(3), () -> Items.STICK), CreativeModeTabs.FOOD_AND_DRINKS);

    Item LOVE_BOTTLE = register("love_bottle", s -> new ConsumableItem(s.food(UFoodComponents.LOVE_BOTTLE, Consumables.DEFAULT_DRINK).stacksTo(1).craftRemainder(Items.GLASS_BOTTLE).usingConvertsTo(Items.GLASS_BOTTLE), ItemUseAnimation.DRINK), CreativeModeTabs.FOOD_AND_DRINKS);
    Item LOVE_BUCKET = register("love_bucket", s -> new ConsumableItem(s.food(UFoodComponents.LOVE_BUCKET, Consumables.DEFAULT_DRINK).craftRemainder(Items.BUCKET).usingConvertsTo(Items.BUCKET), ItemUseAnimation.DRINK), CreativeModeTabs.FOOD_AND_DRINKS);
    Item LOVE_MUG = register("love_mug", s -> new ConsumableItem(s.food(UFoodComponents.LOVE_MUG, Consumables.DEFAULT_DRINK).craftRemainder(MUG).usingConvertsTo(MUG), ItemUseAnimation.DRINK), CreativeModeTabs.FOOD_AND_DRINKS);

    Item GOLDEN_FEATHER = register("golden_feather", s -> new Item(s.rarity(Rarity.UNCOMMON)), CreativeModeTabs.NATURAL_BLOCKS);
    Item GOLDEN_WING = register("golden_wing", s -> new Item(s.rarity(Rarity.UNCOMMON)), CreativeModeTabs.NATURAL_BLOCKS);

    Item DRAGON_BREATH_SCROLL = register("dragon_breath_scroll", s -> new DragonBreathScrollItem(s.cookingFuel(UFuels.ticks(20000)).rarity(Rarity.UNCOMMON)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item GROGARS_BELL = register("grogars_bell", s -> new BellItem(s.rarity(Rarity.RARE).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item MEADOWBROOKS_STAFF = register("meadowbrooks_staff", s -> new StaffItem(s.cookingFuel(UFuels.ticks(800)).rarity(Rarity.UNCOMMON).stacksTo(1).durability(120)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item MAGIC_STAFF = register("magic_staff", s -> new EnchantedStaffItem(s.rarity(Rarity.UNCOMMON).stacksTo(1).durability(120)), CreativeModeTabs.TOOLS_AND_UTILITIES);

    Item IRON_HORSE_SHOE = register("iron_horse_shoe", s -> new HorseShoeItem(s.durability(200), 4, 0.6F, 1), CreativeModeTabs.COMBAT);
    Item GOLDEN_HORSE_SHOE = register("golden_horse_shoe", s -> new HorseShoeItem(s.durability(100), 5, 0.1F, 0.5F), CreativeModeTabs.COMBAT);
    Item COPPER_HORSE_SHOE = register("copper_horse_shoe", s -> new HorseShoeItem(s.durability(250), 6, 0.5F, 0.8F), CreativeModeTabs.COMBAT);
    Item NETHERITE_HORSE_SHOE = register("netherite_horse_shoe", s -> new HorseShoeItem(s.durability(800), 3, 0.7F, 1.2F), CreativeModeTabs.COMBAT);

    Item WOODEN_POLEARM = register("wooden_polearm", s -> new PolearmItem(ToolMaterial.WOOD, 2, -3.2F, 2, s.cookingFuel(UFuels.ticks(200))), CreativeModeTabs.COMBAT);
    Item STONE_POLEARM = register("stone_polearm", s -> new PolearmItem(ToolMaterial.STONE, 2, -3.2F, 2, s), CreativeModeTabs.COMBAT);
    Item IRON_POLEARM = register("iron_polearm", s -> new PolearmItem(ToolMaterial.IRON, 2, -3.1F, 3, s), CreativeModeTabs.COMBAT);
    Item GOLDEN_POLEARM = register("golden_polearm", s -> new PolearmItem(ToolMaterial.GOLD, 3, -3F, 4, s), CreativeModeTabs.COMBAT);
    Item DIAMOND_POLEARM = register("diamond_polearm", s -> new PolearmItem(ToolMaterial.DIAMOND, 3, -3F, 5, s), CreativeModeTabs.COMBAT);
    Item NETHERITE_POLEARM = register("netherite_polearm", s -> new PolearmItem(ToolMaterial.NETHERITE, 3, -3F, 6, s.fireResistant()), CreativeModeTabs.COMBAT);

    Item LOOT_BUG_SPAWN_EGG = register("loot_bug_spawn_egg", s -> new SpawnEggItem(s.spawnEgg(UEntities.LOOT_BUG)), CreativeModeTabs.SPAWN_EGGS);
    Item BUTTERFLY_SPAWN_EGG = register("butterfly_spawn_egg", s -> new SpawnEggItem(s.spawnEgg(UEntities.BUTTERFLY)), CreativeModeTabs.SPAWN_EGGS);
    Item BUTTERFLY = register("butterfly", s -> new ButterflyItem(s.cookingFuel(UFuels.ticks(2)).compostable(UCompostables.VERY_LOW).food(UFoodComponents.INSECTS)), CreativeModeTabs.FOOD_AND_DRINKS);

    Item PALM_BOAT = register("palm_boat", s -> new BoatItem(UEntities.PALM_BOAT, s.stacksTo(1)), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Item PALM_CHEST_BOAT = register("palm_chest_boat", s -> new BoatItem(UEntities.PALM_CHEST_BOAT, s.stacksTo(1)), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Item PALM_SIGN = register("palm_sign", s -> new StandingAndWallBlockItem(UBlocks.PALM_SIGN, UBlocks.PALM_WALL_SIGN, net.minecraft.core.Direction.UP, s), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Item PALM_HANGING_SIGN = register("palm_hanging_sign", s -> new HangingSignItem(UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_WALL_HANGING_SIGN, s), CreativeModeTabs.FUNCTIONAL_BLOCKS);

    Item SPELLBOOK = register("spellbook", s -> new SpellbookItem(s.cookingFuel(UFuels.ticks(9000)).stacksTo(1).rarity(Rarity.UNCOMMON)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item TORN_PAGE = register("torn_page", Item::new, CreativeModeTabs.INGREDIENTS);

    Item OAK_BASKET = register("oak_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.OAK), s.cookingFuel(UFuels.ticks(1700)).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item SPRUCE_BASKET = register("spruce_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.SPRUCE), s.cookingFuel(UFuels.ticks(1700)).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item BIRCH_BASKET = register("birch_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.BIRCH), s.cookingFuel(UFuels.ticks(1700)).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item JUNGLE_BASKET = register("jungle_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.JUNGLE), s.cookingFuel(UFuels.ticks(1700)).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item ACACIA_BASKET = register("acacia_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.ACACIA), s.cookingFuel(UFuels.ticks(1700)).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item CHERRY_BASKET = register("cherry_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.CHERRY), s.cookingFuel(UFuels.ticks(1700)).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item DARK_OAK_BASKET = register("dark_oak_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.DARK_OAK), s.cookingFuel(UFuels.ticks(1700)).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item PALE_OAK_BASKET = register("pale_oak_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.PALE_OAK), s.cookingFuel(UFuels.ticks(1700)).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item MANGROVE_BASKET = register("mangrove_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.MANGROVE), s.cookingFuel(UFuels.ticks(1700)).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item BAMBOO_BASKET = register("bamboo_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.BAMBOO), s.cookingFuel(UFuels.ticks(1700)).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item PALM_BASKET = register("palm_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(UWoodTypes.PALM), s.cookingFuel(UFuels.ticks(1700)).stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);

    Item GIANT_BALLOON = register("giant_balloon", s -> new GiantBalloonItem(s.stacksTo(1).component(UDataComponentTypes.BALLOON_DESIGN, BalloonDesignComponent.DEFAULT)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item SPECTRAL_CLOCK = register("spectral_clock", Item::new, CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item TOTEM_OF_DYING = register("totem_of_dying", s -> new Item(s.stacksTo(1).rarity(Rarity.UNCOMMON).component(UDataComponentTypes.DEATH_CAUSING, UDataComponentTypes.TOTEM_OF_DYING)), CreativeModeTabs.COMBAT);

    Item WHITE_BED_SHEETS = register(CloudBedBlock.SheetPattern.WHITE);
    Item LIGHT_GRAY_BED_SHEETS = register(CloudBedBlock.SheetPattern.LIGHT_GRAY);
    Item GRAY_BED_SHEETS = register(CloudBedBlock.SheetPattern.GRAY);
    Item BLACK_BED_SHEETS = register(CloudBedBlock.SheetPattern.BLACK);
    Item BROWN_BED_SHEETS = register(CloudBedBlock.SheetPattern.BROWN);
    Item RED_BED_SHEETS = register(CloudBedBlock.SheetPattern.RED);
    Item ORANGE_BED_SHEETS = register(CloudBedBlock.SheetPattern.ORANGE);
    Item YELLOW_BED_SHEETS = register(CloudBedBlock.SheetPattern.YELLOW);
    Item LIME_BED_SHEETS = register(CloudBedBlock.SheetPattern.LIME);
    Item GREEN_BED_SHEETS = register(CloudBedBlock.SheetPattern.GREEN);
    Item CYAN_BED_SHEETS = register(CloudBedBlock.SheetPattern.CYAN);
    Item LIGHT_BLUE_BED_SHEETS = register(CloudBedBlock.SheetPattern.LIGHT_BLUE);
    Item BLUE_BED_SHEETS = register(CloudBedBlock.SheetPattern.BLUE);
    Item PURPLE_BED_SHEETS = register(CloudBedBlock.SheetPattern.PURPLE);
    Item MAGENTA_BED_SHEETS = register(CloudBedBlock.SheetPattern.MAGENTA);
    Item PINK_BED_SHEETS = register(CloudBedBlock.SheetPattern.PINK);

    Item APPLE_BED_SHEETS = register(CloudBedBlock.SheetPattern.APPLE);
    Item BARRED_BED_SHEETS = register("barred_bed_sheets", s -> new BedsheetsItem(CloudBedBlock.SheetPattern.BARS, s.stacksTo(8)), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Item CHECKERED_BED_SHEETS = register("checkered_bed_sheets", s -> new BedsheetsItem(CloudBedBlock.SheetPattern.CHECKER, s.stacksTo(8)), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Item KELP_BED_SHEETS = register(CloudBedBlock.SheetPattern.KELP);
    Item RAINBOW_BED_SHEETS = register(CloudBedBlock.SheetPattern.RAINBOW);
    Item RAINBOW_BPW_BED_SHEETS = register(CloudBedBlock.SheetPattern.RAINBOW_BPW);
    Item RAINBOW_BPY_BED_SHEETS = register(CloudBedBlock.SheetPattern.RAINBOW_BPY);
    Item RAINBOW_PBG_BED_SHEETS = register(CloudBedBlock.SheetPattern.RAINBOW_PBG);
    Item RAINBOW_PWR_BED_SHEETS = register(CloudBedBlock.SheetPattern.RAINBOW_PWR);

    Item PEGASUS_AMULET = register("pegasus_amulet", s -> new PegasusAmuletItem(s
            .stacksTo(1)
            .durability(890)
            .rarity(Rarity.UNCOMMON), 900), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item ALICORN_AMULET = register("alicorn_amulet", s -> new AlicornAmuletItem(s
            .stacksTo(1)
            .durability(1000)
            .rarity(Rarity.RARE)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item BROKEN_ALICORN_AMULET = register("broken_alicorn_amulet", Item::new, CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item UNICORN_AMULET = register("unicorn_amulet", s -> new AmuletItem(s
            .stacksTo(1)
            .durability(890)
            .rarity(Rarity.UNCOMMON), 0), CreativeModeTabs.TOOLS_AND_UTILITIES);
    Item PEARL_NECKLACE = register("pearl_necklace", s -> new AmuletItem(s
            .stacksTo(1)
            .durability(16)
            .rarity(Rarity.UNCOMMON), 0), CreativeModeTabs.TOOLS_AND_UTILITIES);

    Item SUNGLASSES = register("sunglasses", s -> new GlassesItem(s
            .stacksTo(1)
            .component(UDataComponentTypes.ITEM_AFTER_BREAKING, new BreaksIntoItemComponent(
                    UTags.DamageTypes.BREAKS_SUNGLASSES,
                    ResourceKey.create(Registries.ITEM, Unicopia.id("broken_sunglasses")),
                    USounds.ITEM_SUNGLASSES_SHATTER.unwrapKey().get())
            )
            ), CreativeModeTabs.COMBAT);
    Item BROKEN_SUNGLASSES = register("broken_sunglasses", s -> new GlassesItem(s.stacksTo(1)), CreativeModeTabs.COMBAT);

    Item CLAM_SHELL = register("clam_shell", Item::new, CreativeModeTabs.INGREDIENTS);
    Item SCALLOP_SHELL = register("scallop_shell", Item::new, CreativeModeTabs.INGREDIENTS);
    Item TURRET_SHELL = register("turret_shell", Item::new, CreativeModeTabs.INGREDIENTS);
    Item SHELLY = register("shelly", Item::new, CreativeModeTabs.INGREDIENTS);

    Item ROTTEN_COD = register("rotten_cod", s -> new Item(s.food(Foods.ROTTEN_FLESH)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item ROTTEN_SALMON = register("rotten_salmon", s -> new Item(s.food(Foods.ROTTEN_FLESH)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item ROTTEN_TROPICAL_FISH = register("rotten_tropical_fish", s -> new Item(s.food(Foods.ROTTEN_FLESH)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item ROTTEN_PUFFERFISH = register("rotten_pufferfish", s -> new Item(s.food(UFoodComponents.ROTTEN_PUFFERFISH, UConsumableComponents.poisonedFish(1, 1, 1))), CreativeModeTabs.FOOD_AND_DRINKS); //ItemTags.WOLF_FOOD

    Item COOKED_TROPICAL_FISH = register("cooked_tropical_fish", s -> new Item(s.food(Foods.COOKED_COD)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item COOKED_PUFFERFISH = register("cooked_pufferfish", s -> new Item(s.food(UFoodComponents.COOKED_PUFFERFISH, UConsumableComponents.poisonedFish(1, 0.3F, 0.4F))), CreativeModeTabs.FOOD_AND_DRINKS);
    Item FRIED_AXOLOTL = register("fried_axolotl", s -> new ConsumableItem(s.food(Foods.COOKED_CHICKEN).stacksTo(1).craftRemainder(Items.BUCKET), ItemUseAnimation.EAT), CreativeModeTabs.FOOD_AND_DRINKS);
    Item GREEN_FRIED_EGG = register("green_fried_egg", s -> new Item(s.food(UFoodComponents.FRIED_EGG)), CreativeModeTabs.FOOD_AND_DRINKS);

    Item FROG_LEGS = register("frog_legs", s -> new Item(s.food(Foods.CHICKEN)), CreativeModeTabs.FOOD_AND_DRINKS);
    Item COOKED_FROG_LEGS = register("cooked_frog_legs", s -> new Item(s.food(Foods.COOKED_CHICKEN)), CreativeModeTabs.FOOD_AND_DRINKS);

    Item GOLDEN_STICK = register("golden_stick", s -> new Item(s.craftRemainder(Items.GOLD_NUGGET)), CreativeModeTabs.NATURAL_BLOCKS);

    Item CARAPACE = register("carapace", Item::new, CreativeModeTabs.INGREDIENTS);
    Item CLOTH_BED = register("cloth_bed", s -> new FancyBedItem(UBlocks.CLOTH_BED, s.stacksTo(1)), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Item CLOUD_BED = register("cloud_bed", s -> new CloudBedItem(UBlocks.CLOUD_BED, s.stacksTo(1)), CreativeModeTabs.FUNCTIONAL_BLOCKS);
    Item CLOUD_LUMP = register("cloud_lump", Item::new, CreativeModeTabs.NATURAL_BLOCKS);

    Item ALICORN_BADGE = register(Race.ALICORN);
    Item PEGASUS_BADGE = register(Race.PEGASUS);
    Item UNICORN_BADGE = register(Race.UNICORN);
    Item EARTH_BADGE = register(Race.EARTH);
    Item BAT_BADGE = register(Race.BAT);
    Item CHANGELING_BADGE = register(Race.CHANGELING);
    Item KIRIN_BADGE = register(Race.KIRIN);
    Item HIPPOGRIFF_BADGE = register(Race.HIPPOGRIFF);

    static void bootstrap() {
        AppleItem.registerTickCallback(Items.APPLE);


        UEnchantments.bootstrap();
        URecipes.bootstrap();
        UItemGroups.bootstrap();
    }
}
