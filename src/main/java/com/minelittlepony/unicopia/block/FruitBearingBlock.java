package com.minelittlepony.unicopia.block;

import java.util.*;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.EarthPonyKickAbility.Buckable;
import com.minelittlepony.unicopia.compat.seasons.FertilizableUtil;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;

public class FruitBearingBlock extends TintedParticleLeavesBlock implements TintedBlock, Buckable, BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
    public static final int MAX_AGE = 25;
    public static final EnumProperty<Stage> STAGE = EnumProperty.create("stage", Stage.class);

    public static final List<FruitBearingBlock> REGISTRY = new ArrayList<>();

    protected final Supplier<Block> fruit;
    protected final Supplier<ItemStack> rottenFruitSupplier;

    protected final int overlay;

    public FruitBearingBlock(int overlay, Supplier<Block> fruit, Supplier<ItemStack> rottenFruitSupplier, Properties settings) {
        super(0.01F, settings
                .randomTicks()
                .noOcclusion()
                .isValidSpawn(BlockConstructionUtils::canSpawnOnLeaves)
                .isSuffocating(BlockConstructionUtils::never)
                .isViewBlocking(BlockConstructionUtils::never));
        registerDefaultState(defaultBlockState().setValue(AGE, 0).setValue(STAGE, Stage.IDLE));
        this.overlay = overlay;
        this.fruit = fruit;
        this.rottenFruitSupplier = rottenFruitSupplier;
        FlammableBlockRegistry.getDefaultInstance().add(this, 30, 60);
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STAGE).add(AGE);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    protected boolean shouldAdvance(RandomSource random) {
        return true;
    }

    public BlockState getPlacedFruitState(RandomSource random) {
        return fruit.get().defaultBlockState();
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.randomTick(state, world, pos, random);

        if (decaying(state) || state.getValue(PERSISTENT)) {
            return;
        }

        if (world.getRawBrightness(pos, 0) <= 8) {
            return;
        }

        if (!PosHelper.fastAny(pos, (p, f) -> !world.getBlockState(p).isFaceSturdy(world, p, f.getOpposite()), PosHelper.ALL)) {
            return;
        }

        int steps = FertilizableUtil.getGrowthSteps(world, pos, state, random);
        BlockPos fruitPosition = pos.below();
        while (steps-- > 0) {
            if (!shouldAdvance(random)) {
                return;
            }

            state = cycleStage(state);
            BlockState fruitState = world.getBlockState(fruitPosition);

            switch (state.getValue(STAGE)) {
                case WITHERING:
                    wither(state, world, pos, fruitPosition, fruitState);
                case BEARING:
                    if (!fruitState.is(fruit.get())) {
                        state = withStage(state, Stage.IDLE);
                    }
                    break;
                case FRUITING: {
                    state = isPositionValidForFruit(state, pos)
                            ? grow(state, world, pos, fruitPosition, fruitState, random)
                            : withStage(state, Stage.IDLE);
                    break;
                }
                default:
            }
        }

        if (!state.equals(world.getBlockState(pos))) {
            world.setBlock(pos, state, Block.UPDATE_ALL, 0);
        }
    }

    protected BlockState withStage(BlockState state, Stage stage) {
        return state.setValue(AGE, 0).setValue(STAGE, stage);
    }

    private BlockState cycleStage(BlockState state) {
        state = state.cycle(AGE);
        if (state.getValue(AGE) == 0) {
            state = state.cycle(STAGE);
        }
        return state;
    }

    protected BlockState grow(BlockState state, Level world, BlockPos pos, BlockPos fruitPosition, BlockState fruitState, RandomSource random) {
        if (world.isEmptyBlock(fruitPosition)) {
            world.setBlock(fruitPosition, getPlacedFruitState(random), Block.UPDATE_ALL);
            return withStage(state, Stage.BEARING);
        }

        if (!fruitState.is(fruit.get())) {
            return withStage(state, Stage.IDLE);
        }
        return state;
    }

    protected void wither(BlockState state, Level world, BlockPos pos, BlockPos fruitPosition, BlockState fruitState) {
        if (!fruitState.is(fruit.get())) {
            if (world.getRandom().nextInt(2) == 0) {
                Block.popResource(world, fruitPosition, rottenFruitSupplier.get());
            } else {
                Block.dropResources(fruitState, world, fruitPosition, fruitState.hasBlockEntity() ? world.getBlockEntity(fruitPosition) : null, null, ItemStack.EMPTY);
            }

            if (world.removeBlock(fruitPosition, false)) {
                world.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(fruitState));
            }

            SoundType group = getSoundType(state);
            world.playSound(null, pos, USounds.ITEM_APPLE_ROT, SoundSource.BLOCKS, group.getVolume(), group.getPitch());
        }
    }

    @Override
    public List<ItemStack> onBucked(ServerLevel world, BlockState state, BlockPos pos) {
        world.setBlockAndUpdate(pos, state.setValue(STAGE, Stage.IDLE).setValue(AGE, 0));

        pos = pos.below();
        state = world.getBlockState(pos);
        if (state.is(fruit.get()) && state.getBlock() instanceof Buckable buckable) {
            return buckable.onBucked(world, state, pos);
        }
        return List.of();
    }

    @Override
    public int getTint(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int foliageColor) {
        return TintedBlock.blend(foliageColor, overlay);
    }

    public boolean isPositionValidForFruit(BlockState state, BlockPos pos) {
        return state.getSeed(pos) % 3 == 1;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state, BonemealSource source) {
        return switch (state.getValue(STAGE)) {
            case FLOWERING -> world.isEmptyBlock(pos.below());
            default -> !world.getBlockState(pos.below()).is(fruit.get());
        };
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return isValidBonemealTarget(world, pos, state, source);
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        state = state.cycle(AGE);
        if (state.getValue(AGE) == 0) {
            state = state.setValue(STAGE, switch (state.getValue(STAGE)) {
                case IDLE -> Stage.FLOWERING;
                case FLOWERING -> Stage.FRUITING;
                default -> Stage.FLOWERING;
            });
        }
        if (state.getValue(STAGE) == Stage.FRUITING && state.getValue(AGE) == 0) {
            state = grow(state, world, pos, pos.below(), world.getBlockState(pos.below()), random);
        }
        world.setBlockAndUpdate(pos, state);
    }

    public enum Stage implements StringRepresentable {
        IDLE,
        FLOWERING,
        FRUITING,
        BEARING,
        WITHERING;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
