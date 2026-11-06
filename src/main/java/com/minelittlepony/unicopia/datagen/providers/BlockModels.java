package com.minelittlepony.unicopia.datagen.providers;

import static net.minecraft.client.data.models.model.TextureSlot.BOTTOM;
import static net.minecraft.client.data.models.model.TextureSlot.SIDE;
import static net.minecraft.client.data.models.model.TextureSlot.TOP;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import com.minelittlepony.unicopia.util.Tuple;
import net.minecraft.world.level.block.Block;
import com.minelittlepony.unicopia.Unicopia;
import com.mojang.math.Quadrant;

public interface BlockModels {
    TextureSlot SHELL = TextureSlot.create("shell");
    TextureSlot STEP = TextureSlot.create("step");

    TexturedModel.Provider FRUIT = TexturedModel.createDefault(block -> TextureMapping.singleSlot(TextureSlot.CROSS, new Material(ModelLocationUtils.getModelLocation(block))), block("fruit", TextureSlot.CROSS));
    TexturedModel.Provider CUBE_BOTTOM = TexturedModel.createDefault(block -> {
        Identifier outside = ModelLocationUtils.getModelLocation(block);
        Identifier inside = ModelLocationUtils.getModelLocation(block, "_bottom");
        return new TextureMapping()
                .put(SIDE, new Material(outside))
                .put(TOP, new Material(outside))
                .put(BOTTOM, new Material(inside));
    }, ModelTemplates.CUBE_BOTTOM_TOP);

    ModelTemplate STRAIGHT_STAIRS = block("seethrough_stairs", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE, STEP);
    ModelTemplate INNER_STAIRS = block("inner_seethrough_stairs", "_inner", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE, STEP);
    ModelTemplate OUTER_STAIRS = block("outer_seethrough_stairs", "_outer", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE, STEP);
    ModelTemplate DOOR_LEFT = block("door_left", TextureSlot.BOTTOM, TextureSlot.TOP);
    ModelTemplate DOOR_RIGHT = block("door_right", TextureSlot.BOTTOM, TextureSlot.TOP);
    ModelTemplate TEMPLATE_PILLAR = block("template_pillar", TextureSlot.SIDE, TextureSlot.TOP, TextureSlot.BOTTOM);
    ModelTemplate TEMPLATE_PILLAR_END = block("template_pillar_end", "_end", TextureSlot.SIDE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.END);
    Identifier TEMPLATE_JAR = Unicopia.id("block/template_jar");

    Factory CROP = Factory.of(TextureMapping::crop, ModelTemplates.CROP);
    Factory CUBE_ALL = Factory.of(TextureMapping::cube, ModelTemplates.CUBE_ALL);
    TexturedModel.Provider SPIKES = TexturedModel.createDefault(b -> TextureMapping.crop(new Material(ModelLocationUtils.getModelLocation(b))), ModelTemplates.CROP);

    String[] FLATTENED_MODEL_SUFFEXES =     {"xyz", "yz", "xy", "y", "xz", "z", "x", "full"};
    String[] FLATTENED_MODEL_SUFFEXES_ROT = {"xyz", "xy", "yz", "y", "xz", "x", "z", "full"};
    Quadrant[] FLATTENED_MODEL_ROTATIONS = {
            Quadrant.R0, Quadrant.R270, Quadrant.R90, Quadrant.R180,
            Quadrant.R270, Quadrant.R180, Quadrant.R0, Quadrant.R90
    };

    ModelTemplate[] FLATTENED_MODELS = Arrays.stream(FLATTENED_MODEL_SUFFEXES)
            .map(variant -> block("flattened_corner_" + variant, "_corner_" + variant, TextureSlot.ALL))
            .toArray(ModelTemplate[]::new);
    ModelTemplate[] SHELL_MODELS = IntStream.range(1, 5)
            .mapToObj(i -> block("template_shell_" + i, "_" + i, SHELL))
            .toArray(ModelTemplate[]::new);
    ModelTemplate[] PIE_MODELS = Stream.of("_full", "_elbow", "_straight", "_corner")
            .map(variant -> block("pie" + variant, variant, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE, TextureSlot.INSIDE))
            .toArray(ModelTemplate[]::new);
    @SuppressWarnings("unchecked")
    Tuple<ModelTemplate, String>[] BALE_MODELS = Stream.of("bnw", "bne", "bsw", "bse", "tnw", "tne", "tsw", "tse")
            .map(suffex -> new Tuple<>(block("template_bale_" + suffex, "_" + suffex, TextureSlot.TOP, TextureSlot.SIDE), "_" + suffex))
            .toArray(Tuple[]::new);

    static ModelTemplate block(String parent, TextureSlot ... usedTextures) {
        return new ModelTemplate(Optional.of(Unicopia.id("block/" + parent)), Optional.empty(), usedTextures);
    }

    static ModelTemplate block(String parent, String variant, TextureSlot ... usedTextures) {
        return new ModelTemplate(Optional.of(Unicopia.id("block/" + parent)), Optional.of(variant), usedTextures);
    }

    static ModelTemplate block(Identifier parent, TextureSlot ... usedTextures) {
        return new ModelTemplate(Optional.of(parent.withPrefix("block/")), Optional.empty(), usedTextures);
    }

    static ModelTemplate block(Identifier parent, String variant, TextureSlot ... usedTextures) {
        return new ModelTemplate(Optional.of(parent.withPrefix("block/")), Optional.of(variant), usedTextures);
    }

    public interface Factory {
        static Factory of(Function<Material, TextureMapping> textureFunc, ModelTemplate model) {
            return (block, suffix) -> TexturedModel.createDefault(b -> textureFunc.apply(new Material(ModelLocationUtils.getModelLocation(b, suffix))), model).get(block);
        }

        TexturedModel get(Block block, String suffix);

        default Identifier upload(Block block, String suffix, BiConsumer<Identifier, ModelInstance> writer) {
            return get(block, suffix).createWithSuffix(block, suffix, writer);
        }
    }
}
