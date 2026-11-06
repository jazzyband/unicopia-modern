package com.minelittlepony.unicopia.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class StableDoorBlock extends DoorBlock {

    public StableDoorBlock(BlockSetType blockSet, Properties settings) {
        super(blockSet, settings);
    }


    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            if (neighborState.is(this) && neighborState.getValue(HALF) != half) {
                state = state
                        .setValue(FACING, neighborState.getValue(FACING))
                        .setValue(HINGE, neighborState.getValue(HINGE));
                if (half ==  DoubleBlockHalf.UPPER && direction == Direction.DOWN && !state.getValue(POWERED)) {
                    state = state.setValue(OPEN, neighborState.getValue(OPEN));
                }
                return state;
            }

            return Blocks.AIR.defaultBlockState();
        }

        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return state;
    }
}
