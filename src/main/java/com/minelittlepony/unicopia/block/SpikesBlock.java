package com.minelittlepony.unicopia.block;

import org.joml.Vector3f;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;

public class SpikesBlock extends OrientedBlock {

    public SpikesBlock(Properties settings) {
        super(settings);
    }


    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean isMoving) {
        if (!(entity instanceof LivingEntity) || entity.getType() == EntityTypes.FOX || entity.getType() == EntityTypes.BEE) {
            return;
        }

        if (world instanceof ServerLevel sw) {
            Vec3 vel = entity.getDeltaMovement().add(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
            Vector3f normVel = state.getValue(FACING).step().mul(vel.toVector3f());

            if ((normVel.x + normVel.y + normVel.z) < -0.08F) {
                float damage = (float)vel.lengthSqr() * 26;
                entity.hurtServer(sw, world.damageSources().source(UDamageTypes.SPIKES), damage);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        if (world instanceof ServerLevel sw && !oldState.is(this)) {
            for (Entity e : world.getEntities(null, new AABB(pos))) {
                if (!(e instanceof LivingEntity) || e.getType() == EntityTypes.FOX || e.getType() == EntityTypes.BEE) {
                    continue;
                }
                e.hurtServer(sw, world.damageSources().source(UDamageTypes.SPIKES), 6);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        pos = pos.relative(facing.getOpposite());
        state = world.getBlockState(pos);
        return state.canBeReplaced() || state.isFaceSturdy(world, pos, facing, SupportType.FULL) || state.is(Blocks.HONEY_BLOCK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction side = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(ctx.getClickedFace().getOpposite())).canBeReplaced() ? Direction.UP : ctx.getClickedFace();
        return defaultBlockState().setValue(FACING, side);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == state.getValue(FACING).getOpposite() && !canSurvive(state, world, pos)) {
            if (!(neighborState.is(Blocks.STICKY_PISTON)
                    || neighborState.is(Blocks.PISTON)
                    || neighborState.is(Blocks.PISTON_HEAD)
                    || neighborState.is(Blocks.MOVING_PISTON))) {

                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
