package com.minelittlepony.unicopia.ability.data.tree;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import com.minelittlepony.unicopia.util.PosHelper;

public class TreeTypes {
    private static Set<TreeType> entries = new HashSet<>();

    private static final TreeType any1x = createDynamic(false);
    private static final TreeType any2x = createDynamic(true);

    public static void load(Map<Identifier, TreeTypeLoader.TreeTypeDef> types) {
        entries = types.entrySet().stream().map(e -> e.getValue().toTreeType(e.getKey())).collect(Collectors.toSet());
    }

    static TreeType get(BlockState state, BlockPos pos, Level world) {
        return entries.stream()
                .filter(type -> type.matches(state))
                .findFirst()
                .map(type -> TreeType.of(type, findLeavesType(type, world, pos)))
                .orElseGet(() -> any1x.matches(state) ? (PosHelper.fastAny(pos, p -> world.getBlockState(p).is(state.getBlock()), PosHelper.HORIZONTAL) ? any2x : any1x) : TreeType.NONE);
    }

    static TreeType get(BlockState state) {
        return entries.stream()
                .filter(type -> type.matches(state))
                .findFirst()
                .orElse(TreeType.NONE);
    }

    private static TreeType findLeavesType(TreeType baseType, Level w, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        while (baseType.isLog(w.getBlockState(mutable.move(Direction.UP)))) {
            if (PosHelper.fastAny(mutable, p -> baseType.isLeaves(w.getBlockState(p)), PosHelper.HORIZONTAL)) {
                return baseType;
            }
        }
        return TreeType.of(w.getBlockState(mutable));
    }

    private static TreeType createDynamic(boolean wide) {
        return new TreeType() {
            @Override
            public boolean isLeaves(BlockState state) {
                return (state.is(BlockTags.LEAVES) || state.getBlock() instanceof LeavesBlock || entries.stream().anyMatch(t -> t.isLeaves(state))) && TreeTypeImpl.isNonPersistent(state);
            }

            @Override
            public boolean isLog(BlockState state) {
                return state.is(BlockTags.LOGS) || entries.stream().anyMatch(t -> t.isLog(state));
            }

            @Override
            public ItemStack pickRandomStack(RandomSource random, BlockState state) {
                TreeType type = get(state);
                if (type == TreeType.NONE) {
                    type = get(Blocks.OAK_LOG.defaultBlockState());
                }
                return type.pickRandomStack(random, state);
            }

            @Override
            public boolean isWide() {
                return wide;
            }

            @Override
            public float leavesRatio() {
                return 0.5F;
            }
        };
    }
}
