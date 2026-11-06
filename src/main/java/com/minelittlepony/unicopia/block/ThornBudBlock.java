package com.minelittlepony.unicopia.block;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.EarthPonyGrowAbility;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ThornBudBlock extends Block implements EarthPonyGrowAbility.Growable, BonemealableBlock {
    static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    static final int MAX_DISTANCE = 25;
    static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 0, MAX_DISTANCE);

    private final BlockState branchState;

    public ThornBudBlock(BlockState branchState, Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN).setValue(DISTANCE, 0));
        this.branchState = branchState;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DISTANCE);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (random.nextInt(50) == 0) {
            grow(world, state, pos);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == state.getValue(FACING) && !(neighborState.is(this) || neighborState.is(branchState.getBlock()))) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public boolean grow(Level world, BlockState state, BlockPos pos) {
        if (state.getValue(DISTANCE) >= MAX_DISTANCE) {
            return false;
        }
        return pickGrowthDirection(world, state, pos).map(randomDirection -> {
            BlockPos p = pos.relative(randomDirection);

            if (!canReplace(world.getBlockState(p))) {
                return false;
            }

            world.playSound(null, pos, USounds.Vanilla.BONE_MEAL_USE, SoundSource.BLOCKS);
            world.setBlockAndUpdate(pos, branchState
                    .setValue(FACING, state.getValue(FACING))
                    .setValue(ThornBlock.PROPERTY_BY_DIRECTION.get(state.getValue(FACING)), true)
                    .setValue(ThornBlock.PROPERTY_BY_DIRECTION.get(randomDirection), true));
            world.setBlockAndUpdate(p, defaultBlockState()
                    .setValue(FACING, randomDirection.getOpposite())
                    .setValue(DISTANCE, state.getValue(DISTANCE) + 1)
            );
            return true;
        }).orElse(false);
    }

    protected boolean canReplace(BlockState state) {
        return state.canBeReplaced();
    }

    private static Optional<Direction> pickGrowthDirection(Level world, BlockState state, BlockPos pos) {
        Direction excluded = state.getValue(FACING);
        return Util.getRandomSafe(ThornBlock.PROPERTY_BY_DIRECTION.keySet().stream()
                .filter(direction -> direction != excluded)
                .flatMap(direction -> getByWeight(direction, excluded))
                .toList(), world.getRandom());
    }

    private static Stream<Direction> getByWeight(Direction input, Direction excluded) {
        return Stream.generate(() -> input)
                .limit(input.getAxis() == excluded.getAxis() ? 6L : input.getAxis() == Direction.Axis.Y ? 1L : 3L);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        grow(world, state, pos);
    }
}
