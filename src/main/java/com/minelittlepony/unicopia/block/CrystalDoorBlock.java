package com.minelittlepony.unicopia.block;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CrystalDoorBlock extends DoorBlock implements EntityBlock {
    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;

    public CrystalDoorBlock(BlockSetType blockSet, Properties settings) {
        super(blockSet, settings);
        registerDefaultState(defaultBlockState().setValue(LOCKED, false));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LOCKED);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(LOCKED);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.randomTick(state, world, pos, random);
        if (!isLocked(world, pos)) {
            setOnGuard(state, world, pos, false);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation orientation, boolean notify) {
        if (!state.getValue(LOCKED)) {
            boolean powered = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.relative(state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
            if (!defaultBlockState().is(sourceBlock) && powered != state.getValue(POWERED)) {
                if (powered) {
                    state = state.cycle(OPEN);
                    playSound(null, world, pos, state.getValue(OPEN));
                    world.gameEvent(null, state.getValue(OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
                }

                world.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
            }
        }

        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            if (!canSurvive(state, world, pos) && world.isEmptyBlock(pos.below())) {
                world.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!shouldProvideAccess(world, pos, player)) {
            if (isLocked(world, pos) || !stack.is(UItems.MEADOWBROOKS_STAFF)) {
                playSound(player, world, pos, false);
                setOnGuard(state, world, pos, true);
                return InteractionResult.CONSUME;
            } else {
                world.playSound(player, pos, USounds.ENTITY_CRYSTAL_SHARDS_AMBIENT, SoundSource.BLOCKS, 1, world.getRandom().nextFloat() * 0.1F + 0.9F);
            }
        } else if (!isLocked(world, pos)) {
            if (stack.is(UItems.FRIENDSHIP_BRACELET)) {
                @Nullable
                UUID signator = FriendshipBraceletItem.getSignatorId(stack);
                if (signator != null) {
                    BlockEntityUtil.getOrCreateBlockEntity(world, state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos, UBlockEntities.CRYSTAL_DOOR).ifPresent(data -> {
                        data.setSignator(signator);
                        setOnGuard(state, world, pos, true);
                        world.playSound(player, pos, USounds.ENTITY_CRYSTAL_SHARDS_AMBIENT, SoundSource.BLOCKS, 1, world.getRandom().nextFloat() * 0.1F + 0.9F);
                    });
                    return InteractionResult.SUCCESS;
                }
            } else {
                setOnGuard(state, world, pos, false);
            }
        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    private void setOnGuard(BlockState state, Level world, BlockPos pos, boolean locked) {
        world.setBlockAndUpdate(pos, state.setValue(LOCKED, locked));
        pos = pos.relative(state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN);
        state = world.getBlockState(pos);
        if (state.is(this)) {
            world.setBlockAndUpdate(pos, state.setValue(LOCKED, locked));
        }
    }

    private boolean shouldProvideAccess(Level world, BlockPos pos, Player player) {
        UUID signator = getSignator(world, pos);
        if (signator != null) {
            return signator.equals(player.getUUID()) || FriendshipBraceletItem.isComrade(signator, player);
        }
        return EquineContext.of(player).getCompositeRace().any(Race::canCast);
    }

    private boolean isLocked(Level world, BlockPos pos) {
        return getSignator(world, pos) != null;
    }

    @Nullable
    private UUID getSignator(Level world, BlockPos pos) {
        pos = world.getBlockState(pos).getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos;
        var d = world.getBlockEntity(pos, UBlockEntities.CRYSTAL_DOOR);
        return d.flatMap(data -> data.signator).orElse(null);
    }

    private void playSound(@Nullable Entity entity, Level world, BlockPos pos, boolean open) {
        world.playSound(entity, pos, open ? type().doorOpen() : type().doorClose(), SoundSource.BLOCKS, 1, world.getRandom().nextFloat() * 0.1f + 0.9f);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileData(pos, state);
    }

    public static class TileData extends BlockEntity {
        private Optional<UUID> signator = Optional.empty();

        public TileData(BlockPos pos, BlockState state) {
            super(UBlockEntities.CRYSTAL_DOOR, pos, state);
        }

        public void setSignator(UUID signator) {
            this.signator = Optional.ofNullable(signator);
            setChanged();
        }

        @Override
        protected void loadAdditional(net.minecraft.world.level.storage.ValueInput nbt) {
            signator = nbt.read("signator", UUIDUtil.AUTHLIB_CODEC);
        }

        @Override
        protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput nbt) {
            nbt.store("signator", CodecUtils.OPTIONAL_UUID, signator);
        }
    }
}
