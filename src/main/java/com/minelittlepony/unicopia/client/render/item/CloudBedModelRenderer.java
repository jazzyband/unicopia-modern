package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.block.FancyBedBlock.SheetPattern;
import com.minelittlepony.unicopia.client.render.entity.CloudBedBlockEntityRenderer;
import com.minelittlepony.unicopia.item.FancyBedItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import java.util.function.Consumer;

public class CloudBedModelRenderer implements SpecialModelRenderer<SheetPattern> {
    private final CloudBedBlockEntityRenderer renderer;
    private final Identifier texture;
    private final boolean translucent;

    public CloudBedModelRenderer(CloudBedBlockEntityRenderer renderer, Identifier texture, boolean translucent) {
        this.renderer = renderer;
        this.texture = texture;
        this.translucent = translucent;
    }

    @Override
    public void submit(SheetPattern pattern, PoseStack matrices, SubmitNodeCollector vertices, int light, int overlay, boolean glint, int color) {
        renderer.renderAsItem(pattern, texture, translucent, matrices, vertices, light, overlay);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(0, 0, -1));
        extents.accept(new Vector3f(1, 1, 1));
    }

    @Override
    public SheetPattern extractArgument(ItemStack stack) {
        return FancyBedItem.getPattern(stack);
    }

    public record Unbaked(Identifier texture, boolean translucent) implements SpecialModelRenderer.Unbaked<SheetPattern> {
        public static final MapCodec<com.minelittlepony.unicopia.client.render.item.CloudBedModelRenderer.Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("texture").forGetter(com.minelittlepony.unicopia.client.render.item.CloudBedModelRenderer.Unbaked::texture),
                Codec.BOOL.fieldOf("translucent").forGetter(com.minelittlepony.unicopia.client.render.item.CloudBedModelRenderer.Unbaked::translucent)
        ).apply(i, com.minelittlepony.unicopia.client.render.item.CloudBedModelRenderer.Unbaked::new));

        @Override
        public MapCodec<com.minelittlepony.unicopia.client.render.item.CloudBedModelRenderer.Unbaked> type() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<SheetPattern> bake(SpecialModelRenderer.BakingContext context) {
            return new CloudBedModelRenderer(new CloudBedBlockEntityRenderer(), texture, translucent);
        }
    }
}
