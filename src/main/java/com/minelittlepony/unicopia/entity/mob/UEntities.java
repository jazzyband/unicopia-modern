package com.minelittlepony.unicopia.entity.mob;

import java.util.function.Predicate;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.behaviour.EntityBehaviour;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.Heightmap;

public interface UEntities {
    EntityType<ButterflyEntity> BUTTERFLY = register("butterfly", FabricEntityType.Builder.createMob(ButterflyEntity::new, MobCategory.AMBIENT, builder -> builder
                .defaultAttributes(ButterflyEntity::createButterflyAttributes))
            .canSpawnFarFromPlayer()
            .sized(0.25F, 0.25F)
            .eyeHeight(0.125F));
    EntityType<MagicProjectileEntity> THROWN_ITEM = register("thrown_item", EntityType.Builder.<MagicProjectileEntity>of(MagicProjectileEntity::new, MobCategory.MISC)
            .clientTrackingRange(100)
            .noSummon()
            .updateInterval(2)
            .sized(0.25F, 0.25F));
    EntityType<LevitatingItemEntity> LEVITATING_ITEM = register("levitating_item", EntityType.Builder.<LevitatingItemEntity>of(LevitatingItemEntity::new, MobCategory.MISC)
            .clientTrackingRange(100)
            .noSummon()
            .updateInterval(2)
            .sized(0.25F, 0.25F));
    EntityType<PhysicsBodyProjectileEntity> MUFFIN = register("muffin", EntityType.Builder.<PhysicsBodyProjectileEntity>of((type, world) -> new PhysicsBodyProjectileEntity(type, world, UItems.MUFFIN.getDefaultInstance()), MobCategory.MISC)
            .clientTrackingRange(100)
            .noSummon()
            .updateInterval(2)
            .sized(0.25F, 0.25F));
    EntityType<MagicBeamEntity> MAGIC_BEAM = register("magic_beam", EntityType.Builder.<MagicBeamEntity>of(MagicBeamEntity::new, MobCategory.MISC)
            .clientTrackingRange(100)
            .noSummon()
            .updateInterval(2)
            .alwaysUpdateVelocity(true)
            .sized(0.25F, 0.25F));
    EntityType<FloatingArtefactEntity> FLOATING_ARTEFACT = register("floating_artefact", EntityType.Builder.of(FloatingArtefactEntity::new, MobCategory.MISC)
            .clientTrackingRange(200)
            .noSummon()
            .sized(1, 1));
    EntityType<CastSpellEntity> CAST_SPELL = register("cast_spell", EntityType.Builder.<CastSpellEntity>of(CastSpellEntity::new, MobCategory.MISC)
            .clientTrackingRange(200)
            .noSummon()
            .sized(4, 4));
    EntityType<FairyEntity> TWITTERMITE = register("twittermite", FabricEntityType.Builder.createMob(FairyEntity::new, MobCategory.MISC, builder -> builder
                .defaultAttributes(FairyEntity::createMobAttributes))
            .clientTrackingRange(200)
            .sized(0.1F, 0.1F));
    EntityType<FriendlyCreeperEntity> FRIENDLY_CREEPER = register("friendly_creeper", FabricEntityType.Builder.createMob(FriendlyCreeperEntity::new, MobCategory.MISC, builder -> builder
                .defaultAttributes(FriendlyCreeperEntity::createCreeperAttributes))
            .clientTrackingRange(8)
            .noSummon()
            .sized(0.6f, 1.7f));
    EntityType<SpellbookEntity> SPELLBOOK = register("spellbook", FabricEntityType.Builder.createMob(SpellbookEntity::new, MobCategory.MISC, builder -> builder
                .defaultAttributes(SpellbookEntity::createMobAttributes))
            .clientTrackingRange(200)
            .sized(0.9F, 0.5F));
    EntityType<SombraEntity> SOMBRA = register("sombra", FabricEntityType.Builder.<SombraEntity>createMob(SombraEntity::new, MobCategory.MONSTER, builder -> builder
                .defaultAttributes(SombraEntity::createMobAttributes))
            .clientTrackingRange(200)
            .sized(2F, 4F));
    EntityType<CrystalShardsEntity> CRYSTAL_SHARDS = register("crystal_shards", EntityType.Builder.of(CrystalShardsEntity::new, MobCategory.MISC)
            .clientTrackingRange(100)
            .sized(1F, 1F));
    EntityType<StormCloudEntity> STORM_CLOUD = register("storm_cloud", EntityType.Builder.of(StormCloudEntity::new, MobCategory.MISC)
            .clientTrackingRange(200)
            .sized(20F, 20F));
    EntityType<Boat> PALM_BOAT = register("oak_boat",
            EntityType.Builder.of(getBoatFactory(() -> UItems.PALM_BOAT), MobCategory.MISC)
                .noLootTable()
                .sized(1.375F, 0.5625F)
                .eyeHeight(0.5625F)
                .clientTrackingRange(10));
    EntityType<ChestBoat> PALM_CHEST_BOAT = register("palm_chest_boat",
        EntityType.Builder.of(getChestBoatFactory(() -> UItems.PALM_CHEST_BOAT), MobCategory.MISC)
            .noLootTable()
            .sized(1.375F, 0.5625F)
            .eyeHeight(0.5625F)
            .clientTrackingRange(10));
    EntityType<AirBalloonEntity> AIR_BALLOON = register("air_balloon", FabricEntityType.Builder.createMob(AirBalloonEntity::new, MobCategory.MISC, builder -> builder
                .defaultAttributes(net.minecraft.world.entity.Mob::createMobAttributes))
            .clientTrackingRange(1000)
            .sized(2.5F, 0.1F));
    EntityType<LootBugEntity> LOOT_BUG = register("loot_bug", FabricEntityType.Builder.createMob(LootBugEntity::new, MobCategory.MONSTER, builder -> builder
                .defaultAttributes(LootBugEntity::createAttributes))
            .clientTrackingRange(8)
            .sized(0.8F, 0.6F));
    EntityType<TentacleEntity> TENTACLE = register("ignominious_vine", EntityType.Builder.<TentacleEntity>of(TentacleEntity::new, MobCategory.MISC)
            .clientTrackingRange(8)
            .sized(0.8F, 0.8F));
    EntityType<IgnominiousBulbEntity> IGNOMINIOUS_BULB = register("ignominious_bulb", FabricEntityType.Builder.<IgnominiousBulbEntity>createMob(IgnominiousBulbEntity::new, MobCategory.MISC, builder -> builder
                .defaultAttributes(IgnominiousBulbEntity::createMobAttributes))
            .clientTrackingRange(8)
            .sized(3, 2));
    EntityType<SpecterEntity> SPECTER = register("specter", FabricEntityType.Builder.createMob(SpecterEntity::new, MobCategory.MONSTER, builder -> builder
                .defaultAttributes(SpecterEntity::createAttributes))
            .fireImmune()
            .canSpawnFarFromPlayer()
            .sized(1, 2));
    EntityType<MimicEntity> MIMIC = register("mimic", FabricEntityType.Builder.createMob(MimicEntity::new, MobCategory.MONSTER, builder -> builder
                .defaultAttributes(MimicEntity::createMobAttributes))
            .fireImmune()
            .noSummon()
            .sized(0.875F, 0.875F));

