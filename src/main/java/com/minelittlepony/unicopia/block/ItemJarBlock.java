package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.jar.EntityJarContents;
import com.minelittlepony.unicopia.block.jar.FluidOnlyJarContents;
import com.minelittlepony.unicopia.block.jar.ItemsJarContents;
import com.minelittlepony.unicopia.util.TypedActionResult;
import com.mojang.serialization.MapCodec;
import com.minelittlepony.unicopia.block.jar.FakeFluidJarContents;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;

public class ItemJarBlock extends JarBlock implements EntityBlock, WorldlyContainerHolder {

    public ItemJarBlock(Properties settings) {
        super(settings);
    }


    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).map(data -> data.interact(player, hand)).orElse(InteractionResult.TRY_WITH_EMPTY_HAND);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (!moved && !state.is(world.getBlockState(pos).getBlock())) {
            world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).ifPresent(data -> {
                data.getContents().onDestroyed();
            });
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, net.minecraft.core.Direction direction) {
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR)
                .map(TileData::getItems)
                .map(data -> Math.min(16, data.stacks().size()))
                .orElse(0);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level world, BlockPos pos, int type, int data) {
        super.triggerEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(type, data);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileData(pos, state);
    }

    @Nullable
    @Override
    public WorldlyContainer getContainer(BlockState state, LevelAccessor world, BlockPos pos) {
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).map(TileData::getItems).orElse(null);
    }

    public static class TileData extends BlockEntity {

        private JarContents contents = new ItemsJarContents(this);

        public TileData(BlockPos pos, BlockState state) {
            super(UBlockEntities.ITEM_JAR, pos, state);
        }

        public InteractionResult interact(Player player, InteractionHand hand) {
            TypedActionResult<JarContents> result = contents.interact(player, hand);
            contents = result.value();
            return result.result().consumesAction() ? InteractionResult.SUCCESS : result.result() == InteractionResult.FAIL ? InteractionResult.FAIL : InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        public JarContents getContents() {
            return contents;
        }

        @Nullable
        public ItemsJarContents getItems() {
            return getContents() instanceof ItemsJarContents c ? c : null;
        }

        @Nullable
        public EntityJarContents getEntity() {
            return getContents() instanceof EntityJarContents c ? c : null;
        }

        @Nullable
        public FluidJarContents getFluid() {
            return getContents() instanceof FluidJarContents c ? c : null;
        }

        @Nullable
        public FakeFluidJarContents getFakeFluid() {
            return getContents() instanceof FakeFluidJarContents c ? c : null;
        }

        @Override
        public Packet<ClientGamePacketListener> getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }

        @Override
        public CompoundTag getUpdateTag(Provider lookup) {
            return saveWithoutMetadata(lookup);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (getLevel() instanceof ServerLevel sw) {
                sw.getChunkSource().blockChanged(getBlockPos());
            }
        }

        @Override
        protected void loadAdditional(ValueInput nbt) {
            if (nbt.read("items", CompoundTag.CODEC).isPresent()) {
                contents = new ItemsJarContents(this, nbt.read("items", CompoundTag.CODEC).orElseGet(CompoundTag::new), nbt.lookup());
            } else if (nbt.read("entity", CompoundTag.CODEC).isPresent()) {
                contents = new EntityJarContents(this, nbt.read("entity", CompoundTag.CODEC).orElseGet(CompoundTag::new));
            } else if (nbt.read("fluid", CompoundTag.CODEC).isPresent()) {
                contents = new FluidOnlyJarContents(this, nbt.read("fluid", CompoundTag.CODEC).orElseGet(CompoundTag::new), nbt.lookup());
            } else if (nbt.read("fakeFluid", CompoundTag.CODEC).isPresent()) {
                contents = new FakeFluidJarContents(this, nbt.read("fakeFluid", CompoundTag.CODEC).orElseGet(CompoundTag::new));
            }
        }

        @Override
        protected void saveAdditional(ValueOutput nbt) {
            var items = getItems();
            if (items != null) {
                nbt.store("items", CompoundTag.CODEC, items.toNBT(new CompoundTag(), getLevel().registryAccess()));
            } else if (getEntity() != null) {
                nbt.store("entity", CompoundTag.CODEC, getEntity().toNBT(new CompoundTag(), getLevel().registryAccess()));
            } else if (getFluid() != null) {
                nbt.store("fluid", CompoundTag.CODEC, getFluid().toNBT(new CompoundTag(), getLevel().registryAccess()));
            } else if (getFakeFluid() != null) {
                nbt.store("fakeFluid", CompoundTag.CODEC, getFakeFluid().toNBT(new CompoundTag(), getLevel().registryAccess()));
            }
        }
    }

    public interface JarContents {
        TypedActionResult<JarContents> interact(Player player, InteractionHand hand);

        void onDestroyed();

        CompoundTag toNBT(CompoundTag compound, Provider lookup);

        default void consumeAndSwap(Player player, InteractionHand hand, ItemStack output) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(player.getItemInHand(hand), player, output.copy()));
        }
    }

    public interface FluidJarContents extends JarContents {
        FluidVariant fluid();

        default long amount() {
            return FluidConstants.BUCKET;
        }
    }
}
