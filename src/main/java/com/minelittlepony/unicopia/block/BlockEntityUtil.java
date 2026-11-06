package com.minelittlepony.unicopia.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface BlockEntityUtil {
    @SuppressWarnings("unchecked")
    @Nullable
    static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (@Nullable BlockEntityTicker<A>) ticker : null;
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    static <T extends BlockEntity> Optional<T> getOrCreateBlockEntity(Level world, BlockPos pos, BlockEntityType<T> type) {
        return world.getBlockEntity(pos, type).or(() -> {
            BlockState state = world.getBlockState(pos);
            if (!(state.hasBlockEntity())) {
                return Optional.empty();
            }
            BlockEntity e =  ((EntityBlock)state.getBlock()).newBlockEntity(pos, state);
            if (e == null || e.getType() != type) {
                return Optional.empty();
            }
            e.setBlockState(state);
            world.setBlockEntity(e);
            return Optional.of((T)e);
        });
    }

}
