package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.mob.UEntities;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class UEntityTypeTagProvider extends FabricTagsProvider<EntityType<?>> {
    public UEntityTypeTagProvider(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
        super(output, Registries.ENTITY_TYPE, registriesFuture);
    }

    @Override
    protected RegistryEntryTagAppender<EntityType<?>> tag(TagKey<EntityType<?>> tag) {
        return new RegistryEntryTagAppender<>(super.tag(tag), entity -> BuiltInRegistries.ENTITY_TYPE.getResourceKey(entity).orElseThrow(),
                id -> ResourceKey.create(Registries.ENTITY_TYPE, id));
    }

    @Override
    protected void addTags(Provider lookup) {
        // TODO: Separate these into classes
        tag(UTags.Entities.TRANSFORMABLE).add(
                EntityTypes.SKELETON, EntityTypes.WITHER_SKELETON,
                EntityTypes.ZOMBIE, EntityTypes.DROWNED, EntityTypes.HUSK, EntityTypes.ZOMBIE_VILLAGER,
                EntityTypes.CREEPER,
                EntityTypes.VILLAGER, EntityTypes.WANDERING_TRADER, EntityTypes.PILLAGER, EntityTypes.ILLUSIONER, EntityTypes.EVOKER, EntityTypes.WITCH,
                EntityTypes.TURTLE,
                EntityTypes.BLAZE, EntityTypes.BREEZE,
                EntityTypes.SHEEP, EntityTypes.PIG, EntityTypes.GOAT,
                EntityTypes.RABBIT, EntityTypes.POLAR_BEAR, EntityTypes.PANDA,
                EntityTypes.COW, EntityTypes.MOOSHROOM,
                EntityTypes.HORSE, EntityTypes.LLAMA, EntityTypes.DONKEY, EntityTypes.MULE,
                EntityTypes.CHICKEN, EntityTypes.PARROT,
                EntityTypes.SPIDER, EntityTypes.CAVE_SPIDER, EntityTypes.BEE, UEntities.BUTTERFLY,
                EntityTypes.MAGMA_CUBE, EntityTypes.SLIME,
                EntityTypes.ENDERMITE, UEntities.LOOT_BUG,
                EntityTypes.SQUID, EntityTypes.GLOW_SQUID,
                EntityTypes.OCELOT, EntityTypes.CAT,
                EntityTypes.WOLF, EntityTypes.FOX,
                EntityTypes.SALMON, EntityTypes.COD, EntityTypes.PUFFERFISH,
                EntityTypes.FROG
        );
        tag(EntityTypeTags.CAN_BREATHE_UNDER_WATER).add(UEntities.MIMIC);
        tag(EntityTypeTags.IMPACT_PROJECTILES).add(UEntities.MUFFIN);
        tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(UEntities.LEVITATING_ITEM);
    }
}
