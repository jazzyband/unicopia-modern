package com.minelittlepony.unicopia.ability.data.tree;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.minelittlepony.unicopia.util.PosHelper;

public class TreeTraverser {
    private static final Direction[] WIDE_DIRS = new Direction[] {
            Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    private final TreeType type;

    private PosHelper.PositionRecord logs = new PosHelper.PositionRecord();
    private PosHelper.PositionRecord leaves = new PosHelper.PositionRecord();

    private final int maxRecurse = 50;

    public TreeTraverser(TreeType type) {
        this.type = type;
    }

    public PosHelper.PositionRecord collectLogs(Level w, BlockPos pos) {
        traverse(w, pos.mutable());
        return logs;
    }

    public PosHelper.PositionRecord collectLeaves(Level w, BlockPos pos) {
        traverse(w, findCanopy(w, pos));
        return leaves;
    }

    public void traverse(Level w, BlockPos pos) {
        traverse(w, pos.mutable());
    }

    private void traverse(Level w, BlockPos.MutableBlockPos pos) {
        logs = new PosHelper.PositionRecord();
        leaves = new PosHelper.PositionRecord();
        innerTraverse(w, pos, 0);
    }

    private void innerTraverse(Level w, BlockPos.MutableBlockPos pos, int recurseLevel) {
        if (recurseLevel >= maxRecurse || logs.hasVisited(pos) || leaves.hasVisited(pos)) {
            return;
        }

        BlockState state = w.getBlockState(pos);
        boolean yay = false;

        if (type.isLeaves(state)) {
            leaves.visit(pos);
            yay = true;
        } else if (type.isLog(state)) {
            logs.visit(pos);
            yay = true;
        }

        if (yay) {
            PosHelper.fastAll(pos, p -> innerTraverse(w, p, recurseLevel + 1), WIDE_DIRS);
        }
    }

    /**
     * Locates the top of the tree's trunk. Usually the point where wood meets leaves.
     */
    private BlockPos.MutableBlockPos findCanopy(Level w, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        while (type.isLog(w.getBlockState(mutable.move(Direction.UP)))) {
            if (PosHelper.fastAny(mutable, p -> type.isLeaves(w.getBlockState(p)), PosHelper.HORIZONTAL)) {
                break;
            }
        }
        return mutable.move(Direction.DOWN);
    }

    public Optional<BlockPos> findBase(Level w, BlockPos pos) {
        logs.clear();
        return findBase(w, pos.mutable());
    }

    private Optional<BlockPos> findBase(Level w, BlockPos.MutableBlockPos pos) {
        if (logs.hasVisited(pos) || !type.isLog(w.getBlockState(pos))) {
            return Optional.empty();
        }

        do {
            logs.visit(pos);
        } while (type.isLog(w.getBlockState(pos.move(Direction.DOWN))));
        pos.move(Direction.UP);

        if (type.isWide()) {
            PosHelper.fastAll(pos, p -> findBase(w, p)
                    .filter(a -> a.getY() < pos.getY())
                    .ifPresent(pos::set), PosHelper.HORIZONTAL);
        }

        return Optional.of(logs.visit(pos).immutable());
    }
}
