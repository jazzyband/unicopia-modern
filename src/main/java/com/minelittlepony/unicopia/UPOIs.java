package com.minelittlepony.unicopia;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.mixin.MixinPointOfInterestType;
import com.minelittlepony.unicopia.mixin.PointOfInterestTypesAccessor;

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface UPOIs {
    Set<ResourceKey<PoiType>> CHEST_POINTS_OF_INTEREST = new HashSet<>();
    ResourceKey<PoiType> CHESTS = register(Unicopia.id("chests"), 1, 64, () -> {
        return BuiltInRegistries.BLOCK.entrySet().stream()
                .map(entry -> entry.getValue())
                .filter(b -> b instanceof AbstractChestBlock)
                .flatMap(block -> {
                    List<BlockState> states = block.getStateDefinition().getPossibleStates();
                    List<ResourceKey<PoiType>> existingTypes = states.stream()
                            .flatMap(state -> PoiTypes.forState(state).stream())
                            .flatMap(entry -> entry.unwrapKey().stream())
                            .toList();

                    if (!existingTypes.isEmpty()) {
                        CHEST_POINTS_OF_INTEREST.addAll(existingTypes);
                        return Stream.empty();
                    }
                    return states.stream();
                })
                .distinct();
    });

    static ResourceKey<PoiType> register(Identifier id, int ticketCount, int searchDistance, Supplier<Stream<BlockState>> states) {
        PoiType type = Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, id, new PoiType(new HashSet<>(), ticketCount, searchDistance));
        ((MixinPointOfInterestType)(Object)type).setStates(new HashSet<>());
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            if (type.matchingStates().isEmpty()) {
                type.matchingStates().addAll(states.get().collect(Collectors.toSet()));
                BuiltInRegistries.POINT_OF_INTEREST_TYPE.get(CHESTS.identifier()).ifPresent(entry -> {
                    PointOfInterestTypesAccessor.registerStates(entry, type.matchingStates());
                });
            }
        });
        return ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, id);
    }

    static boolean isChest(Holder<PoiType> type) {
        return type.unwrapKey().filter(CHEST_POINTS_OF_INTEREST::contains).isPresent();
    }

    static void bootstrap() {
        CHEST_POINTS_OF_INTEREST.add(CHESTS);
        BuiltInRegistries.POINT_OF_INTEREST_TYPE.entrySet().forEach(poi -> {
            if (poi.getValue().matchingStates().stream().anyMatch(state -> state.getBlock() instanceof ChestBlock)) {
                CHEST_POINTS_OF_INTEREST.add(poi.getKey());
            }
        });
        RegistryEntryAddedCallback.event(BuiltInRegistries.POINT_OF_INTEREST_TYPE).register((raw, key, value) -> {
            if (value.matchingStates().stream().anyMatch(state -> state.getBlock() instanceof ChestBlock)) {
                CHEST_POINTS_OF_INTEREST.add(ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, key));
            }
        });
    }
}
