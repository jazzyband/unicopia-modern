package com.minelittlepony.unicopia.block.cloud;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CloudStairsBlock extends StairBlock implements CloudLike {

    private final CloudBlock baseBlock;

    public CloudStairsBlock(BlockState baseState, Properties settings) {
        super(baseState, CloudLike.applyCloudProperties(settings));
        this.baseBlock = (CloudBlock)baseState.getBlock();
    }


    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return baseBlock.getShadeBrightness(state, world, pos);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return baseBlock.propagatesSkylightDown(state);
    }

    @Override
    public void bounceOn(Level world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        baseBlock.bounceOn(world, state, pos, entity, fallDistance);
    }

    @Override
    public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        baseBlock.fallOn(world, state, pos, entity, fallDistance);
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean isMoving) {
        baseBlock.entityInside(state, world, pos, entity, handler, isMoving);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (!baseBlock.canInteract(state, world, pos, EquineContext.of(context))) {
            return Shapes.empty();
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    protected final VoxelShape getOcclusionShape(BlockState state) {
        return super.getShape(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return this.hasCollision ? state.getShape(world, pos, context) : Shapes.empty();
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        EquineContext equineContext = EquineContext.of(context);
        if (!baseBlock.canInteract(defaultBlockState(), context.getLevel(), context.getClickedPos(), equineContext)) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return baseBlock.canBeReplaced(state, context);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        return baseBlock.skipRendering(state, stateFrom, direction);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return baseBlock.isPathfindable(state, type);
    }
}
