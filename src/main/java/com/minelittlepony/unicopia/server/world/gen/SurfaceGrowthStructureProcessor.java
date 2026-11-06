package com.minelittlepony.unicopia.server.world.gen;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import com.minelittlepony.unicopia.server.world.UWorldGen;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class SurfaceGrowthStructureProcessor implements StructureProcessor {
    public static final MapCodec<SurfaceGrowthStructureProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        RuleTest.CODEC.fieldOf("input_predicate").forGetter(rule -> rule.inputPredicate),
        BlockState.CODEC.fieldOf("output_state").forGetter(rule -> rule.outputState)
    ).apply(instance, SurfaceGrowthStructureProcessor::new));

    private final RuleTest inputPredicate;
    private final BlockState outputState;

    public SurfaceGrowthStructureProcessor(RuleTest inputPredicate, BlockState outputState) {
        this.inputPredicate = inputPredicate;
        this.outputState = outputState;
    }

    @Override
    public MapCodec<SurfaceGrowthStructureProcessor> codec() {
        return CODEC;
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
        ServerLevelAccessor world,
        BlockPos pos,
        BlockPos pivot,
        List<StructureTemplate.StructureBlockInfo> originalBlockInfos,
        List<StructureTemplate.StructureBlockInfo> currentBlockInfos,
        StructurePlaceSettings data
    ) {
        Map<BlockPos, StructureTemplate.StructureBlockInfo> positionalInfos = currentBlockInfos.stream().collect(Collectors.toMap(
                StructureTemplate.StructureBlockInfo::pos,
                Function.identity()
        ));

        return currentBlockInfos.stream().map(currentBlockInfo -> {
            StructureTemplate.StructureBlockInfo aboveBlockInfo = positionalInfos.get(currentBlockInfo.pos().above());
            BlockState currentState = aboveBlockInfo == null ? world.getBlockState(currentBlockInfo.pos().above()) : aboveBlockInfo.state();
            if ((currentState.isAir() || currentState.canBeReplaced())
                    && inputPredicate.test(currentBlockInfo.state(), currentBlockInfo.pos(), RandomSource.create(Mth.getSeed(currentBlockInfo.pos())))) {
                return new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), outputState, currentBlockInfo.nbt());
            }
            return currentBlockInfo;
        }).collect(Collectors.toList());
    }
}
