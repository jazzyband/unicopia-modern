package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PileBlock extends Block implements SimpleWaterloggedBlock {
    public static final int MAX_COUNT = 3;
    public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, MAX_COUNT);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape[] MYSTERIOUS_EGG_SHAPES = new VoxelShape[] {
            Block.box(3, 0, 3, 13, 13, 13),
            Shapes.or(
                    Block.box(1, 0, 4, 9, 13, 12),
                    Block.box(8, 0, 8, 14, 8, 14)
            ),
            Shapes.or(
                    Block.box(0, 0, -1, 8, 11, 7),
                    Block.box(4, 0, 6, 14, 15, 16),
                    Block.box(10, 0, 0, 16, 8, 6)
            )
    };

    private final VoxelShape[] shapes;

    public PileBlock(Properties settings, VoxelShape[] shapes) {
        super(settings.offsetType(OffsetType.XZ).dynamicShape());
        registerDefaultState(defaultBlockState().setValue(COUNT, 1));
        this.shapes = shapes;
    }


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(pos);
        return shapes[state.getValue(COUNT) - 1].move(offset.x, offset.y, offset.z);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, COUNT);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        BlockState state = ctx.getLevel().getBlockState(pos);
        if (state.is(this)) {
            return state.setValue(COUNT, Math.min(MAX_COUNT, state.getValue(COUNT) + 1));
        }

        return super.getStateForPlacement(ctx).setValue(WATERLOGGED, ctx.getLevel().getFluidState(pos).is(FluidTags.WATER));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        pos = pos.below();
        return world.getBlockState(pos).isFaceSturdy(world, pos, Direction.UP, SupportType.CENTER);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return (!context.isSecondaryUseActive() && context.getItemInHand().is(asItem()) && state.getValue(COUNT) < MAX_COUNT) || super.canBeReplaced(state, context);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.getValue(WATERLOGGED).booleanValue()) {
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }
}
