package com.minelittlepony.unicopia.block;

import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public interface BlockConstructionUtils {
    static Function<BlockBehaviour.Properties, ButtonBlock> woodenButton(BlockSetType setType) {
        return s -> new ButtonBlock(setType, 30, s.noCollision().strength(0.5f).pushReaction(PushReaction.POPPED));
    }

    static boolean never(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
        return false;
    }

    static boolean never(BlockState state, BlockGetter world, BlockPos pos, net.minecraft.world.phys.AABB bounds) {
        return false;
    }

    static boolean never(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    static Function<BlockBehaviour.Properties, StrippablePillarBlock> createMetallicLogBlock(MapColor topMapColor, MapColor sideMapColor) {
        return s -> new StrippablePillarBlock(s
                .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor)
                .instrument(NoteBlockInstrument.BELL)
                .strength(3.0f)
                .sound(SoundType.METAL));
    }

    static Function<BlockBehaviour.Properties, RotatedPillarBlock> createLogBlock(MapColor topMapColor, MapColor sideMapColor) {
        return s -> new RotatedPillarBlock(s
                .mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .ignitedByLava());
    }

    static Function<BlockBehaviour.Properties, RotatedPillarBlock> createWoodBlock(MapColor mapColor) {
        return s -> new RotatedPillarBlock(s
                .mapColor(mapColor)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .ignitedByLava());
    }

    static Function<BlockBehaviour.Properties, StrippablePillarBlock> createMetallicWoodBlock(MapColor mapColor) {
        return s -> new StrippablePillarBlock(s
                .mapColor(mapColor)
                .instrument(NoteBlockInstrument.BELL)
                .strength(3.0f)
                .sound(SoundType.METAL));
    }

    static Function<BlockBehaviour.Properties, LeavesBlock> createLeavesBlock(SoundType soundGroup) {
        return createLeavesBlock(TintedParticleLeavesBlock::new, soundGroup);
    }

    static <T extends LeavesBlock> Function<BlockBehaviour.Properties, T> createLeavesBlock(BiFunction<Float, BlockBehaviour.Properties, T> factory, SoundType soundGroup) {
        return s -> factory.apply(0.01F, s
                .mapColor(MapColor.PLANT)
                .strength(0.2f)
                .randomTicks()
                .sound(soundGroup)
                .noOcclusion()
                .isValidSpawn(BlockConstructionUtils::canSpawnOnLeaves)
                .isSuffocating(BlockConstructionUtils::never)
                .isViewBlocking(BlockConstructionUtils::never)
                .ignitedByLava()
                .pushReaction(PushReaction.POPPED)
                .isRedstoneConductor(BlockConstructionUtils::never));
    }

    static Boolean canSpawnOnLeaves(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
        return type == EntityTypes.OCELOT || type == EntityTypes.PARROT;
    }
}
