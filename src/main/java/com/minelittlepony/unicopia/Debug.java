package com.minelittlepony.unicopia;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.particle.ParticleSource;

public interface Debug {
    boolean SPELLBOOK_CHAPTERS = Boolean.getBoolean("unicopia.debug.spellbookChapters");
    boolean CHECK_GAME_VALUES = Boolean.getBoolean("unicopia.debug.checkGameValues");
    boolean CHECK_TRAIT_COVERAGE = Boolean.getBoolean("unicopia.debug.checkTraitCoverage");

    AtomicReference<Level> LAST_TESTED_WORLD = new AtomicReference<>(null);

    static void runTests(Level world) {
        if (!CHECK_GAME_VALUES || !world.dimensionTypeRegistration().is(BuiltinDimensionTypes.OVERWORLD) || (LAST_TESTED_WORLD.getAndSet(world) == world)) {
            return;
        }

        if (CHECK_TRAIT_COVERAGE) {
            testTraitCoverage(BuiltInRegistries.ITEM, Unicopia.LOGGER, entry -> SpellTraits.of(entry.key()).isEmpty());
        }

        try {
            for (var type : WoodType.values().toList()) {
                var balloon = UEntities.AIR_BALLOON.create(world, EntitySpawnReason.SPAWN_ITEM_USE);
                balloon.setBasketType(AirBalloonEntity.BasketType.of(type));
                balloon.asItem();
            }
        } catch (Throwable t) {
            throw new IllegalStateException("Tests failed", t);
        }
    }

    static void drawBoxSelection(ParticleSource<?> pony, ParticleOptions particle, BlockPos pos) {
        if (CHECK_GAME_VALUES) {
            float amount = pony.asWorld().getRandom().nextFloat();

            pony.addParticle(particle, new Vec3(pos.getX() + amount, pos.getY(), pos.getZ()), Vec3.ZERO);
            pony.addParticle(particle, new Vec3(pos.getX(), pos.getY(), pos.getZ() + amount), Vec3.ZERO);

            pony.addParticle(particle, new Vec3(pos.getX() + 1, pos.getY(), pos.getZ() + amount), Vec3.ZERO);
            pony.addParticle(particle, new Vec3(pos.getX() + amount, pos.getY(), pos.getZ() + 1), Vec3.ZERO);
        }
    }

    static void testTraitCoverage(HolderLookup<Item> registry, Logger logger, Predicate<Holder.Reference<Item>> traitsCheck) {
        registry.listElements().collect(Collectors.toMap(
                entry -> entry.key().identifier().getNamespace(),
                Set::of,
                Sets::union
        )).forEach((namespace, entries) -> {
            List<String> unregistered = entries.stream()
                .filter(entry -> !entry.is(UTags.Items.HAS_NO_TRAITS) && !traitsCheck.test(entry))
                .map(entry -> {
                    String id = entry.value().toString();

                    return id + "(" + registry.listTags()
                        .filter(i -> i.contains(entry))
                        .map(i -> i.key().location().toString())
                        .collect(Collectors.joining(", ")) +  ")";
                })
                .toList();

            if (!unregistered.isEmpty()) {
                logger.warn("No traits registered for {} items in namepsace {} {}", unregistered.size(), namespace, String.join(",\r\n", unregistered));
            }
        });
    }
}