    private static EntityType.EntityFactory<Boat> getBoatFactory(Supplier<Item> itemSupplier) {
        return (type, world) -> new Boat(type, world, itemSupplier);
    }

    private static EntityType.EntityFactory<ChestBoat> getChestBoatFactory(Supplier<Item> itemSupplier) {
        return (type, world) -> new ChestBoat(type, world, itemSupplier);
    }

    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        var key = ResourceKey.create(Registries.ENTITY_TYPE, Unicopia.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    static void bootstrap() {
        net.minecraft.world.entity.SpawnPlacements.register(BUTTERFLY, SpawnPlacementTypes.NO_RESTRICTIONS, Types.MOTION_BLOCKING_NO_LEAVES, ButterflyEntity::canSpawn);
        net.minecraft.world.entity.SpawnPlacements.register(SPECTER, SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        if (!Unicopia.getConfig().disableButterflySpawning.get()) {
            final Predicate<BiomeSelectionContext> butterflySpawnable = BiomeSelectors.foundInOverworld()
                    .and(ctx -> ctx.getBiome().hasPrecipitation() && ctx.getBiome().getBaseTemperature() > 0.15F);

            BiomeModifications.addSpawn(butterflySpawnable.and(
                        BiomeSelectors.tag(BiomeTags.IS_RIVER)
                    .or(BiomeSelectors.tag(BiomeTags.IS_FOREST))
                    .or(BiomeSelectors.tag(BiomeTags.IS_HILL))
            ), MobCategory.AMBIENT, BUTTERFLY, 3, 3, 12);
            BiomeModifications.addSpawn(butterflySpawnable.and(
                        BiomeSelectors.tag(BiomeTags.IS_JUNGLE)
                    .or(BiomeSelectors.tag(BiomeTags.IS_MOUNTAIN))
            ), MobCategory.AMBIENT, BUTTERFLY, 7, 5, 19);
        }

        BiomeModifications.addSpawn(BiomeSelectors.spawnsOneOf(EntityTypes.ZOMBIE), MobCategory.MONSTER, SPECTER, 2, 1, 2);

        MimicEntity.bootstrap();
        UTradeOffers.bootstrap();
        EntityBehaviour.bootstrap();
        UEntityAttributes.bootstrap();
    }
}
