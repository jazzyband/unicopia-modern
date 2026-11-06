package com.minelittlepony.unicopia.block;

import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.compat.seasons.FertilizableUtil;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.dedicated.Settings;

public class SegmentedCropBlock extends CropBlock implements SegmentedBlock {
    private static final Codec<Supplier<SegmentedCropBlock>> SEGMENT_CODEC = CodecUtils.supplierOf(BuiltInRegistries.BLOCK.byNameCodec().xmap(
            b -> (SegmentedCropBlock)b,
            b -> (Block)b
    ));

    static final float BASE_GROWTH_CHANCE = /*1 in */ 50F /* chance, half the speed of regular crops */;

    private final ItemLike seeds;

    @Nullable
    private final Supplier<SegmentedCropBlock> prevSegmentSupplier;
    @Nullable
    private Supplier<SegmentedCropBlock> nextSegmentSupplier;

    private final int progressionAge;

    public static SegmentedCropBlock create(final int maxAge, int progressionAge,
            ItemLike seeds,
            @Nullable Supplier<SegmentedCropBlock> prevSegmentSupplier,
            @Nullable Supplier<SegmentedCropBlock> nextSegmentSupplier, BlockBehaviour.Properties settings) {

        final IntegerProperty age = IntegerProperty.create("age", 0, maxAge);
        return new SegmentedCropBlock(progressionAge, settings.mapColor(state -> state.getValue(age) >= maxAge && prevSegmentSupplier == null ? MapColor.COLOR_YELLOW : MapColor.PLANT), seeds, prevSegmentSupplier, nextSegmentSupplier) {
            @Override
            public IntegerProperty getAgeProperty() {
                return age;
            }

            @Override
            public int getMaxAge() {
                return maxAge;
            }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                builder.add(age);
            }
        };
    }

    protected SegmentedCropBlock(int progressionAge, BlockBehaviour.Properties settings,
            ItemLike seeds,
            @Nullable Supplier<SegmentedCropBlock> prevSegmentSupplier,
            @Nullable Supplier<SegmentedCropBlock> nextSegmentSupplier) {
       super(settings);
       this.seeds = seeds;
       this.prevSegmentSupplier = prevSegmentSupplier;
       this.nextSegmentSupplier = nextSegmentSupplier;
       this.progressionAge = progressionAge;
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return super.getAgeProperty();
    }

    public Function<Properties, SegmentedCropBlock> createNext(int progressionAge) {
        return s -> {
            SegmentedCropBlock next = create(getMaxAge() - this.progressionAge, progressionAge, seeds, () -> this, null, s);
            nextSegmentSupplier = () -> next;
            return next;
        };
    }


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        BlockPos tip = getTip(world, pos);
        BlockPos root = getRoot(world, pos);

        int height = (tip.getY() - root.getY());

        BlockState tipState = world.getBlockState(tip);
        double tipHeight = tipState.getBlock() instanceof SegmentedCropBlock tipBlock ? SegmentedBlock.getHeight(tipBlock.getAge(tipState)) : 0;

        double offset = (root.getY() - pos.getY()) * 16;

        return Block.box(0, offset, 0, 16, height * 16 + tipHeight + offset, 16);
    }

    @Override
    public ItemLike getBaseSeedId() {
        return seeds;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter view, BlockPos pos) {
        return (state.getBlock() instanceof SegmentedCropBlock o && o.canSupportBlock(this, state, view, pos)) || super.mayPlaceOn(state, view, pos);
    }

    protected boolean canSupportBlock(Block other, BlockState state, BlockGetter view, BlockPos pos) {
        return (nextSegmentSupplier != null && nextSegmentSupplier.get() == other);
    }

    @Override
    public void growCrops(Level world, BlockPos pos, BlockState state) {
        super.growCrops(world, pos, state);
        propagateGrowth(world, pos, state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.UP && !isNext(neighborState)) {
            return state.setValue(getAgeProperty(), Math.min(state.getValue(getAgeProperty()), getMaxAge() - 1));
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockPos tip = getTip(world, pos);
        BlockPos root = getRoot(world, pos);

        if (root.getY() != pos.getY()) {
            return;
        }

        if (world.getRawBrightness(tip, 0) >= 9) {
            int age = getAge(state);
            if (age < getMaxAge()) {
                float moisture = ((CropBlockAccessor)this).unicopia_getAvailableMoisture(world.getBlockState(root), world, root);
                int steps = FertilizableUtil.getGrowthSteps(world, pos, state, random);
                while (steps-- > 0) {
                    if (random.nextInt((int)(BASE_GROWTH_CHANCE / moisture) + 1) == 0) {
                        world.setBlock(pos, getStateForAge(age + 1), Block.UPDATE_CLIENTS);
                        propagateGrowth(world, pos, state);
                    }
                }
            }
        }
    }

    private void propagateGrowth(Level world, BlockPos pos, BlockState state) {
        int oldAge = getAge(state);
        state = world.getBlockState(pos);
        int ageChange = Math.max(1, getAge(state) - oldAge);

        onGrown(world, pos, state, ageChange);

        BlockPos root = getRoot(world, pos);
        BlockPos tip = getTip(world, pos);
        for (BlockPos p : BlockPos.betweenClosed(root, tip)) {
            if (p.getY() == pos.getY()) {
                continue;
            }

            BlockState s = world.getBlockState(p);
            if (s.getBlock() instanceof SegmentedCropBlock segment) {
                int segAge = Math.min(segment.getAge(s) + ageChange, segment.getMaxAge());
                world.setBlockAndUpdate(p, s.setValue(segment.getAgeProperty(), segAge));
                segment.onGrown(world, p, s, ageChange);
            }
        }
    }

    private void onGrown(Level world, BlockPos pos, BlockState state, int ageChange) {
        if (nextSegmentSupplier != null && getAge(state) >= progressionAge && world.isEmptyBlock(pos.above())) {
            SegmentedCropBlock nxt = nextSegmentSupplier.get();

            world.setBlock(pos.above(), nxt.getStateForAge(Math.min(ageChange, nxt.getMaxAge())), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state, BonemealSource source) {
        if (super.isValidBonemealTarget(world, pos, state, source)) {
            return true;
        }

        if (nextSegmentSupplier == null) {
            return false;
        }

        pos = pos.above();
        state = world.getBlockState(pos);
        return state.isAir() || (isNext(state) && state.getBlock() instanceof BonemealableBlock f && f.isValidBonemealTarget(world, pos, state, source));
    }

    @Override
    protected int getBonemealAgeIncrease(Level world) {
        return super.getBonemealAgeIncrease(world) / 2;
    }

    @Override
    public boolean isBase(BlockState state) {
        return state.getBlock() == this || (prevSegmentSupplier != null && prevSegmentSupplier.get().isBase(state));
    }

    @Override
    public boolean isNext(BlockState state) {
        return state.getBlock() == this || (nextSegmentSupplier != null && nextSegmentSupplier.get().isNext(state));
    }

    @Nullable
    public SegmentedCropBlock getNext() {
        return nextSegmentSupplier == null ? null : nextSegmentSupplier.get();
    }
}
