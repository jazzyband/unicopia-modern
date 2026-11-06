package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CloudSlabBlock extends WaterloggableCloudBlock {
    private static final VoxelShape BOTTOM_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private static final VoxelShape TOP_SHAPE = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);

    public CloudSlabBlock(boolean meltable, @Nullable Supplier<Soakable> soggyBlock, Properties settings) {
        super(meltable, soggyBlock, settings);
        registerDefaultState(defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
    }


    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SlabBlock.TYPE);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, EquineContext equineContext) {
        return switch (state.getValue(SlabBlock.TYPE)) {
            case DOUBLE -> Shapes.block();
            case TOP -> TOP_SHAPE;
            case BOTTOM -> BOTTOM_SHAPE;
        };
    }

    @Override
    @Nullable
    public BlockState getPlacementState(BlockPlaceContext ctx, EquineContext equineContext) {
        BlockPos blockPos = ctx.getClickedPos();
        BlockState blockState = ctx.getLevel().getBlockState(blockPos);
        if (blockState.is(this)) {
            return blockState.setValue(SlabBlock.TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, false);
        }
        BlockState state = super.getPlacementState(ctx, equineContext).setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        Direction direction = ctx.getClickedFace();
        if (direction == Direction.DOWN || direction != Direction.UP && ctx.getClickLocation().y - blockPos.getY() > 0.5) {
            return state.setValue(SlabBlock.TYPE, SlabType.TOP);
        }
        return state;
    }

    @Override
    protected boolean canReplace(BlockState state, BlockPlaceContext context, EquineContext equineContext) {
        SlabType slabType = state.getValue(SlabBlock.TYPE);
        if (slabType == SlabType.DOUBLE || !context.getItemInHand().is(asItem())) {
            return false;
        }

        if (!context.replacingClickedOnBlock()) {
            return true;
        }

        boolean hitTop = context.getClickLocation().y - context.getClickedPos().getY() > 0.5;
        Direction side = context.getClickedFace();
        if (slabType == SlabType.BOTTOM) {
            return side == Direction.UP || hitTop && side.getAxis().isHorizontal();
        }
        return side == Direction.DOWN || !hitTop && side.getAxis().isHorizontal();
    }

    @Override
    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        return state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE && super.placeLiquid(world, pos, state, fluidState);
    }

    @Override
    public boolean canPlaceLiquid(LivingEntity filler, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        return state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE && super.canPlaceLiquid(filler, world, pos, state, fluid);
    }
}
