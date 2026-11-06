package com.minelittlepony.unicopia.block.cloud;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CloudDoorBlock extends DoorBlock implements CloudLike {
    private final BlockState baseState;
    private final CloudBlock baseBlock;

    public CloudDoorBlock(BlockState baseState, BlockSetType blockSet, Properties settings) {
        super(blockSet, CloudLike.applyCloudProperties(settings));
        this.baseState = baseState;
        this.baseBlock = (CloudBlock)baseState.getBlock();
    }


    @Override
    protected final VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (canPassThrough(state, world, pos, EquineContext.of(context))) {
            return Shapes.empty();
        }
        return super.getShape(state, world, pos, context);
    }

    protected boolean canPassThrough(BlockState state, BlockGetter world, BlockPos pos, EquineContext context) {
        return context.getCompositeRace().any(Race::canUseEarth);
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
    public final BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!baseBlock.canInteract(baseState, context.getLevel(), context.getClickedPos(), EquineContext.of(context))) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!baseBlock.canInteract(baseState, world, pos, EquineContext.of(player))) {
            return InteractionResult.PASS;
        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean isMoving) {
        baseState.entityInside(world, pos, entity, handler, isMoving);

        EquineContext context = EquineContext.of(entity);

        if (!baseBlock.canInteract(baseState, world, pos, context)) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5F, 1, 0.5F));
        }
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return !InteractionManager.getInstance().getPathingEquineContext().collidesWithClouds() || super.isPathfindable(state, type);
    }
}
