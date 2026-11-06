package com.minelittlepony.unicopia.block.cloud;

import java.util.Map;
import java.util.function.Function;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.mojang.serialization.MapCodec;

public class CloudPillarBlock extends CloudBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    private static final BooleanProperty TOP = BlockStateProperties.NORTH;
    private static final BooleanProperty BOTTOM = BlockStateProperties.SOUTH;
    private static final Map<Direction, BooleanProperty> DIRECTION_PROPERTIES = Map.of(
            Direction.UP, TOP, Direction.DOWN, BOTTOM,
            Direction.SOUTH, TOP, Direction.NORTH, BOTTOM,
            Direction.EAST, TOP, Direction.WEST, BOTTOM
    );
    private static final Function<Direction.Axis, VoxelShape[]> SHAPES = Util.memoize(axis -> {
        int[] offsets = { axis.choose(1, 0, 0), axis.choose(0, 1, 0), axis.choose(0, 0, 1) };
        float capOffset = 11F / 16F;
        VoxelShape core = Block.box(
            axis.choose(0, 1, 1), axis.choose(1, 0, 1), axis.choose(1, 1, 0),
            16 - axis.choose(0, 1, 1), 16 - axis.choose(1, 0, 1), 16 - axis.choose(1, 1, 0)
        );
        VoxelShape foot = Block.box(0, 0, 0, 16 - (11 * offsets[0]), 16 - (11 * offsets[1]), 16 - (11 * offsets[2]));
        VoxelShape cap = foot.move(capOffset * offsets[0], capOffset * offsets[1], capOffset * offsets[2]);
        return new VoxelShape[] {
                core,
                Shapes.or(core, foot),
                Shapes.or(core, cap),
                Shapes.or(core, cap, foot)
        };
    });

    public CloudPillarBlock(Properties settings) {
        super(false, settings);
        registerDefaultState(defaultBlockState().setValue(TOP, true).setValue(BOTTOM, true).setValue(AXIS, Direction.Axis.Y));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, TOP, BOTTOM);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, EquineContext equineContext) {
        return SHAPES.apply(state.getValue(AXIS))[(state.getValue(TOP) ? 0 : 2) + (state.getValue(BOTTOM) ? 0 : 1)];
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext placementContext, EquineContext equineContext) {
        BlockPos pos = placementContext.getClickedPos();
        Direction.Axis axis = placementContext.getClickedFace().getAxis();
        Direction upDirection = Direction.get(AxisDirection.POSITIVE, axis);
        Direction downDirection = Direction.get(AxisDirection.NEGATIVE, axis);
        BlockState above = placementContext.getLevel().getBlockState(pos.relative(upDirection));
        BlockState below = placementContext.getLevel().getBlockState(pos.relative(downDirection));
        return super.getPlacementState(placementContext, equineContext)
                .setValue(DIRECTION_PROPERTIES.get(upDirection), above.is(this) && above.getValue(AXIS) == axis)
                .setValue(DIRECTION_PROPERTIES.get(downDirection), below.is(this) && below.getValue(AXIS) == axis)
                .setValue(AXIS, axis);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction.getAxis() == state.getValue(AXIS)) {
            return state.setValue(DIRECTION_PROPERTIES.get(direction), neighborState.is(this) && neighborState.getValue(AXIS) == state.getValue(AXIS));
        }

        return state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return RotatedPillarBlock.rotatePillar(state, rotation);
    }
}
