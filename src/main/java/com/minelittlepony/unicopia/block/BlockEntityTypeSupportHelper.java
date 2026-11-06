package com.minelittlepony.unicopia.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface BlockEntityTypeSupportHelper {

    static BlockEntityTypeSupportHelper of(BlockEntityType<?> type) {
        return (BlockEntityTypeSupportHelper)type;
    }

    BlockEntityTypeSupportHelper addSupportedBlocks(Block... blocks);
}
