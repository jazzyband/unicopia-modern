package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.minelittlepony.unicopia.block.EdibleBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.IndirectionUtils;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;

public class UExternalBlockLootTableProvider {
    private final BiConsumer<ResourceKey<LootTable>, LootTable.Builder> lootTables;
    private final ResourceCondition[] conditions;

    protected UExternalBlockLootTableProvider(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> lootTables, ResourceCondition...conditions) {
        this.lootTables = lootTables;
        this.conditions = conditions;
    }

    public void addDrop(Identifier block, Function<Identifier, LootTable.Builder> drop) {
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, block.withPrefix("blocks/"));
        LootTable.Builder table = drop.apply(block);
        FabricDataGenHelper.addConditions(table, conditions);
        lootTables.accept(key, table);
    }

    public static LootTable.Builder edibleBlockDrops(Identifier block, Identifier drop) {
        LootTable.Builder builder = LootTable.lootTable();
        for (BooleanProperty segment : EdibleBlock.SEGMENTS) {
            builder
                .withPool(LootPool.lootPool()
                    .setRolls(UBlockLootTableProvider.exactly(1))
                        .add(applyStateCondition(block, segment, true, DynamicLoot.dynamicEntry(drop)))
                        .when(ExplosionCondition.survivesExplosion()));
        }
        return builder;
    }

    public static <T extends ConditionUserBuilder<T>> T applyStateCondition(Identifier block,
            BooleanProperty property, boolean value, ConditionUserBuilder<T> builder) {
        Holder<Block> entry = IndirectionUtils.entryOf(Registries.BLOCK, block, UBlocks.HAY_BLOCK);
        return builder.when(() -> new MatchBlock(new BlockPredicate(
                java.util.Optional.of(HolderSet.direct(entry)),
                StatePropertiesPredicate.Builder.properties().hasProperty(property, value).build(),
                java.util.Optional.empty(), DataComponentMatchers.ANY))
        );
    }
}
