package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.block.state.StateUtil;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HayBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EdibleBlock extends HayBlock {
    private static final List<EdibleBlock> REGISTRY = new ArrayList<>();

    static final BooleanProperty TOP_NORTH_EAST = BooleanProperty.create("top_north_east");
    static final BooleanProperty TOP_NORTH_WEST = BooleanProperty.create("top_north_west");
    static final BooleanProperty TOP_SOUTH_EAST = BooleanProperty.create("top_south_east");
    static final BooleanProperty TOP_SOUTH_WEST = BooleanProperty.create("top_south_west");

    static final BooleanProperty BOTTOM_NORTH_EAST = BooleanProperty.create("bottom_north_east");
    static final BooleanProperty BOTTOM_NORTH_WEST = BooleanProperty.create("bottom_north_west");
    static final BooleanProperty BOTTOM_SOUTH_EAST = BooleanProperty.create("bottom_south_east");
    static final BooleanProperty BOTTOM_SOUTH_WEST = BooleanProperty.create("bottom_south_west");

    // [up/down][north/south][west/east]
    public static final BooleanProperty[] SEGMENTS = {
            BOTTOM_NORTH_WEST, BOTTOM_NORTH_EAST,
            BOTTOM_SOUTH_WEST, BOTTOM_SOUTH_EAST,

            TOP_NORTH_WEST, TOP_NORTH_EAST,
            TOP_SOUTH_WEST, TOP_SOUTH_EAST
    };

    private static final VoxelShape[] SHAPES = {
            Block.box(0, 0, 0, 8, 8, 8),
            Block.box(8, 0, 0, 16, 8, 8),
            Block.box(0, 0, 8, 8, 8, 16),
            Block.box(8, 0, 8, 16, 8, 16),
            Block.box(0, 8, 0, 8, 16, 8),
            Block.box(8, 8, 0, 16, 16, 8),
            Block.box(0, 8, 8, 8, 16, 16),
            Block.box(8, 8, 8, 16, 16, 16)
    };
    private static final Function<BlockState, VoxelShape> SHAPE_CACHE = Util.memoize(state -> {
        @Nullable
        VoxelShape shape = null;
        for (int i = 0; i < SEGMENTS.length; i++) {
            if (state.getValue(SEGMENTS[i])) {
                shape = shape == null ? SHAPES[i] : Shapes.or(shape, SHAPES[i]);
            }
        }
        return shape == null ? Shapes.block() : shape.optimize();
    });

    public static int rotate(Direction.Axis axis, int index) {
        Direction x = ((index & 1) == 0 ? Direction.EAST : Direction.WEST).getClockWise(axis);
        Direction y = ((index & 4) == 0 ? Direction.DOWN : Direction.UP).getClockWise(axis);
        Direction z = ((index & 2) == 0 ? Direction.NORTH : Direction.SOUTH).getClockWise(axis);
        return toIndex(x) | toIndex(y) | toIndex(z);
    }

    private static int toIndex(Direction direction) {
        if (direction.getAxis() == Direction.Axis.X) {
            direction = direction.getOpposite();
        }
        return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? getOffset(direction.getAxis()) : 0;
    }

    private static int getOffset(Direction.Axis axis) {
        return switch (axis) {
            case X -> 1;
            case Z -> 2;
            case Y -> 4;
        };
    }

    static void bootstrap() {
        UseBlockCallback.EVENT.register((Player player, Level world, InteractionHand hand, BlockHitResult hitResult) -> {
            if (!Pony.of(player).getSpecies().isEquine()
                    || (player.isSecondaryUseActive() && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()))) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            for (EdibleBlock edibleBlock : REGISTRY) {
                Block match = edibleBlock.getBaseBlock();
                if (match != Blocks.AIR && state.is(match)) {
                    BlockState copiedState = StateUtil.copyState(state, edibleBlock.defaultBlockState());
                    InteractionResult result = copiedState.useItemOn(player.getItemInHand(hand), world, player, hand, hitResult);

                    if (result.consumesAction()) {
                        return result;
                    }

                    if (result == InteractionResult.TRY_WITH_EMPTY_HAND && hand == InteractionHand.MAIN_HAND) {
                        InteractionResult actionResult = copiedState.useWithoutItem(world, player, hitResult);
                        if (actionResult.consumesAction()) {
                            return actionResult;
                        }
                    }
                }
            }

            return InteractionResult.PASS;
        });
    }

    private final Identifier baseBlock;
    private final Identifier material;

    public EdibleBlock(Identifier baseBlock, Identifier material, boolean register, Properties settings) {
        super(settings.overrideDescription(Util.makeDescriptionId("block", baseBlock)));
        for (BooleanProperty segment : SEGMENTS) {
            registerDefaultState(defaultBlockState().setValue(segment, true));
        }
        this.baseBlock = baseBlock;
        this.material = material;
        if (register) {
            REGISTRY.add(this);
            FlammableBlockRegistry.getDefaultInstance().add(this, 60, 20);
        }
    }

    public Block getBaseBlock() {
        return BuiltInRegistries.BLOCK.getValue(baseBlock);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SEGMENTS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE_CACHE.apply(state);
    }

    @Override
    @Deprecated
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator()) {
            return InteractionResult.FAIL;
        }

        if (!stack.isEmpty() && stack.is(BuiltInRegistries.ITEM.getValue(material))) {
            BooleanProperty segment = getHitCorner(hit, 1);

            if (!state.getValue(segment)) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                if (!world.isClientSide()) {
                    state = state.setValue(segment, true);
                    if (SHAPE_CACHE.apply(state) == Shapes.block()) {
                        state = StateUtil.copyState(state, getBaseBlock().defaultBlockState());
                    }
                    world.setBlockAndUpdate(pos, state);
                }
                world.playSound(player, pos, getSoundType(state).getPlaceSound(), SoundSource.BLOCKS);

                return InteractionResult.SUCCESS;
            }

            return InteractionResult.FAIL;
        }

        BooleanProperty corner = getHitCorner(hit, -1);

        if (!state.getValue(corner)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        boolean usingHoe = stack.is(ItemTags.HOES);

        if (!usingHoe) {
            if (!(player.isCreative() || player.getFoodData().needsFood()) || !player.isShiftKeyDown()) {
                return InteractionResult.FAIL;
            }
        }

        if (!world.isClientSide()) {
            state = state.setValue(corner, false);
            if (SHAPE_CACHE.apply(state) == Shapes.block()) {
                world.removeBlock(pos, false);
            } else {
                world.setBlockAndUpdate(pos, state);
            }
        }

        if (usingHoe) {
            stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            popResource(world, pos, BuiltInRegistries.ITEM.getValue(material).getDefaultInstance());
            player.playSound(USounds.Vanilla.HOE_TILL.value(), 1, 1);
        } else {
            player.playSound(USounds.Vanilla.GENERIC_EAT.value(), 1, 1);
            if (world.getRandom().nextInt(10) == 0) {
                player.playSound(USounds.Vanilla.PLAYER_BURP, 1, player.getVoicePitch());
            }
            player.getFoodData().eat(2, 1.3F);
        }
        return InteractionResult.SUCCESS;
    }

    static BooleanProperty getHitCorner(BlockHitResult hit, int direction) {
        Vec3 pos = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getUnitVec3i()).scale(direction * 0.001F));

        BlockPos bPos = hit.getBlockPos();

        return SEGMENTS[
                  (4 * getIndex(pos.y, bPos.getY()))
                + (2 * getIndex(pos.z, bPos.getZ()))
                + (getIndex(pos.x, bPos.getX()))
        ];
    }

    static int getIndex(double axisHit, int tile) {
        axisHit -= tile;
        return Math.abs(axisHit) > 0.5 ? 1 : 0;
    }
}
