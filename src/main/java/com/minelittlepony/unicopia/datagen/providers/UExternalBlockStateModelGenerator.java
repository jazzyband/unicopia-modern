package com.minelittlepony.unicopia.datagen.providers;

import static net.minecraft.client.data.models.model.TextureSlot.SIDE;
import static net.minecraft.client.data.models.model.TextureSlot.TOP;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import com.minelittlepony.unicopia.util.Tuple;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.EdibleBlock;
import com.minelittlepony.unicopia.datagen.IndirectionUtils;
import com.minelittlepony.unicopia.datagen.IndirectionUtils.IndirectMultipartBlockStateSupplier;

import static com.mojang.math.Quadrant.*;

public class UExternalBlockStateModelGenerator extends UBlockStateModelGenerator {
    public UExternalBlockStateModelGenerator(BlockModelGenerators modelGenerator, Consumer<BlockModelDefinitionGenerator> blockStateCollector) {
        super(blockStateCollector, modelGenerator.itemModelOutput, modelGenerator.modelOutput);
    }

    @Override
    public void run() {
        // bales
        registerAll((g, block) -> registerBale(Unicopia.id(block.getA().getPath().replace("bale", "block")), block.getA(), block.getB()),
                new Tuple<>(Identifier.withDefaultNamespace("hay_block"), "_top"),
                new Tuple<>(Identifier.fromNamespaceAndPath("farmersdelight", "rice_bale"), "_top"),
                new Tuple<>(Identifier.fromNamespaceAndPath("farmersdelight", "straw_bale"), "_end")
        );
    }


    public void registerBale(Identifier blockId, Identifier baseBlockId, String endSuffex) {
        Identifier top = baseBlockId.withPath(p -> "block/" + p + endSuffex);
        Identifier side = baseBlockId.withPath(p -> "block/" + p + "_side");
        TextureMapping textures = new TextureMapping().put(TOP, new net.minecraft.client.resources.model.sprite.Material(top)).put(SIDE, new net.minecraft.client.resources.model.sprite.Material(side));

        IndirectMultipartBlockStateSupplier supplier = IndirectionUtils.multipartBlockStateSupplier(blockId);
        Map<Integer, Identifier> uploadedModels = new HashMap<>();

        for (Direction.Axis axis : Direction.Axis.VALUES) {
            for (int i = 0; i < EdibleBlock.SEGMENTS.length; i++) {
                int index = i;
                if (axis == Direction.Axis.X) {
                    index = EdibleBlock.rotate(Direction.Axis.Z, index);
                    index = EdibleBlock.rotate(Direction.Axis.Z, index);
                    index = EdibleBlock.rotate(Direction.Axis.Z, index);
                    index = EdibleBlock.rotate(Direction.Axis.X, index);
                }
                if (axis == Direction.Axis.Z) {
                    index = EdibleBlock.rotate(Direction.Axis.X, index);
                }

                BooleanProperty segment = EdibleBlock.SEGMENTS[index];

                supplier.with(condition().term(EdibleBlock.AXIS, axis).term(segment, true), variant(plainModel(uploadedModels.computeIfAbsent(i, ii -> {
                            return BlockModels.BALE_MODELS[ii].getA().create(blockId.withPath(p -> "block/" + p + BlockModels.BALE_MODELS[ii].getB()), textures, modelOutput);
                        }))
                        .withXRot(axis == Direction.Axis.Y ? R0 : axis == Direction.Axis.X ? R90 : R90)
                        .withYRot(axis == Direction.Axis.Y ? R0 : axis == Direction.Axis.X ? R90 : R0))
                );
            }
        }

        blockStateOutput.accept(supplier);
    }
}
