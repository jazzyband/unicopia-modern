package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.BedsheetsItem;
import com.minelittlepony.unicopia.util.VoxelShapeUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class FancyBedBlock extends BedBlock implements net.minecraft.world.level.block.EntityBlock {
    private static final List<Function<Direction, VoxelShape>> SHAPES = List.of(
        VoxelShapeUtil.rotator(Shapes.or(
                box(0, 3, 1, 16, 9, 16),
                box(-0.5, 0, 1, 1.5, 13, 4),
                box(14.5, 0, 1, 16.5, 13, 4),
                box(1.5, 1, 0, 14.5, 16, 3)
        )),
        VoxelShapeUtil.rotator(Shapes.or(
                box(0, 3, 0, 16, 9, 16),
                box(-0.5, 0, -1, 2.5, 10, 2),
                box(13.5, 0, -1, 16.5, 10, 2),
                box(1.5, 1, -2, 14.5, 12, 1)
        ))
    );

    protected final String base;

    public FancyBedBlock(String base, Properties settings) {
        super(DyeColor.WHITE, settings);
        this.base = base;
    }


    public String getBase() {
        return base;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(PART).ordinal()).apply(BedBlock.getConnectedDirection(state));
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (state.hasBlockEntity() && !state.is(world.getBlockState(pos).getBlock()) && state.getValue(PART) == BedPart.HEAD) {
            world.getBlockEntity(pos, UBlockEntities.FANCY_BED).ifPresent(tile -> {
                SheetPattern pattern = tile.getPattern();
                if (pattern != SheetPattern.NONE) {
                    popResource(world, pos, BedsheetsItem.forPattern(pattern).getDefaultInstance());
                }
            });
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        // MC-269785
        BedPart part = state.getValue(PART);
        BlockPos otherHalfPos = pos.relative(getNeighbourDirection(part, state.getValue(FACING)));
        BlockState otherHalfState = world.getBlockState(otherHalfPos);
        if (/*!world.isClient &&*/ player.isCreative() && part == BedPart.FOOT && otherHalfState.is(this) && otherHalfState.getValue(PART) == BedPart.HEAD) {
            if (!world.isClientSide()) {
                world.setBlock(otherHalfPos, otherHalfState.getFluidState().createLegacyBlock(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
            spawnDestroyParticles(world, otherHalfPos, otherHalfState);
            if (state.is(BlockTags.GUARDED_BY_PIGLINS) && !world.isClientSide()) {
                PiglinAi.angerNearbyPiglins((ServerLevel)world, player, false);
            }
            world.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
        } else {
            spawnDestroyParticles(world, pos, state);
            if (state.is(BlockTags.GUARDED_BY_PIGLINS) && !world.isClientSide()) {
                PiglinAi.angerNearbyPiglins((ServerLevel)world, player, false);
            }
            world.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
        }
        return state;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Tile(pos, state);
    }

    public static void setBedPattern(Level world, BlockPos pos, SheetPattern pattern) {
        world.getBlockEntity(pos, UBlockEntities.FANCY_BED).ifPresent(tile -> {
            ItemStack stack = BedsheetsItem.forPattern(tile.getPattern()).getDefaultInstance();
            if (!stack.isEmpty()) {
                Block.popResource(world, pos, stack);
            }
            tile.setPattern(pattern);
            BlockState state = tile.getBlockState();
            BlockPos other = pos.relative(getNeighbourDirection(state.getValue(PART), state.getValue(FACING)));
            world.getBlockEntity(other, UBlockEntities.FANCY_BED).ifPresent(tile2 -> {
                tile2.setPattern(pattern);
            });
        });
    }

    private static Direction getNeighbourDirection(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    public static class Tile extends BlockEntity {
        private SheetPattern pattern = SheetPattern.NONE;

        public Tile(BlockPos pos, BlockState state) {
            super(UBlockEntities.FANCY_BED, pos, state);
        }

        @Override
        public boolean isValidBlockState(BlockState state) {
            return getType().isValid(state);
        }

        @Override
        public BlockEntityType<?> getType() {
            return UBlockEntities.FANCY_BED;
        }

        @Override
        protected void loadAdditional(ValueInput nbt) {
            pattern = nbt.read("pattern", SheetPattern.CODEC).orElse(SheetPattern.NONE);
        }

        @Override
        protected void saveAdditional(ValueOutput nbt) {
            nbt.store("pattern", SheetPattern.CODEC, pattern);
        }

        @Override
        public CompoundTag getUpdateTag(Provider lookup) {
            return saveWithoutMetadata(lookup);
        }

        public String getBase() {
            return ((FancyBedBlock)getBlockState().getBlock()).base;
        }

        public void setPattern(SheetPattern pattern) {
            this.pattern = pattern;
            setChanged();
            if (level instanceof ServerLevel sw) {
                sw.getChunkSource().blockChanged(getBlockPos());
            }
        }

        public SheetPattern getPattern() {
            return pattern;
        }
    }

    public enum SheetPattern implements StringRepresentable {
        NONE(DyeColor.WHITE),
        WHITE(DyeColor.WHITE),
        LIGHT_GRAY(DyeColor.LIGHT_GRAY),
        GRAY(DyeColor.GRAY),
        BLACK(DyeColor.BLACK),
        BROWN(DyeColor.BROWN),
        RED(DyeColor.RED),
        ORANGE(DyeColor.ORANGE),
        YELLOW(DyeColor.YELLOW),
        LIME(DyeColor.LIME),
        GREEN(DyeColor.GREEN),
        CYAN(DyeColor.CYAN),
        LIGHT_BLUE(DyeColor.LIGHT_BLUE),
        BLUE(DyeColor.BLUE),
        PURPLE(DyeColor.PURPLE),
        MAGENTA(DyeColor.MAGENTA),
        PINK(DyeColor.PINK),

        APPLE(null),
        BARS(null),
        CHECKER(null),
        KELP(null),
        RAINBOW(null),
        RAINBOW_BPW(null),
        RAINBOW_BPY(null),
        RAINBOW_PBG(null),
        RAINBOW_PWR(null);

        public static final Codec<SheetPattern> CODEC = StringRepresentable.fromEnum(SheetPattern::values);

        private final String name = name().toLowerCase(Locale.ROOT);
        @Nullable
        private final DyeColor color;

        SheetPattern(@Nullable DyeColor color) {
            this.color = color;
        }

        @Nullable
        public DyeColor getColor() {
            return color;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
