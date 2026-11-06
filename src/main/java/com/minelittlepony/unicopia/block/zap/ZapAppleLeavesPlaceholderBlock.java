package com.minelittlepony.unicopia.block.zap;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore.Stage;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ZapAppleLeavesPlaceholderBlock extends AirBlock implements ZapStagedBlock {
    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;


    public ZapAppleLeavesPlaceholderBlock(Properties settings) {
        super(settings);
        registerDefaultState(stateDefinition.any().setValue(DISTANCE, BlockStateProperties.STABILITY_MAX_DISTANCE));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return updateDistanceFromLogs(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos());
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return shouldDecay(state);
    }

    @Override
    public Stage getStage(BlockState state) {
        return ZapAppleStageStore.Stage.HIBERNATING;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        updateStage(state, world, pos);
    }

    protected boolean shouldDecay(BlockState state) {
        return state.getValue(DISTANCE) == 7;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (shouldDecay(state)) {
            dropResources(state, world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Deprecated
    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(state, world, pos, random);
        world.setBlock(pos, updateDistanceFromLogs(state, world, pos), Block.UPDATE_ALL);
        tryAdvanceStage(state, world, pos, random);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        int i = getDistanceFromLog(neighborState) + 1;
        if (i != 1 || state.getValue(DISTANCE) != i) {
            tickView.scheduleTick(pos, this, 1);
        }

        return state;
    }

    private static BlockState updateDistanceFromLogs(BlockState state, LevelAccessor world, BlockPos pos) {
        int i = BlockStateProperties.STABILITY_MAX_DISTANCE;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.values()) {
            mutable.setWithOffset(pos, direction);
            i = Math.min(i, getDistanceFromLog(world.getBlockState(mutable)) + 1);
            if (i == 1) {
                break;
            }
        }

        return state.setValue(DISTANCE, i);
    }

    private static int getDistanceFromLog(BlockState state) {
        return LeavesBlock.getOptionalDistanceAt(state).orElse(7);
    }

}
