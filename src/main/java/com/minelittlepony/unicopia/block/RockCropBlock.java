package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.item.UItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RockCropBlock extends CropBlock {
    private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[] {
            Shapes.or(
                    box(7, -1, 11, 8, 0, 12),
                    box(3.5F, -1, 3.5F, 5, 0, 5),
                    box(11.5F, -1, 5.5F, 13, 0, 7)
            ),
            Shapes.or(
                    box(6, -1, 10, 9, 1, 13),
                    box(2.5F, -1, 2.5F, 6.5F, 2, 6.5F),
                    box(10.5F, -1, 4.5F, 14.5F, 2, 8.5F)
            ),
            Shapes.or(
                    box(4, -1, 9, 10, 2, 14),
                    box(1.5F, -1, 1.5F, 7.5F, 3, 7.5F),
                    box(9, -1, 3.5F, 15.5F, 3, 9.5F)
            ),
            Shapes.or(
                    box(4, -1, 9, 10, 2, 14),
                    box(1.5F, -1, 1.5F, 7.5F, 4, 7.5F),
                    box(9, -1, 3.5F, 15.5F, 3, 9.5F)
            ),
            Shapes.or(
                    box(3, -1, 8, 11, 3, 15),
                    box(0.5F, -1, 0.5F, 8.5F, 5, 8.5F),
                    box(7.5F, -1, 2, 17, 5, 10.5F)
            ),
            Shapes.or(
                    box(3, -1, 8, 11, 4, 15),
                    box(0.5F, -1, 0.5F, 8.5F, 5, 8.5F),
                    box(7.5F, -1, 2, 17, 7, 10.5F)
            ),
            Shapes.or(
                    box(3, -1, 8, 11, 4, 15),
                    box(0.5F, -1, 0.5F, 8.5F, 7, 8.5F),
                    box(7.5F, -1, 2, 17, 9, 10.5F)
            ),
            Shapes.or(
                    box(3, -1, 8, 11, 4, 15),
                    box(0.5F, -1, 0.5F, 8.5F, 9, 8.5F),
                    box(7.5F, -1, 2, 17, 13, 10.5F)
            )
    };

    protected RockCropBlock(Properties settings) {
        super(settings);
    }


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return AGE_TO_SHAPE[state.getValue(getAgeProperty())];
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (isBonemealSuccess(world, random, pos, state, BonemealSource.MOB)) {
            super.randomTick(state, world, pos, random);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
        if (!moved && !(state.getBlock() == this && world.getBlockState(pos).is(this))) {
            world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
        }
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return world.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 20, EquinePredicates.PLAYER_CAN_USE_EARTH) != null;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state, BonemealSource source) {
        if (world instanceof Level level && !isBonemealSuccess(level, level.getRandom(), pos, state, source)) {
            return false;
        }
        return super.isValidBonemealTarget(world, pos, state, source);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return UItems.PEBBLES;
    }

    @Override
    @Nullable
    public final BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!EquineContext.of(context).getCompositeRace().any(Race::canUseEarth)) {
            return null;
        }

        return super.getStateForPlacement(context);
    }
}
