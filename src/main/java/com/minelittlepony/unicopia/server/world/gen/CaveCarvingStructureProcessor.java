package com.minelittlepony.unicopia.server.world.gen;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.server.world.UWorldGen;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class CaveCarvingStructureProcessor implements StructureProcessor {
    public static final MapCodec<CaveCarvingStructureProcessor> CODEC = MapCodec.unit(new CaveCarvingStructureProcessor());

    @Override
    public MapCodec<CaveCarvingStructureProcessor> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader world,
        BlockPos pos,
        BlockPos pivot,
        BlockPos relativePos,
        StructureTemplate.StructureBlockInfo currentBlockInfo,
        StructurePlaceSettings data
    ) {
        int topY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, currentBlockInfo.pos().getX(), currentBlockInfo.pos().getZ());
        return currentBlockInfo.pos().getY() > topY && world.isEmptyBlock(currentBlockInfo.pos()) ? null : currentBlockInfo;
    }
}
