package com.minelittlepony.unicopia.ability.data.tree;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public record TreeTypeImpl (
        Identifier name,
        boolean wideTrunk,
        Set<ResourceKey<Block>> logs,
        Set<ResourceKey<Block>> leaves,
        Supplier<Optional<Supplier<ItemStack>>> pool,
        int rarity,
        float leavesRatio
) implements TreeType {
    @Override
    public boolean isLeaves(BlockState state) {
        return findMatch(leaves, state) && isNonPersistent(state);
    }

    @Override
    public boolean isLog(BlockState state) {
        return findMatch(logs, state);
    }

    @Override
    public boolean isWide() {
        return wideTrunk;
    }

    @Override
    public ItemStack pickRandomStack(RandomSource random, BlockState state) {
        if (rarity <= 0 || random.nextInt(rarity) == 0) {
            return pool.get().map(Supplier::get).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @SuppressWarnings("deprecation")
    private static boolean findMatch(Set<ResourceKey<Block>> ids, BlockState state) {
        return ids.contains(state.getBlock().builtInRegistryHolder().key());
    }

    static boolean isNonPersistent(BlockState state) {
        return !state.hasProperty(LeavesBlock.PERSISTENT) || !state.getValue(LeavesBlock.PERSISTENT);
    }
}
