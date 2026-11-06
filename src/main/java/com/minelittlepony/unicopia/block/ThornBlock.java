package com.minelittlepony.unicopia.block;

import java.util.Collection;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.ability.EarthPonyGrowAbility;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ThornBlock extends PipeBlock implements EarthPonyGrowAbility.Growable, BonemealableBlock {
    static final Collection<BooleanProperty> PROPERTIES = PROPERTY_BY_DIRECTION.values();
    static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    static final int MAX_DISTANCE = 25;
    static final int MAX_AGE = BlockStateProperties.MAX_AGE_4;
    static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 0, MAX_DISTANCE);
    static final IntegerProperty AGE = BlockStateProperties.AGE_4;

    private final Supplier<Block> bud;

    public ThornBlock(Supplier<Block> bud, Properties settings) {
        super(0.125F, settings);
        this.bud = bud;
        PROPERTIES.forEach(property -> registerDefaultState(defaultBlockState().setValue(property, false)));
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.DOWN)
                .setValue(DISTANCE, 0)
                .setValue(AGE, 0)
                .setValue(DOWN, true)
        );
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROPERTIES.toArray(Property[]::new));
        builder.add(FACING, DISTANCE, AGE);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) == MAX_AGE
                && random.nextInt(1200) == 0
                && world.hasNearbyAlivePlayer(pos.getX(), pos.getY(), pos.getZ(), 3)) {
            UEntities.LOOT_BUG.spawn(world, pos, EntitySpawnReason.NATURAL);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(world, pos)) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) == MAX_AGE && world.hasNearbyAlivePlayer(pos.getX(), pos.getY(), pos.getZ(), 3)) {
            Vec3 particlePos = Vec3.atCenterOf(pos).add(VecHelper.triangular(random, 0, 0.5));
            world.addAlwaysVisibleParticle(ParticleTypes.ASH, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == state.getValue(FACING) && !state.canSurvive(world, pos)) {
            tickView.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockState neighborState = world.getBlockState(pos.relative(facing));
        return (facing == Direction.DOWN && state.getValue(DISTANCE) == 0 && neighborState.is(BlockTags.DIRT))
            || neighborState.is(this)
            || neighborState.is(bud.get());
    }

    @Override
    public boolean grow(Level world, BlockState state, BlockPos pos) {
        if (state.getValue(DISTANCE) >= MAX_DISTANCE) {
            return false;
        }

        world.setBlockAndUpdate(pos, state.setValue(AGE, Math.min(state.getValue(AGE) + 1, MAX_AGE)));
        return PROPERTY_BY_DIRECTION.keySet().stream()
                .filter(direction -> isConnected(state, world.getBlockState(pos.relative(direction)), direction))
                .map(direction -> {
            BlockPos p = pos.relative(direction);
            BlockState s = world.getBlockState(p);
            if (s.isAir()) {
                // sprout a new branch for cut off nodes
                world.setBlockAndUpdate(p, bud.get().defaultBlockState()
                        .setValue(FACING, direction.getOpposite())
                        .setValue(DISTANCE, state.getValue(DISTANCE) + 1)
                );
                return true;
            }
            return ((EarthPonyGrowAbility.Growable)s.getBlock()).grow(world, s, p);
        }).reduce(false, Boolean::logicalOr);
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

    private boolean isConnected(BlockState state, BlockState neighborState, Direction direction) {
        if (!state.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
            return false;
        }
        if (neighborState.isAir()) {
            return true;
        }
        return neighborState.getBlock() instanceof EarthPonyGrowAbility.Growable
            && (neighborState.is(this) || neighborState.is(bud.get()))
            && neighborState.getValue(FACING) == direction.getOpposite();
    }
}
