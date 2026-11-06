package com.minelittlepony.unicopia.datagen;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import com.google.common.collect.ImmutableList;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;

final class CustomStructurePools {
    static void bootstrapPools(BootstrapContext<StructureTemplatePool> registerable) {
    }

    static void bootstrapProcessors(BootstrapContext<StructureProcessorList> registerable) {
        registerExtra(registerable, ProcessorLists.FARM_PLAINS, ImmutableList.of(new RuleProcessor(ImmutableList.of(
                new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.2F), AlwaysTrueTest.INSTANCE, UBlocks.OATS.defaultBlockState())
        ))));
        registerExtra(registerable, ProcessorLists.FARM_SAVANNA, ImmutableList.of(new RuleProcessor(ImmutableList.of(
                new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.1F), AlwaysTrueTest.INSTANCE, UBlocks.OATS.defaultBlockState())
        ))));
    }

    static void register(BootstrapContext<StructureTemplatePool> registerable, String name, StructureTemplatePool pool) {
        registerable.register(ResourceKey.create(Registries.TEMPLATE_POOL, Unicopia.id(name)), pool);
    }

    static void registerExtra(BootstrapContext<StructureTemplatePool> registerable, String name, StructureTemplatePool pool) {
        registerable.register(ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.fromNamespaceAndPath(Unicopia.VANILLA_EXTENSIONS_NAMESPACE, name)), pool);
    }

    static void registerExtra(BootstrapContext<StructureProcessorList> registerable, ResourceKey<StructureProcessorList> key, List<StructureProcessor> processors) {
        registerable.register(ResourceKey.create(Registries.PROCESSOR_LIST, Identifier.fromNamespaceAndPath(Unicopia.VANILLA_EXTENSIONS_NAMESPACE, key.identifier().getPath())), new StructureProcessorList(processors));
    }
}
