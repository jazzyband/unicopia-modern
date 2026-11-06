package com.minelittlepony.unicopia.block;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.minelittlepony.unicopia.util.PosHelper;

public interface SegmentedBlock {
    double[] STAGE_SIZES = new double[] {3.2, 6.4, 9.6, 12.8, 16};
    VoxelShape[] STAGE_SHAPES = Arrays.stream(STAGE_SIZES).mapToObj(height -> {
        return Block.box(0, 0, 0, 16, height, 16);
    }).toArray(VoxelShape[]::new);

    static VoxelShape getShape(int age) {
        if (age < 0 || age >= STAGE_SHAPES.length) {
            return Shapes.block();
        }
        return STAGE_SHAPES[age];
    }

    static double getHeight(int age) {
        if (age < 0 || age >= STAGE_SHAPES.length) {
            return 16;
        }
        return STAGE_SIZES[age];
    }

    static VoxelShape[] computeShapes(int maxHeight) {
       VoxelShape[] shapes = new VoxelShape[STAGE_SIZES.length * maxHeight];

       for (int i = 0; i < maxHeight; i++) {
           for (int j = 0; j < STAGE_SIZES.length; j++) {
               shapes[j + (i * STAGE_SIZES.length)] = Block.box(0, 0, 0, 16, STAGE_SIZES[j] + (i * 16), 16);
           }
       }

       return shapes;
    }

    boolean isBase(BlockState state);

    boolean isNext(BlockState state);

    default BlockPos getTip(BlockGetter world, BlockPos startingPos) {
        return PosHelper.traverseChain(world, startingPos, Direction.UP, this::isNext);
    }

    default BlockPos getRoot(BlockGetter world, BlockPos startingPos) {
        return PosHelper.traverseChain(world, startingPos, Direction.DOWN, this::isBase);
    }
}
