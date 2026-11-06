package com.minelittlepony.unicopia.client.render.item;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.CloudChestBlockEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import java.util.function.Consumer;

public class CloudChestModelRenderer implements NoDataSpecialModelRenderer {
    public static final Identifier TEXTURE = Unicopia.id("textures/entity/chest/cloud.png");
    private final CloudChestBlockEntityRenderer.Model model = new CloudChestBlockEntityRenderer.Model(CloudChestBlockEntityRenderer.Model.getSingleChestModelData().bakeRoot(), TEXTURE);

    private final Identifier texture;
    private final float openness;

    public CloudChestModelRenderer(Identifier texture, float openness) {
        this.texture = texture;
        this.openness = openness;
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector vertices, int light, int overlay, boolean glint, int color) {
        model.setAngles(openness);
        vertices.submitCustomGeometry(matrices, RenderTypes.entityTranslucent(texture), (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            model.render(stack, buffer, light, overlay);
        });
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(0, 0, 0));
        extents.accept(new Vector3f(1, 1, 1));
    }

    public record Unbaked(Identifier texture, float openness) implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<com.minelittlepony.unicopia.client.render.item.CloudChestModelRenderer.Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("texture").forGetter(com.minelittlepony.unicopia.client.render.item.CloudChestModelRenderer.Unbaked::texture),
                Codec.FLOAT.optionalFieldOf("openness", 0F).forGetter(com.minelittlepony.unicopia.client.render.item.CloudChestModelRenderer.Unbaked::openness)
        ).apply(i, com.minelittlepony.unicopia.client.render.item.CloudChestModelRenderer.Unbaked::new));

        @Override
        public MapCodec<com.minelittlepony.unicopia.client.render.item.CloudChestModelRenderer.Unbaked> type() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new CloudChestModelRenderer(texture, openness);
        }
    }
}
