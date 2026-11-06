package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.minelittlepony.unicopia.util.PosHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HiveBlock extends PipeBlock implements EntityBlock {
    static final BooleanProperty AWAKE = BooleanProperty.create("awake");
    static final BooleanProperty CONSUMING = BooleanProperty.create("consuming");
    static final Collection<BooleanProperty> PROPERTIES = PROPERTY_BY_DIRECTION.values();

    public HiveBlock(Properties settings) {
        super(0.5F, settings);
        registerDefaultState(defaultBlockState().setValue(AWAKE, false).setValue(CONSUMING, false));
        PROPERTIES.forEach(property -> {
            registerDefaultState(defaultBlockState().setValue(property, true));
        });
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROPERTIES.toArray(Property[]::new));
        builder.add(AWAKE, CONSUMING);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);

        if (random.nextInt(250) == 0) {
            world.playLocalSound(pos, USounds.BLOCK_CHITIN_AMBIENCE, SoundSource.BLOCKS, 0.13F, 0.2F, true);

            for (int i = 0; i < 9; i++) {
                world.addParticle(random.nextInt(2) == 0 ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.CRIMSON_SPORE,
                        pos.getX() + random.nextDouble(),
                        pos.getY() + 1.1, pos.getZ() + random.nextDouble(),
                        random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5
                );
            }
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(CONSUMING)) {
            return state;
        }
        boolean connected = !neighborState.isAir();
        state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), connected);

        if (!connected) {
            return state.setValue(AWAKE, true);
        }

        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(CONSUMING) || !state.getValue(AWAKE)) {
            return;
        }

        for (var property : PROPERTY_BY_DIRECTION.entrySet()) {
            if (!state.getValue(property.getValue())) {
                BlockPos neighborPos = pos.relative(property.getKey());
                //world.setBlockState(neighborPos, getDefaultState());
                world.setBlockAndUpdate(neighborPos, UBlocks.CHITIN.defaultBlockState());

                world.playSound(null, neighborPos, USounds.Vanilla.SLIME_BLOCK_PLACE, SoundSource.BLOCKS);
                world.playSound(null, neighborPos, USounds.BLOCK_CHITIN_AMBIENCE, SoundSource.BLOCKS, 0.13F, 0.2F);

                for (int i = 0; i < 9; i++) {
                    ParticleUtils.spawnParticle(world, random.nextInt(2) == 0 ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.CRIMSON_SPORE,
                            neighborPos.getX() + random.nextDouble(),
                            neighborPos.getY() + 1.1, pos.getZ() + random.nextDouble(),
                            random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5
                    );
                }
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (EquineContext.of(player).getCompositeRace().includes(Race.CHANGELING)) {
            world.setBlockAndUpdate(pos, state.setValue(CONSUMING, true));
            if (!world.isClientSide()) {
                BlockEntityUtil.getOrCreateBlockEntity(world, pos, UBlockEntities.HIVE_STORAGE).ifPresent(data -> {

                    if (data.opening) {
                        data.opening = false;
                        data.closing = true;
                    } else {
                        data.opening = true;
                        data.closing = false;
                    }
                    data.tickNow = true;
                    data.setChanged();
                });
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation orientation, boolean notify) {
        world.scheduleTick(pos, this, 15);
        super.neighborChanged(state, world, pos, sourceBlock, orientation, notify);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return EquineContext.of(context).getCompositeRace().includes(Race.CHANGELING) ? Shapes.empty() : super.getCollisionShape(state, world, pos, context);
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        float delta = super.getDestroyProgress(state, player, world, pos);
        delta *= Pony.of(player).getCompositeRace().includes(Race.CHANGELING) ? 2 : 1;
        return delta;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(CONSUMING) ? new TileData(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (!state.getValue(CONSUMING)) {
            return null;
        }
        return BlockEntityUtil.checkType(type, UBlockEntities.HIVE_STORAGE, TileData::tick);
    }

    static class TileData extends BlockEntity implements GameEventListener.Provider<TileData.Listener> {
        private final Map<BlockPos, Entry> storedBlocks = new HashMap<>();
        private final List<Set<BlockPos>> lastConsumed = new ArrayList<>();
        private boolean opening;
        private boolean closing;
        private boolean tickNow;
        private long lastTick;
        private final Listener listener;

        public TileData(BlockPos pos, BlockState state) {
            super(UBlockEntities.HIVE_STORAGE, pos, state);
            listener = new Listener(pos);
        }

        @Override
        protected void loadAdditional(net.minecraft.world.level.storage.ValueInput nbt) {
            opening = nbt.getBooleanOr("opening", false);
            closing = nbt.getBooleanOr("closing", false);
            storedBlocks.clear();
            nbt.read("storedBlocks", Entry.MAP_CODEC).ifPresent(storedBlocks::putAll);
        }

        @Override
        protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput nbt) {
            nbt.putBoolean("opening", opening);
            nbt.putBoolean("closing", closing);
            nbt.store("storedBlocks", Entry.MAP_CODEC, storedBlocks);
        }

        static void tick(Level world, BlockPos pos, BlockState state, TileData data) {
            if (data.tickNow || (world.getGameTime() > data.lastTick + 2)) {
                data.tickNow = false;
                data.lastTick = world.getGameTime();
                if (data.closing) {
                    data.stepClosing(world, pos);

                    if (data.lastConsumed.isEmpty() && state.getValue(CONSUMING)) {
                        world.setBlockAndUpdate(pos, state.setValue(CONSUMING, false));
                    }
                } else if (data.opening) {
                    data.stepOpening(world, pos);
                }
            }
        }

        private void stepOpening(Level world, BlockPos pos) {
            if (lastConsumed.size() >= 4) {
                return;
            }

            Set<BlockPos> consumed = new HashSet<>();
            for (BlockPos neighbor : lastConsumed.isEmpty() ? Set.of(pos) : lastConsumed.get(lastConsumed.size() - 1)) {
                if (!(neighbor.equals(pos) || (storedBlocks.containsKey(neighbor) && storedBlocks.get(neighbor).state().is(UBlocks.CHITIN)))) {
                    continue;
                }
                PosHelper.adjacentNeighbours(neighbor).forEach(adjacent -> {
                    BlockState s = world.getBlockState(adjacent);

                    if (consumed.add(adjacent.immutable()) && !storedBlocks.containsKey(adjacent)) {
                        BlockEntity data = world.getBlockEntity(adjacent);
                        storedBlocks.put(adjacent.immutable(), new Entry(adjacent.immutable(), s, data instanceof TileData ? Optional.empty() : Optional.ofNullable(data).map(d -> {
                            var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                                    net.minecraft.util.ProblemReporter.DISCARDING, world.registryAccess());
                            d.saveWithId(output);
                            return output.buildResult();
                        })));

                        if (s.is(UBlocks.CHITIN)) {
                            world.destroyBlock(adjacent, false);
                        } else if (s.is(UBlocks.HIVE)) {
                            world.setBlockAndUpdate(adjacent, s.setValue(CONSUMING, true));
                            TileData next = BlockEntityUtil.getOrCreateBlockEntity(world, adjacent.immutable(), UBlockEntities.HIVE_STORAGE).orElse(null);
                            if (next != null) {
                                next.opening = opening;
                                next.closing = closing;
                                next.tickNow = true;
                                next.setChanged();
                            }
                        }
                    }
                });
            }
            lastConsumed.add(consumed);
            setChanged();
        }

        private void stepClosing(Level world, BlockPos pos) {
            if (lastConsumed.isEmpty()) {
                closing = false;
                setChanged();
                return;
            }

            for (BlockPos neighbor : lastConsumed.remove(lastConsumed.size() - 1)) {
                @Nullable
                Entry entry = storedBlocks.remove(neighbor);
                if (entry != null && !entry.state().isAir()) {
                    if (world.getBlockState(entry.pos()).is(UBlocks.HIVE)) {
                        BlockEntityUtil.getOrCreateBlockEntity(world, entry.pos(), UBlockEntities.HIVE_STORAGE).ifPresent(data -> {
                            data.closing = closing;
                            data.opening = opening;
                            data.tickNow = true;
                            data.setChanged();
                        });
                    } else {
                        entry.restore(world);
                    }
                }
            }

            setChanged();
        }

        record Entry (BlockPos pos, BlockState state, Optional<CompoundTag> data) {
            public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                    BlockState.CODEC.fieldOf("state").forGetter(Entry::state),
                    CompoundTag.CODEC.optionalFieldOf("data").forGetter(Entry::data)
            ).apply(instance, Entry::new));
            public static final Codec<Map<BlockPos, Entry>> MAP_CODEC = CODEC.listOf().xmap(
                entries -> entries.stream().collect(Collectors.toMap(Entry::pos, Function.identity())),
                entries -> entries.values().stream().toList()
            );

            public void restore(Level world) {
                if (world.isEmptyBlock(pos)) {
                    world.setBlockAndUpdate(pos, state);
                    data.map(nbt -> BlockEntity.loadStatic(pos, state, nbt, world.registryAccess())).ifPresent(world::setBlockEntity);
                }
            }
        }

        @Override
        public Listener getListener() {
            return listener;
        }

        class Listener implements GameEventListener {
            private final PositionSource position;

            Listener(BlockPos pos) {
                this.position = new BlockPositionSource(pos);
            }

            @Override
            public PositionSource getListenerSource() {
                return position;
            }

            @Override
            public int getListenerRadius() {
                return 15;
            }

            @Override
            public boolean handleGameEvent(ServerLevel world, Holder<GameEvent> event, Context emitter, Vec3 emitterPos) {
                if (isImportant(event) || (EquinePredicates.IS_PLAYER.test(emitter.sourceEntity()) && !EquinePredicates.CHANGELING.test(emitter.sourceEntity()))) {
                    closing = true;
                    setChanged();
                    return true;
                }
                return false;
            }

            private boolean isImportant(Holder<GameEvent> event) {
                return event == GameEvent.EXPLODE
                    || event == GameEvent.PRIME_FUSE
                    || event == GameEvent.SHRIEK;
            }
        }
    }
}
