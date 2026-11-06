package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.SoundEmitter;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;

public class PieBlock extends Block implements SimpleWaterloggedBlock {
    public static final int MAX_BITES = 3;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, MAX_BITES);
    public static final BooleanProperty STOMPED = BooleanProperty.create("stomped");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape[] SHAPES;
    static {
        final int PIE_HEIGHT = 4;
        final VoxelShape WEDGE = Block.box(2, 0, 2, 8, PIE_HEIGHT, 8);
        final float OFFSET_AMOUNT = 6F/16F;
        SHAPES = new VoxelShape[] {
                Block.box(2, 0, 2, 14, PIE_HEIGHT, 14),
                Shapes.or(WEDGE, WEDGE.move(OFFSET_AMOUNT, 0, 0), WEDGE.move(OFFSET_AMOUNT, 0, OFFSET_AMOUNT)),
                Shapes.or(WEDGE, WEDGE.move(OFFSET_AMOUNT, 0, 0)),
                WEDGE
            };
    }

    private final ItemLike sliceItem;
    private final ItemLike normalItem;
    private final ItemLike stompedItem;

    public PieBlock(ItemLike sliceItem, ItemLike normalItem, ItemLike stompedItem, Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(STOMPED, false).setValue(WATERLOGGED, false));
        this.sliceItem = sliceItem;
        this.normalItem = normalItem;
        this.stompedItem = stompedItem;
    }


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(BITES)];
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        if (world.isClientSide()) {

            if (itemStack.is(UTags.Items.CAN_CUT_PIE)) {
                return InteractionResult.SUCCESS;
            }

            if (tryEat(world, pos, state, player).consumesAction()) {
                return InteractionResult.SUCCESS;
            }

            if (itemStack.isEmpty()) {
                return InteractionResult.CONSUME;
            }
        }

        if (itemStack.is(UTags.Items.CAN_CUT_PIE)) {
            SoundEmitter.playSoundAt(player, USounds.BLOCK_PIE_SLICE, SoundSource.NEUTRAL, 1, 1);
            removeSlice(world, pos, state, player);
            itemStack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            SoundEmitter.playSoundAt(player, USounds.BLOCK_PIE_SLICE_POP, SoundSource.NEUTRAL, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.9F);
            Block.popResource(world, pos, sliceItem.asItem().getDefaultInstance());
            return InteractionResult.SUCCESS;
        }

        return tryEat(world, pos, state, player);
    }

    protected InteractionResult tryEat(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        player.awardStat(Stats.EAT_CAKE_SLICE);
        player.getFoodData().eat(state.getValue(STOMPED) ? 1 : 2, 0.1f);

        world.gameEvent(player, GameEvent.EAT, pos);
        SoundEmitter.playSoundAt(player, USounds.Vanilla.GENERIC_EAT.value(), 0.5F, world.getRandom().nextFloat() * 0.1F + 0.9F);
        if (world instanceof Level ww && (!player.canEat(false) || world.getRandom().nextInt(10) == 0)) {
            AwaitTickQueue.scheduleTask(ww, w -> {
                SoundEmitter.playSoundAt(player, USounds.Vanilla.PLAYER_BURP, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.9F);
            }, 5);
        }

        removeSlice(world, pos, state, player);
        return InteractionResult.SUCCESS;
    }

    protected void removeSlice(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
        int bites = state.getValue(BITES);

        if (bites < MAX_BITES) {
            world.setBlock(pos, state.setValue(BITES, bites + 1), Block.UPDATE_ALL);
        } else {
            world.removeBlock(pos, false);
            world.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return (state.getValue(STOMPED) ? stompedItem : normalItem).asItem().getDefaultInstance();
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (state.getValue(STOMPED)) {
            Vec3 center = Vec3.atCenterOf(pos);
            world.addParticle(ParticleTypes.SNEEZE,
                    random.triangle(center.x(), 0.9),
                    random.triangle(center.y(), 0.9),
                    random.triangle(center.z(), 0.9),
                    0,
                    0,
                    0
            );
        } else {
            if (world.getRandom().nextInt(10) == 0) {
                Vec3 center = Vec3.atCenterOf(pos);
                world.addParticle(ParticleTypes.SNEEZE,
                        random.triangle(center.x(), 0.2),
                        random.triangle(center.y(), 0.2),
                        random.triangle(center.z(), 0.2),
                        0,
                        0.01F,
                        0
                );
            }
        }
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        if (!state.getValue(STOMPED) && entity instanceof LivingEntity && !entity.isSteppingCarefully()) {
            world.setBlockAndUpdate(pos, state.cycle(STOMPED));
            world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            world.gameEvent(UGameEvents.PIE_STOMP, pos, GameEvent.Context.of(entity, state));
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.getValue(WATERLOGGED)) {
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos.below()).isFaceSturdy(world, pos, Direction.UP);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.defaultBlockState()
                .setValue(WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER)
                .setValue(STOMPED, ctx.getItemInHand().is(UItems.APPLE_PIE_HOOF));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.getValue(WATERLOGGED)) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES, STOMPED, WATERLOGGED);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, net.minecraft.core.Direction direction) {
        return (5 - state.getValue(BITES)) * 2;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
