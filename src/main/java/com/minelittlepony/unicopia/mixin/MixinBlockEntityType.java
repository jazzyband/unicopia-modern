package com.minelittlepony.unicopia.mixin;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.minelittlepony.unicopia.block.BlockEntityTypeSupportHelper;

@Mixin(BlockEntityType.class)
abstract class MixinBlockEntityType implements BlockEntityTypeSupportHelper {
    @Shadow
    @Mutable
    private @Final Set<Block> validBlocks;

    @Override
    public BlockEntityTypeSupportHelper addSupportedBlocks(Block...blocks) {
        this.validBlocks = new HashSet<>(this.validBlocks);
        this.validBlocks.addAll(Set.of(blocks));
        return this;
    }
}
