package com.minelittlepony.unicopia.block.cloud;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;

public interface CloudLike {
    static BlockBehaviour.Properties applyCloudProperties(BlockBehaviour.Properties settings) {
        return settings.noOcclusion().dynamicShape().isValidSpawn((state, world, pos, type) -> {
            return type == EntityTypes.PHANTOM || type == EntityTypes.PARROT || type.getCategory() == MobCategory.AMBIENT;
        });
    }
}
