package com.minelittlepony.unicopia.block.cloud;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.minelittlepony.unicopia.EquineContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CompactedCloudBlock extends CloudBlock {
    static final Map<Direction, BooleanProperty> FACING_PROPERTIES = PipeBlock.PROPERTY_BY_DIRECTION;
    static final Collection<BooleanProperty> PROPERTIES = FACING_PROPERTIES.values();

    static final Function<BlockState, VoxelShape> SHAPE_CACHE = Util.memoize(state -> {
        return Block.box(
                state.getValue(PipeBlock.WEST) ? 0 : 2,
                state.getValue(PipeBlock.DOWN) ? 0 : 2,
                state.getValue(PipeBlock.NORTH) ? 0 : 2,
                state.getValue(PipeBlock.EAST) ? 16 : 14,
                state.getValue(PipeBlock.UP) ? 16 : 14,
                state.getValue(PipeBlock.SOUTH) ? 16 : 14
        );
    });

    private final BlockState baseState;

    public CompactedCloudBlock(BlockState baseState, Properties settings) {
        super(true, settings.overrideLootTable(baseState.getBlock().getLootTable()));
        this.baseState = baseState;
        PROPERTIES.forEach(property -> {
            registerDefaultState(defaultBlockState().setValue(property, true));
        });
    }


    @Override
    protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return baseState.getCloneItemStack(world, pos, includeData);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, EquineContext equineContext) {
        return SHAPE_CACHE.apply(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROPERTIES.toArray(Property[]::new));
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        if (stack.is(ItemTags.SHOVELS)) {
            BooleanProperty property = FACING_PROPERTIES.get(hit.getDirection());
            if (state.getValue(property)) {
                world.setBlockAndUpdate(pos, state.setValue(property, false));
                stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                world.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN.value(), SoundSource.BLOCKS);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return transform(state, rotation::rotate);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return transform(state, mirror::mirror);
    }

    private BlockState transform(BlockState state, Function<Direction, Direction> transformation) {
        BlockState result = state;
        for (var property : FACING_PROPERTIES.entrySet()) {
            if (property.getKey().getAxis() != Direction.Axis.Y) {
                result = result.setValue(FACING_PROPERTIES.get(transformation.apply(property.getKey())), state.getValue(property.getValue()));
            }
        }
        return result;
    }
}
