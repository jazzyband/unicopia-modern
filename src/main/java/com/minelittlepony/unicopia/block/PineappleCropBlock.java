package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.compat.seasons.FertilizableUtil;
import com.minelittlepony.unicopia.item.UItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;

public class PineappleCropBlock extends CropBlock {
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final BooleanProperty WILD = BooleanProperty.create("wild");

    public PineappleCropBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(HALF, Half.BOTTOM).setValue(WILD, false));
    }


    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        if (state.getValue(HALF) == Half.TOP) {
            return UItems.PINEAPPLE.getDefaultInstance();
        }
        return UItems.PINEAPPLE_CROWN.getDefaultInstance();
    }

    @Override
    public boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        return floor.is(this) || super.mayPlaceOn(floor, world, pos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (state.getValue(WILD) && world.getBlockState(pos.below()).is(BlockTags.DIRT)) {
            return world.getRawBrightness(pos, 0) >= 8 || world.canSeeSky(pos);
        }
        return super.canSurvive(state, world, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.UP && !neighborState.is(this)) {
            return state.setValue(AGE, Math.min(state.getValue(AGE), getMaxAge() - 1));
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) == Half.BOTTOM) {
            super.randomTick(state, world, pos, random);

            BlockState newState = world.getBlockState(pos);

            if (newState.is(this) && !isMaxAge(state) && isMaxAge(newState) && world.isEmptyBlock(pos.above())) {
                world.setBlockAndUpdate(pos.above(), defaultBlockState().setValue(HALF, Half.TOP));
            }
        } else {
            int age = getAge(state);
            if (world.getRawBrightness(pos, 0) >= 9 && age < getMaxAge()) {
                int steps = FertilizableUtil.getGrowthSteps(world, pos, state, random);
                if (steps > 0) {
                    world.setBlock(pos, state.setValue(AGE, Math.min(getMaxAge(), age + steps)), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    @Override
    public void growCrops(Level world, BlockPos pos, BlockState state) {
        world.setBlock(pos, state.setValue(AGE, Math.min(getMaxAge(), getAge(state) + getBonemealAgeIncrease(world))), Block.UPDATE_CLIENTS);

        Half half = state.getValue(HALF);
        if (half == Half.BOTTOM && isMaxAge(world.getBlockState(pos))) {
            if (world.isEmptyBlock(pos.above())) {
                world.setBlockAndUpdate(pos.above(), defaultBlockState().setValue(HALF, Half.TOP));
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF, WILD);
    }
}
