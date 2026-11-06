package com.minelittlepony.unicopia.block.cloud;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.block.UBlockEntities;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CloudChestBlock extends ChestBlock implements CloudLike {
    private final BlockState baseState;
    private final CloudBlock baseBlock;

    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> NAME_RETRIEVER = new DoubleBlockCombiner.Combiner<>(){
        @Override
        public Optional<MenuProvider> acceptDouble(final ChestBlockEntity first, final ChestBlockEntity second) {
            final CompoundContainer inventory = new CompoundContainer(first, second);
            return Optional.of(new MenuProvider(){
                @Override
                @Nullable
                public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player player) {
                    if (first.canOpen(player) && second.canOpen(player)) {
                        first.unpackLootTable(playerInventory.player);
                        second.unpackLootTable(playerInventory.player);
                        return ChestMenu.sixRows(i, playerInventory, inventory);
                    }
                    return null;
                }

                @Override
                public Component getDisplayName() {
                    if (first.hasCustomName()) {
                        return first.getDisplayName();
                    }
                    if (second.hasCustomName()) {
                        return second.getDisplayName();
                    }
                    return Component.translatable(first.getBlockState().getBlock().getDescriptionId() + ".double");
                }
            });
        }

        @Override
        public Optional<MenuProvider> acceptSingle(ChestBlockEntity chest) {
            return Optional.of(chest);
        }

        @Override
        public Optional<MenuProvider> acceptNone() {
            return Optional.empty();
        }
    };

    public CloudChestBlock(BlockState baseState, Properties settings) {
        super(() -> UBlockEntities.CLOUD_CHEST, net.minecraft.sounds.SoundEvents.CHEST_OPEN, net.minecraft.sounds.SoundEvents.CHEST_CLOSE, CloudLike.applyCloudProperties(settings));
        this.baseState = baseState;
        this.baseBlock = (CloudBlock)baseState.getBlock();
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileData(pos, state);
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return combine(state, world, pos, false).apply(NAME_RETRIEVER).orElse(null);
    }

    @Override
    protected final VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (!baseBlock.canInteract(baseState, world, pos, EquineContext.of(context))) {
            return Shapes.empty();
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    protected final VoxelShape getOcclusionShape(BlockState state) {
        return super.getShape(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return this.hasCollision ? state.getShape(world, pos, context) : Shapes.empty();
    }

    @Override
    @Nullable
    public final BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!baseBlock.canInteract(baseState, context.getLevel(), context.getClickedPos(), EquineContext.of(context))) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!baseBlock.canInteract(baseState, world, pos, EquineContext.of(player))) {
            return InteractionResult.PASS;
        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean isMoving) {
        baseState.entityInside(world, pos, entity, handler, isMoving);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return type != PathComputationType.LAND || !InteractionManager.getInstance().getPathingEquineContext().collidesWithClouds();
    }

    public static class TileData extends ChestBlockEntity {
        protected TileData(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
            super(blockEntityType, blockPos, blockState);
        }

        public TileData(BlockPos pos, BlockState state) {
            super(UBlockEntities.CLOUD_CHEST, pos, state);
        }

        @Override
        protected Component getDefaultName() {
            return getBlockState().getBlock().getName();
        }
    }
}
