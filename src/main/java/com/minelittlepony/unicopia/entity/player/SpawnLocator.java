package com.minelittlepony.unicopia.entity.player;

import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.block.cloud.CloudLike;
import com.minelittlepony.unicopia.util.MeteorlogicalUtil;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

public class SpawnLocator {
    // Modified from SpawnLocating.findOverworldSpawn
    private static BlockPos findOverworldSpawn(ServerLevel world, int x, int z, boolean avoidAir, boolean allowClouds) {
        boolean hasCeiling = world.dimensionType().hasCeiling();
        LevelChunk chunk = world.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        int startHeight = hasCeiling
                ? world.getChunkSource().getGenerator().getSpawnHeight(world)
                : chunk.getHeight(allowClouds ? Heightmap.Types.MOTION_BLOCKING : Heightmap.Types.WORLD_SURFACE, x & 0xF, z & 0xF);
        if (startHeight < world.getMinY()) {
            return null;
        }

        int terrainHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 0xF, z & 0xF);
        if (terrainHeight <= startHeight && terrainHeight > chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x & 0xF, z & 0xF)) {
            return null;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = startHeight + 1; y >= world.getMinY(); --y) {
            mutable.set(x, y, z);
            BlockState state = world.getBlockState(mutable);
            var pass = checkAtmosphere(state.getFluidState(), avoidAir);
            if (pass == TriState.TRUE) {
                continue;
            } else if (pass == TriState.FALSE) {
                return null;
            }

            if (Block.isFaceFull(state.getCollisionShape(world, mutable), Direction.UP) && (allowClouds || !(state.getBlock() instanceof CloudLike))) {
                return mutable.above().immutable();
            }
        }
        return null;
    }

    private static BlockPos findAdjustedOverworldSpawn(ServerLevel world, Player entity, AABB box, BlockPos basePos,
            int x, int z,
            int spawnRadius, boolean avoidAir, boolean avoidSun, boolean allowClouds) {
        BlockPos spawnPos = SpawnLocator.findOverworldSpawn(world, x, z, avoidAir, allowClouds);

        if (spawnPos == null) {
            return null;
        }

        if (avoidSun && MeteorlogicalUtil.isPositionExposedToSun(world, spawnPos)) {
            spawnPos = findUndergroundSpaceBelow(world, entity, basePos, spawnRadius, box, spawnPos);

            if (MeteorlogicalUtil.isPositionExposedToSun(world, spawnPos)) {
                return null;
            }
        }

        if (!checkAtmosphere(world, spawnPos, avoidAir)) {
            return null;
        }

        return spawnPos;
    }

    public static BlockPos findAdjustedOverworldSpawn(ServerLevel world, Player entity, AABB box, BlockPos basePos, int x, int z, Operation<BlockPos> operation) {
        boolean avoidSun = Pony.of(entity).getCompositeRace().includes(Race.BAT);
        boolean avoidAir = Pony.of(entity).getCompositeRace().includes(Race.SEAPONY);
        boolean allowClouds = Pony.of(entity).collidesWithClouds();
        if (!(avoidSun || avoidAir) && allowClouds) {
            return operation.call(world, x, z);
        }
        int spawnRadius = Math.max(16, world.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.RESPAWN_RADIUS));
        BlockPos adjusted = findAdjustedOverworldSpawn(world, entity, box, basePos, x, z, spawnRadius, avoidAir, avoidSun, allowClouds);
        // This now adjusts the final vanilla spawn, rather than a nullable search candidate.
        // Race preferences must never turn a valid vanilla respawn into a null position.
        return adjusted != null ? adjusted : operation.call(world, x, z);
    }

    private static TriState checkAtmosphere(FluidState state, boolean avoidAir) {
        if (avoidAir) {
            if (state.isEmpty()) {
                return TriState.TRUE;
            }
            if (!state.is(FluidTags.WATER)) {
                return TriState.FALSE;
            }
        }

        return state.isEmpty() ? TriState.DEFAULT : TriState.FALSE;
    }

    private static boolean checkAtmosphere(ServerLevel world, BlockPos pos, boolean avoidAir) {
        if (avoidAir) {
            return world.getFluidState(pos).is(FluidTags.WATER);
        }
        return world.getFluidState(pos).isEmpty();
    }

    private static BlockPos findUndergroundSpaceBelow(ServerLevel world, Player entity, BlockPos basePos, int spawnRadius, AABB box, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        mutable.move(0, -1, 0);
        // move to ground
        while (!world.noCollision(entity, box.move(Vec3.atBottomCenterOf(mutable)))
                && mutable.getY() >= basePos.getY() - spawnRadius * 2
                && mutable.getY() > world.getMinY() + 1) {
            mutable.move(Direction.DOWN);
        }
        // move down until we find a place we can stand
        while (world.noCollision(entity, box.move(Vec3.atBottomCenterOf(mutable.below())))
                && mutable.getY() >= basePos.getY() - spawnRadius * 2
                && mutable.getY() > world.getMinY() + 1) {
            mutable.move(Direction.DOWN);
        }
        return mutable.immutable();
    }
}
