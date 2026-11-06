package com.minelittlepony.unicopia.datagen.providers;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.world.level.block.Block;

final class FireModels {
    static void registerSoulFire(BlockModelGenerators modelGenerator, Block fire, Block texture) {
        MultiVariant weightedVariant = modelGenerator.createFloorFireModels(texture);
        MultiVariant weightedVariant2 = modelGenerator.createSideFireModels(texture);
        modelGenerator.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(fire)
                    .with(weightedVariant)
                    .with(weightedVariant2)
                    .with(weightedVariant2.with(BlockModelGenerators.Y_ROT_90))
                    .with(weightedVariant2.with(BlockModelGenerators.Y_ROT_180))
                    .with(weightedVariant2.with(BlockModelGenerators.Y_ROT_270))
            );
    }

}
