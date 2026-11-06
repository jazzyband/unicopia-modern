package com.minelittlepony.unicopia.block.cloud;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CloudBlock extends Block implements CloudLike {

    protected final boolean meltable;

    public CloudBlock(boolean meltable, Properties settings) {
        super(CloudLike.applyCloudProperties(meltable ? settings.randomTicks() : settings));
        this.meltable = meltable;
    }


    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 0.9F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public void bounceOn(Level world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        boolean bounce = Math.abs(entity.getDeltaMovement().y) > 0.3;
        super.bounceOn(world, state, pos, entity, fallDistance);
        if (bounce) {
            entity.push(0, 0.2F, 0);
        }
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (world.getRandom().nextInt(150) == 0) {
            generateSurfaceParticles(world, state, pos, CollisionContext.empty(), 1);
        }
    }

    @Override
    public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage(fallDistance, 0, world.damageSources().fall());
        generateSurfaceParticles(world, state, pos, CollisionContext.empty(), 9);

        if (!world.isClientSide() && fallDistance > 7) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        if (world.getRandom().nextInt(15) == 0) {
            generateSurfaceParticles(world, state, pos, CollisionContext.empty(), 1);
        }
    }

    protected void generateSurfaceParticles(Level world, BlockState state, BlockPos pos, CollisionContext context, int count) {
        VoxelShape shape = getOutlineShape(state, world, pos, context, EquineContext.of(context));
        if (shape.isEmpty()) {
            return;
        }
        RandomSource rng = world.getRandom();
        AABB box = shape.bounds();

        for (int i = 0; i < count; i++) {
            world.addParticle(ParticleTypes.CLOUD,
                    pos.getX() + Mth.lerp(rng.nextFloat(), box.minX, box.maxX),
                    pos.getY() + box.maxY,
                    pos.getZ() + Mth.lerp(rng.nextFloat(), box.minZ, box.maxZ), 0, 0, 0);

            world.addParticle(ParticleTypes.CLOUD,
                    pos.getX() + (rng.nextBoolean() ? box.minX : box.maxX),
                    pos.getY() + Mth.lerp(rng.nextFloat(), box.minY, box.maxY),
                    pos.getZ() + Mth.lerp(rng.nextFloat(), box.minZ, box.maxZ), 0, 0, 0);

            world.addParticle(ParticleTypes.CLOUD,
                    pos.getX() + Mth.lerp(rng.nextFloat(), box.minX, box.maxX),
                    pos.getY() + Mth.lerp(rng.nextFloat(), box.minY, box.maxY),
                    pos.getZ() + (rng.nextBoolean() ? box.minZ : box.maxZ), 0, 0, 0);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean isMoving) {

        if (entity instanceof Player player && (player.getAbilities().flying || Pony.of(player).getPhysics().isFlying())) {
            return;
        }

        if (entity.getDeltaMovement().y < 0) {
            float cloudWalking = EquineContext.of(entity).getCloudWalkingStrength();
            if (cloudWalking > 0) {
                entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, 1 - cloudWalking, 1));
                entity.push(0, 0.07, 0);
                entity.setOnGround(true);
            }
        }

        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.9F, 1, 0.9F));
    }

    @Override
    protected final VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        EquineContext equineContext = EquineContext.of(context);
        if (!canInteract(state, world, pos, equineContext)) {
            return Shapes.empty();
        }
        return getOutlineShape(state, world, pos, context, equineContext);
    }

    @Override
    protected final VoxelShape getOcclusionShape(BlockState state) {
        return getOutlineShape(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty(), EquineContext.ABSENT);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return this.hasCollision ? state.getShape(world, pos, context) : Shapes.empty();
    }

    @Override
    @Nullable
    public final BlockState getStateForPlacement(BlockPlaceContext context) {
        EquineContext equineContext = EquineContext.of(context);
        if (!canInteract(defaultBlockState(), context.getLevel(), context.getClickedPos(), equineContext)) {
            return null;
        }
        return getPlacementState(context, equineContext);
    }

    @Override
    protected final boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        EquineContext equineContext = EquineContext.of(context);
        if (canInteract(state, context.getLevel(), context.getClickedPos(), equineContext)) {
            return canReplace(state, context, equineContext);
        }
        return true;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        VoxelShape shape = state.getOcclusionShape();
        VoxelShape shapeFrom = stateFrom.getOcclusionShape();
        return !shape.isEmpty() && !shapeFrom.isEmpty() && Shapes.blockOccludes(shape, shapeFrom, direction);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return type != PathComputationType.LAND || !InteractionManager.getInstance().getPathingEquineContext().collidesWithClouds();
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, EquineContext equineContext) {
        return Shapes.block();
    }

    protected boolean canInteract(BlockState state, BlockGetter world, BlockPos pos, EquineContext context) {
        return context.collidesWithClouds() || context.hasFeatherTouch();
    }

    protected boolean canReplace(BlockState state, BlockPlaceContext context, EquineContext equineContext) {
        return super.canBeReplaced(state, context);
    }

    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext placementContext, EquineContext equineContext) {
        return super.getStateForPlacement(placementContext);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (meltable) {
            if (world.getBrightness(LightLayer.BLOCK, pos) > 11) {
                dropResources(state, world, pos);
                world.removeBlock(pos, false);
            }
        }
    }
}
